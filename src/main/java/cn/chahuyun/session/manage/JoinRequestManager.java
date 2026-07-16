package cn.chahuyun.session.manage;

import cn.chahuyun.session.HuYanSession;
import cn.chahuyun.session.data.ApplyClusterInfo;
import cn.chahuyun.session.entity.Power;
import cn.chahuyun.session.data.StaticData;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MemberJoinEvent;
import net.mamoe.mirai.event.events.MemberJoinRequestEvent;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Maintains all pending join requests without creating per-request listeners. */
public final class JoinRequestManager {

    private static final Pattern COMMAND = Pattern.compile("^(同意|拒绝|开门|关门) +(\\d+|all)$");
    private static final Pattern LIST = Pattern.compile("^[!！]申请列表$");
    private static final AtomicInteger NEXT_DOOR_NUMBER = new AtomicInteger(1);
    private static final Map<Integer, PendingRequest> REQUESTS = new ConcurrentHashMap<>();
    private static final Map<String, Integer> REQUEST_BY_MEMBER = new ConcurrentHashMap<>();
    private static final Map<String, ApplyClusterInfo> INFO_BY_MEMBER = new ConcurrentHashMap<>();

    private JoinRequestManager() {
    }

    public static synchronized int register(MemberJoinRequestEvent event) {
        long maximumSize = Math.max(1L, HuYanSession.CONFIG.getRuntimeCacheMaximumSize());
        while (REQUESTS.size() >= maximumSize) {
            REQUESTS.values().stream()
                    .min((left, right) -> Integer.compare(left.doorNumber, right.doorNumber))
                    .ifPresent(request -> expire(request.doorNumber, request));
        }
        int doorNumber = NEXT_DOOR_NUMBER.getAndIncrement();
        String memberKey = memberKey(event.getGroupId(), event.getFromId());
        Integer previousDoor = REQUEST_BY_MEMBER.put(memberKey, doorNumber);
        if (previousDoor != null) {
            PendingRequest previous = REQUESTS.get(previousDoor);
            if (previous != null) {
                removePending(previousDoor, previous, true);
            }
        }

        ApplyClusterInfo info = new ApplyClusterInfo();
        info.setJoinRequestEvent(event);
        INFO_BY_MEMBER.put(memberKey, info);
        PendingRequest request = new PendingRequest(doorNumber, memberKey, event, info);
        request.expiration = PluginRuntime.schedule(
                () -> expire(doorNumber, request),
                Math.max(1L, HuYanSession.CONFIG.getJoinRequestTimeoutMinutes()),
                TimeUnit.MINUTES
        );
        REQUESTS.put(doorNumber, request);
        return doorNumber;
    }

    public static boolean handle(GroupMessageEvent event) {
        String content = event.getMessage().contentToString().trim();
        if (!LIST.matcher(content).matches() && !COMMAND.matcher(content).matches()) {
            return false;
        }
        if (!hasPermission(event)) {
            return false;
        }
        if (LIST.matcher(content).matches()) {
            sendRequestList(event);
            return true;
        }

        Matcher matcher = COMMAND.matcher(content);
        if (!matcher.matches()) {
            return false;
        }
        boolean accept = "同意".equals(matcher.group(1)) || "开门".equals(matcher.group(1));
        String target = matcher.group(2);
        if ("all".equals(target)) {
            List<PendingRequest> requests = new ArrayList<>(REQUESTS.values());
            requests.removeIf(request -> request.resolved || request.event.getGroupId() != event.getGroup().getId());
            requests.forEach(request -> resolve(request, event, accept));
            event.getSubject().sendMessage(accept ? "已处理本群全部入群申请。" : "已拒绝本群全部入群申请。");
            return true;
        }

        int doorNumber = Integer.parseInt(target);
        PendingRequest request = REQUESTS.get(doorNumber);
        if (request == null || request.resolved || request.event.getGroupId() != event.getGroup().getId()) {
            event.getSubject().sendMessage("未找到该门牌号，申请可能已经处理或超时。");
            return true;
        }
        resolve(request, event, accept);
        event.getSubject().sendMessage(accept ? "好的，我这就开门。" : "该入群申请已拒绝。" );
        return true;
    }

    private static boolean hasPermission(GroupMessageEvent event) {
        if (event.getSender().getId() == HuYanSession.CONFIG.getOwner()) {
            return true;
        }
        if (event.getSender().getPermission() != MemberPermission.MEMBER) {
            return true;
        }
        String powerKey = event.getGroup().getId() + "." + event.getSender().getId();
        Power power = StaticData.getPowerMap(event.getBot()).get(powerKey);
        return power != null && (power.isAdmin() || power.isGroupManage() || power.isGroupHyc());
    }

    private static void resolve(PendingRequest request, GroupMessageEvent event, boolean accept) {
        synchronized (request) {
            if (request.resolved || REQUESTS.get(request.doorNumber) != request) {
                return;
            }
            request.info.setMessageEvent(event);
            try {
                if (accept) {
                    request.event.accept();
                    request.resolved = true;
                } else {
                    request.event.reject();
                    expire(request.doorNumber, request);
                }
            } catch (Exception exception) {
                expire(request.doorNumber, request);
                HuYanSession.LOGGER.warning("处理入群申请失败: " + exception.getMessage());
            }
        }
    }

    private static void sendRequestList(GroupMessageEvent event) {
        MessageChainBuilder builder = new MessageChainBuilder();
        builder.append("当前待处理入群申请:\n");
        REQUESTS.values().stream()
                .filter(request -> !request.resolved && request.event.getGroupId() == event.getGroup().getId())
                .sorted((left, right) -> Integer.compare(left.doorNumber, right.doorNumber))
                .forEach(request -> builder.append(String.format(
                        "门牌号:%d %s(%d) 口令:%s\n",
                        request.doorNumber,
                        request.event.getFromNick(),
                        request.event.getFromId(),
                        request.event.getMessage().isEmpty() ? "无" : request.event.getMessage()
                )));
        event.getSubject().sendMessage(builder.build());
    }

    public static ApplyClusterInfo markJoined(MemberJoinEvent event) {
        String key = memberKey(event.getGroup().getId(), event.getMember().getId());
        Integer doorNumber = REQUEST_BY_MEMBER.get(key);
        ApplyClusterInfo info = null;
        if (doorNumber != null) {
            PendingRequest request = REQUESTS.get(doorNumber);
            if (request != null) {
                info = request.info;
                removePending(doorNumber, request, false);
            }
        }
        if (info == null) {
            info = INFO_BY_MEMBER.computeIfAbsent(key, ignored -> new ApplyClusterInfo());
        } else {
            INFO_BY_MEMBER.put(key, info);
        }
        info.setJoinEvent(event);
        return info;
    }

    public static ApplyClusterInfo get(long groupId, long memberId) {
        return INFO_BY_MEMBER.get(memberKey(groupId, memberId));
    }

    public static void remove(long groupId, long memberId) {
        String key = memberKey(groupId, memberId);
        Integer doorNumber = REQUEST_BY_MEMBER.get(key);
        if (doorNumber != null) {
            PendingRequest request = REQUESTS.get(doorNumber);
            if (request != null) {
                removePending(doorNumber, request, true);
                return;
            }
        }
        REQUEST_BY_MEMBER.remove(key);
        INFO_BY_MEMBER.remove(key);
    }

    private static void expire(int doorNumber, PendingRequest expected) {
        removePending(doorNumber, expected, true);
    }

    private static void removePending(int doorNumber, PendingRequest expected, boolean removeInfo) {
        synchronized (expected) {
            REQUESTS.remove(doorNumber, expected);
            REQUEST_BY_MEMBER.remove(expected.memberKey, doorNumber);
            if (removeInfo) {
                INFO_BY_MEMBER.remove(expected.memberKey, expected.info);
            }
            ScheduledFuture<?> expiration = expected.expiration;
            if (expiration != null && !expiration.isDone()) {
                expiration.cancel(false);
            }
        }
    }

    public static void shutdown() {
        REQUESTS.values().forEach(request -> {
            if (request.expiration != null) {
                request.expiration.cancel(false);
            }
        });
        REQUESTS.clear();
        REQUEST_BY_MEMBER.clear();
        INFO_BY_MEMBER.clear();
    }

    static int pendingCount() {
        return REQUESTS.size();
    }

    private static String memberKey(long groupId, long memberId) {
        return groupId + "." + memberId;
    }

    private static final class PendingRequest {
        private final int doorNumber;
        private final String memberKey;
        private final MemberJoinRequestEvent event;
        private final ApplyClusterInfo info;
        private volatile boolean resolved;
        private volatile ScheduledFuture<?> expiration;

        private PendingRequest(int doorNumber, String memberKey, MemberJoinRequestEvent event, ApplyClusterInfo info) {
            this.doorNumber = doorNumber;
            this.memberKey = memberKey;
            this.event = event;
            this.info = info;
        }
    }
}
