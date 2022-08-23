package cn.chahuyun.manage;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.data.ApplyClusterInfo;
import cn.chahuyun.data.StaticData;
import cn.chahuyun.dialogue.Dialogue;
import cn.chahuyun.entity.*;
import cn.chahuyun.enums.Mate;
import cn.chahuyun.utils.BlackHouseUtil;
import cn.chahuyun.utils.HibernateUtil;
import cn.chahuyun.utils.ScopeUtil;
import cn.chahuyun.utils.ShareUtils;
import kotlin.coroutines.EmptyCoroutineContext;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.event.*;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MemberJoinEvent;
import net.mamoe.mirai.event.events.MemberJoinRequestEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.MiraiLogger;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;
import xyz.cssxsh.mirai.hibernate.MiraiHibernateRecorder;
import xyz.cssxsh.mirai.hibernate.entry.MessageRecord;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * GroupManager
 * 群管理类
 *
 * @author Moyuyanli
 * @date 2022/8/15 12:52
 */
public class GroupManager {


    public final static Map<String, ApplyClusterInfo> map = new HashMap<>();
    private final static MiraiLogger l = HuYanSession.INSTANCE.getLogger();
    private static int doorNumber = 0;


    /**
     * @param event 消息事件
     * @description 解禁言
     * @author Moyuyanli
     * @date 2022/6/21 16:44
     */
    public static void prohibit(MessageEvent event) {
        Contact subject = event.getSubject();
        String code = event.getMessage().serializeToMiraiCode();
        Bot bot = event.getBot();
        //判断bot的权限
        Group group = (Group) subject;
        boolean botIsAdmin = group.getBotPermission() == MemberPermission.MEMBER;
        if (botIsAdmin) {
            subject.sendMessage("人家还不是管理员哦~");
            return;
        }

        //获取群友对象
        Long qq = null;
        for (SingleMessage s : event.getMessage()) {
            if (s instanceof At) {
                qq = ((At) s).getTarget();
            }
        }
        if (qq == null) {
            subject.sendMessage("禁言失败，没有这个人呢！");
            return;
        }

        NormalMember member = Objects.requireNonNull(bot.getGroup(event.getSubject().getId())).get(qq);
        //获取参数
        String[] split = code.split(" ");
        String param = split[1];
        //分解参数
        String type = param.substring(param.length() - 1);
        int timeParam = Integer.parseInt(param.substring(0, param.length() - 1));
        if (timeParam == 0) {
            assert member != null;
            member.unmute();
            subject.sendMessage("解禁成功！");
            return;
        }
        //禁言时间计算
        int time = 0;
        MessageChainBuilder messages = new MessageChainBuilder().append("禁言成功!");
        switch (type) {
            case "s":
                time = timeParam;
                messages.append("禁言:").append(String.valueOf(timeParam)).append("秒");
                break;
            case "m":
                time = timeParam * 60;
                messages.append("禁言:").append(String.valueOf(timeParam)).append("分钟");
                break;
            case "h":
                time = timeParam * 60 * 60;
                messages.append("禁言:").append(String.valueOf(timeParam)).append("小时");
                break;
            case "d":
                time = timeParam * 60 * 60 * 24;
                messages.append("禁言:").append(String.valueOf(timeParam)).append("天");
                break;
            default:
                break;
        }
        //禁言
        assert member != null;
        try {
            member.mute(time);
        } catch (Exception e) {
            if (e instanceof PermissionDeniedException) {
                subject.sendMessage("禁言失败,你机器居然不是管理员???");
            } else {
                subject.sendMessage("出错啦~");
                l.error("出错啦~", e);
            }
        }
        subject.sendMessage(messages.build());
    }

    /**
     * 三种形式撤回消息
     * ！recall 撤回上一条
     * ！recall 5 撤回不带本条的前 5 条消息
     * ！recall 0-5 撤回从本条消息开始算起的总共6条消息
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/8/15 17:15
     */
    public static void recall(MessageEvent event) {
        //!recall 0? - 0?
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();

        //只有是群的情况下才会触发
        Group group;
        if (subject instanceof Group) {
            group = (Group) subject;
        } else {
            return;
        }

        //如果不是管理员，就直接不反应
        boolean botIsAdmin = group.getBotPermission() == MemberPermission.MEMBER;
        if (botIsAdmin) {
            return;
        }

        //拿到所有本群的所有消息
        List<MessageRecord> records = MiraiHibernateRecorder.INSTANCE.get(group).collect(Collectors.toList());

        //识别参数并分割消息
        String[] split = code.split(" +");
        if (split.length == 1) {
            records = records.subList(1, 2);
        } else if (split.length == 2) {
            String string = split[1];
            if (string.contains("-") || string.contains("~")) {
                String[] strings = string.split("[~-]");
                int start = Integer.parseInt(strings[0]);
                int end = Integer.parseInt(strings[1]);
                l.info("s-" + start + " e-" + end);
                records = records.subList(start, end);
            } else {
                int end = Integer.parseInt(split[1]);
                records = records.subList(1, end);
            }
        }

        //循环撤回
        for (MessageRecord record : records) {
            try {
                MessageSource.recall(record.toMessageSource());
            } catch (PermissionDeniedException e) {
                l.warning("消息撤回冲突-无权操作");
            } catch (IllegalStateException e) {
                l.warning("消息撤回冲突-已被撤回 或 消息未找到");
            } catch (Exception e) {
                subject.sendMessage("消息撤回失败!");
                l.error("出错啦~", e);
            }
        }
    }

    /**
     * 判断是否是违禁词
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/8/16 17:26
     */
    public static boolean isProhibited(MessageEvent event) throws IOException {
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        User sender = event.getSender();
        Bot bot = event.getBot();

        if (!(subject instanceof Group)) {
            return false;
        }

        GroupProhibited groupProhibited = null;

        Map<Scope, List<GroupProhibited>> prohibitedMap = StaticData.getProhibitedMap(bot);

        for (Scope scope : prohibitedMap.keySet()) {
            if (ShareUtils.mateScope(event, scope)) {
                List<GroupProhibited> groupProhibits = prohibitedMap.get(scope);
                for (GroupProhibited prohibited : groupProhibits) {
                    int mateType = prohibited.getMateType();
                    Mate mate = Mate.VAGUE;
                    String trigger = prohibited.getTrigger();
                    if (mateType == 1) {
                        mate = Mate.ACCURATE;
                    } else if (mateType == 3) {
                        mate = Mate.START;
                    } else if (mateType == 4) {
                        mate = Mate.END;
                    }
                    if (ShareUtils.mateMate(code, mate, trigger)) {
                        groupProhibited = prohibited;
                    }
                }
            }
        }

        if (groupProhibited == null) {
            return false;
        }

        //撤回
        if (groupProhibited.isWithdraw()) {
            try {
                MessageSource.recall(event.getMessage());
            } catch (PermissionDeniedException e) {
                l.warning("违禁词撤回失败-权限不足");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //禁言
        if (groupProhibited.isProhibit()) {
            Member member = (Member) sender;
            if (groupProhibited.getProhibitTime() > 0) {
                member.mute(groupProhibited.getProhibitTime());
            }
        }

        if (groupProhibited.isAccumulate()) {
            BlackHouse blackHouse = BlackHouseUtil.getBlackHouse(bot, sender.getId());
            if (blackHouse == null) {
                blackHouse = new BlackHouse(bot.getId(), sender.getId(), groupProhibited.getId(), 1);
            } else {
                blackHouse.setNumber(blackHouse.getNumber() + 1);
            }
            if (blackHouse.getNumber() >= groupProhibited.getAccumulateNumber()) {
                subject.sendMessage(sender.getNick() + "已经到达违禁词触发次数，将被踢出本群!");
                bot.getGroup(subject.getId()).get(sender.getId()).kick(sender.getNick() + "已经到达违禁词触发次数，将被踢出本群！");
                return true;
            }
            subject.sendMessage(MessageUtils.newChain()
                    .plus(new At(sender.getId()))
                    .plus(new PlainText("你已经违规 " + blackHouse.getNumber() + " 次，当违规 " + groupProhibited.getAccumulateNumber() + " 次就会被踢出!")));
            BlackHouseUtil.saveOrUpdate(blackHouse);
        }
        //回复消息
        MessageChain messages = ShareUtils.parseMessageParameter(event, groupProhibited.getReply(), groupProhibited);
        if (messages != null) {
            subject.sendMessage(messages);
        }

        return true;
    }

    /**
     * 有人申请入群
     *
     * @param event 群事件
     * @author Moyuyanli
     * @date 2022/8/22 10:41
     */
    public static void userRequestGroup(MemberJoinRequestEvent event) {
        Group group = event.getGroup();
        String fromNick = event.getFromNick();
        long fromId = event.getFromId();
        String message = event.getMessage();
        Long invitorId = event.getInvitorId();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = simpleDateFormat.format(new Date());

        Map<Integer, Long> eventMap = new HashMap<>();
        eventMap.put(doorNumber, event.getEventId());
        MessageChainBuilder messageChain = new MessageChainBuilder();
        messageChain.append(new PlainText("来人啦~!\n" +
                "门牌号:" +  doorNumber++ + "\n" +
                "时间:" + format + "\n" +
                "敲门人:" + fromNick + "(" + fromId + ")"));
        if (message.isEmpty()) {
            messageChain.append("\n敲门口令:(这个人啥也没说!)");
        } else {
            messageChain.append("\n敲门口令:").append(message);
        }

        try {
            if (invitorId != null) {
                messageChain.append("\n指路人:").append(group.get(invitorId).getNick()).append("(").append(String.valueOf(invitorId)).append(")");
            }
        } catch (Exception e) {
            l.warning("新人加群申请-欢迎消息构造失败!");
        }
        assert group != null;
        group.sendMessage(messageChain.build());

        EventChannel<GroupMessageEvent> channel = GlobalEventChannel.INSTANCE.parentScope(HuYanSession.INSTANCE)
                .filterIsInstance(GroupMessageEvent.class)
                .filter(nextGroup -> nextGroup.getGroup() == group)
                .filter(nextEvent -> {
                    String toString = nextEvent.getMessage().contentToString();
                    return Pattern.matches("(同意|拒绝|开门|关门) +(\\d+|all)|[!！]申请列表", toString);
                });


        map.put(event.getGroupId() + "." + event.getFromId(), new ApplyClusterInfo() {{
            setJoinRequestEvent(event);
        }});

        //手动控制监听什么时候结束
        channel.subscribe(GroupMessageEvent.class, EmptyCoroutineContext.INSTANCE,
                ConcurrencyKind.LOCKED, EventPriority.HIGH, messageEvent -> AgreeOrRefuseToApply(event, messageEvent,eventMap));

    }


    /**
     * 有人入群
     *
     * @param event 群事件
     * @author Moyuyanli
     * @date 2022/8/22 10:39
     */
    public static void userJoinGroup(MemberJoinEvent event) {
        Bot bot = event.getBot();
        Group group = event.getGroup();


        List<GroupWelcomeInfo> welcomeInfoList = null;
        try {
            welcomeInfoList = HibernateUtil.factory.fromTransaction(session -> {
                HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
                JpaCriteriaQuery<GroupWelcomeInfo> query = builder.createQuery(GroupWelcomeInfo.class);
                JpaRoot<GroupWelcomeInfo> from = query.from(GroupWelcomeInfo.class);

                query.select(from);
                query.where(builder.equal(from.get("bot"), bot.getId()));

                return session.createQuery(query).list();
            });
        } catch (Exception e) {
            l.error("出错啦!", e);
        }
        GroupWelcomeInfo groupWelcomeInfo = null;
        boolean next = true;
        for (GroupWelcomeInfo groupWelcome : welcomeInfoList) {
            Scope scope = ScopeUtil.getScope(groupWelcome.getScopeMark());
            assert scope != null;
            if (ShareUtils.mateScope(bot, group, scope)) {
                next = false;
                groupWelcomeInfo = groupWelcome;
                break;
            }
        }

        if (next) {
            return;
        }
        String mark = group.getId() + "." + event.getMember().getId();
        if (map.containsKey(mark)) {
            map.get(mark).setJoinEvent(event);
        } else {
            ApplyClusterInfo applyClusterInfo = new ApplyClusterInfo();
            applyClusterInfo.setJoinEvent(event);
            map.put(mark, applyClusterInfo);
        }

        Dialogue.INSTANCE.dialogueSession(event, groupWelcomeInfo);

    }

    //==============================================================================

    /**
     * 同意或拒绝这个请求
     *
     * @param apply 申请
     * @param event 消息
     * @return net.mamoe.mirai.event.ListeningStatus
     * @author Moyuyanli
     * @date 2022/8/22 11:10
     */
    private static ListeningStatus AgreeOrRefuseToApply(MemberJoinRequestEvent apply, GroupMessageEvent event,Map<Integer,Long> numbers) {
        Group group = event.getGroup();
        Member sender = event.getSender();
        Bot bot = event.getBot();
        //权限用户识别符
        String powerString = group.getId() + "." + sender.getId();

        Map<String, Power> powerMap = StaticData.getPowerMap(bot);
        MemberPermission permission = event.getGroup().get(event.getSender().getId()).getPermission();
        if (permission == MemberPermission.MEMBER) {
            if (!powerMap.containsKey(powerString)) {
                return ListeningStatus.LISTENING;
            }
            Power power = powerMap.get(powerString);
            if (!power.isAdmin() && !power.isGroupManage()) {
                return ListeningStatus.LISTENING;
            }
        }
        String content = event.getMessage().contentToString();
        if (Pattern.matches("同意 \\d+", content)) {
            int number  = Integer.parseInt(content.substring(3));
            if (!numbers.containsKey(number)) {
                return ListeningStatus.LISTENING;
            }
            Long eventId = numbers.get(number);
            if (apply.getEventId() == eventId) {
                apply.accept();
                map.get(apply.getGroupId() + "." + apply.getFromId()).setMessageEvent(event);
            }
            return ListeningStatus.STOPPED;
        } else if (Pattern.matches("开门 \\d+", content)) {
            int number  = Integer.parseInt(content.substring(3));
            if (!numbers.containsKey(number)) {
                return ListeningStatus.LISTENING;
            }
            Long eventId = numbers.get(number);
            if (apply.getEventId() == eventId) {
                event.getSubject().sendMessage("好的，我这就开门");
                apply.accept();
                map.get(apply.getGroupId() + "." + apply.getFromId()).setMessageEvent(event);
            }
            return ListeningStatus.STOPPED;
        }else if (Pattern.matches("开门 all", content)) {
            event.getSubject().sendMessage("大门开着的，都进来了");
            apply.accept();
            map.get(apply.getGroupId() + "." + apply.getFromId()).setMessageEvent(event);
            return ListeningStatus.STOPPED;
        } else if (Pattern.matches("同意 all", content)) {
            apply.accept();
            map.get(apply.getGroupId() + "." + apply.getFromId()).setMessageEvent(event);
            return ListeningStatus.STOPPED;
        } else if (Pattern.matches("拒绝 \\d+", content)) {
            int number  = Integer.parseInt(content.substring(3));
            if (!numbers.containsKey(number)) {
                return ListeningStatus.LISTENING;
            }
            Long eventId = numbers.get(number);
            if (apply.getEventId() == eventId) {
                apply.reject();
                map.get(apply.getGroupId() + "." + apply.getFromId()).setMessageEvent(event);
            }
            return ListeningStatus.STOPPED;
        } else if (Pattern.matches("关门 \\d+", content)) {
            int number  = Integer.parseInt(content.substring(3));
            if (!numbers.containsKey(number)) {
                return ListeningStatus.LISTENING;
            }
            Long eventId = numbers.get(number);
            if (apply.getEventId() == eventId) {
                event.getSubject().sendMessage("门我反锁了！");
                apply.accept();
                map.get(apply.getGroupId() + "." + apply.getFromId()).setMessageEvent(event);
            }
            return ListeningStatus.STOPPED;
        } else if (Pattern.matches("拒绝 all", content)) {
            apply.accept();
            map.get(apply.getGroupId() + "." + apply.getFromId()).setMessageEvent(event);
            return ListeningStatus.STOPPED;
        } else if (Pattern.matches("锁大门", content)) {
            event.getSubject().sendMessage("大门我上锁了！");
            apply.accept();
            map.get(apply.getGroupId() + "." + apply.getFromId()).setMessageEvent(event);
            return ListeningStatus.STOPPED;
        } else {
            String fromNick = apply.getFromNick();
            long fromId = apply.getFromId();
            String message = apply.getMessage();
            Long invitorId = apply.getInvitorId();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String format = simpleDateFormat.format(new Date());
            MessageChainBuilder messageChain = new MessageChainBuilder();
            messageChain.append(new PlainText("门外还有人呢!\n" +
                    "门牌号:" +  doorNumber++ + "\n" +
                    "时间:" + format + "\n" +
                    "敲门人:" + fromNick + "(" + fromId + ")"));
            if (message.isEmpty()) {
                messageChain.append("\n敲门口令:(这个人啥也没说!)");
            } else {
                messageChain.append("\n敲门口令:").append(message);
            }

            try {
                if (invitorId != null) {
                    messageChain.append("\n指路人:").append(group.get(invitorId).getNick()).append("(").append(String.valueOf(invitorId)).append(")");
                }
            } catch (Exception e) {
                l.warning("新人加群申请-欢迎消息构造失败!");
            }
            group.sendMessage(messageChain.build());
        }
        return ListeningStatus.LISTENING;
    }

}