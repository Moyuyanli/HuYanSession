package cn.chahuyun.utils;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.config.ConfigData;
import cn.chahuyun.data.StaticData;
import cn.chahuyun.entity.GroupInfo;
import cn.chahuyun.entity.GroupList;
import cn.chahuyun.entity.Scope;
import cn.chahuyun.enums.Mate;
import kotlin.coroutines.EmptyCoroutineContext;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.ConcurrencyKind;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.EventPriority;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.SingleMessage;
import net.mamoe.mirai.utils.MiraiLogger;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ShareUtils
 *
 * @author Moyuyanli
 * @description 公共工具包
 * @date 2022/7/29 12:40
 */
public class ShareUtils {

    private final static MiraiLogger l = HuYanSession.INSTANCE.getLogger();

    private static final Map<String, Integer> map = new HashMap<>();
    /**
     * String.format用法
     * format 替换 %s ， 下一个String字符
     */
    public static final String DYNAMIC_MESSAGE_PATTERN = String.format("\\%s\\w+\\((\\S+?)\\)", ConfigData.INSTANCE.getVariableSymbol());


    private ShareUtils() {
    }

    /**
     * 返回是否为退出
     *
     * @param event 消息事件
     * @return boolean true 退出
     * @author Moyuyanli
     * @date 2022/7/29 12:43
     */
    public static boolean isQuit(MessageEvent event) {
        String messagePattern = "^!!!|^！！！";
        Pattern pattern = Pattern.compile(messagePattern);
        Matcher matcher = pattern.matcher(event.getMessage().serializeToMiraiCode());
        return matcher.find();
    }

    /**
     * 添加下N条忽略消息
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/8/15 9:19
     */
    public static void spotPause(MessageEvent event) {
        //! pause @bot num?
        MessageChain message = event.getMessage();
        String contentToString = message.contentToString();
        Contact subject = event.getSubject();
        Bot thisBot = event.getBot();
        User sender = event.getSender();

        long botQq = 0;
        for (SingleMessage singleMessage : message) {
            if (singleMessage instanceof At) {
                botQq = ((At) singleMessage).getTarget();
            }
        }
        Bot bot = Bot.getInstance(botQq);
        if (bot != thisBot) {
            return;
        }

        String[] split = contentToString.split(" +");
        int num = 1;
        if (split.length == 3) {
            num = Integer.parseInt(split[2]);
        }

        String mark = botQq + "." + sender.getId();

        map.put(mark, num);
        subject.sendMessage(bot.getNick() + "(" + botQq + ")开始忽略接下来你的 " + num + " 条消息");
    }

    /**
     * 判断该用户的下一条消息是否忽略
     *
     * @param event 消息事件
     * @return boolean  true 忽略下一条消息
     * @author Moyuyanli
     * @date 2022/8/15 9:18
     */
    public static boolean isPause(MessageEvent event) {
        Bot bot = event.getBot();
        User sender = event.getSender();

        String mark = bot.getId() + "." + sender.getId();

        if (map.containsKey(mark)) {
            Integer integer = map.get(mark);
            l.info("integer-" + integer);
            if (integer > 0) {
                map.put(mark, integer - 1);
                return true;
            }
            return false;
        }
        return false;
    }

    /**
     * 匹配作用域
     *
     * @param event 消息事件
     * @param scope 作用域
     * @return boolean true 匹配成功! false 匹配失败！
     * @author Moyuyanli
     * @date 2022/7/13 21:34
     */
    public static boolean mateScope(MessageEvent event, Scope scope) {
        Bot bot = event.getBot();
        long group = event.getSubject().getId();

        Map<Integer, GroupList> groupListMap = StaticData.getGroupListMap(bot);

        if (scope.getGroupInfo()) {
            GroupList groupList = groupListMap.get(scope.getListId());
            List<GroupInfo> groupNumbers = groupList.getGroups();
            for (GroupInfo aLong : groupNumbers) {
                if (group == aLong.getGroupId()) {
                    return true;
                }
            }
        } else if (scope.getGlobal()) {
            return true;
        } else {
            long l = scope.getGroupNumber();
            return l == group;
        }
        return false;
    }

    /**
     * 匹配作用域
     *
     * @param bot   所属机器人
     * @param group 匹配群
     * @param scope 作用域
     * @return boolean true 匹配成功! false 匹配失败！
     * @author Moyuyanli
     * @date 2022/7/13 21:34
     */
    public static boolean mateScope(Bot bot, Group group, Scope scope) {
        Map<Integer, GroupList> groupListMap = StaticData.getGroupListMap(bot);

        if (scope.getGroupInfo()) {
            GroupList groupList = groupListMap.get(scope.getListId());
            List<GroupInfo> groupNumbers = groupList.getGroups();
            for (GroupInfo aLong : groupNumbers) {
                if (group.getId() == aLong.getGroupId()) {
                    return true;
                }
            }
        } else if (scope.getGlobal()) {
            return true;
        } else {
            long l = scope.getGroupNumber();
            return l == group.getId();
        }
        return false;
    }

    /**
     * 匹配匹配方式
     *
     * @param code 消息
     * @param mate 匹配方式
     * @param key  匹配内容
     * @return boolean true 匹配成功! false 匹配失败！
     * @author Moyuyanli
     * @date 2022/7/13 21:40
     */
    public static boolean mateMate(String code, Mate mate, String key) {
        switch (mate) {
            case ACCURATE:
                if (code.equals(key)) {
                    return true;
                }
                break;
            case VAGUE:
                if (code.contains(key)) {
                    return true;
                }
                break;
            case START:
                if (code.startsWith(key)) {
                    return true;
                }
                break;
            case END:
                if (code.endsWith(key)) {
                    return true;
                }
                break;
            default:
                break;
        }
        return false;
    }

    /**
     * 获取用户的下一次消息事件
     *
     * @param user 用户
     * @return MessageEvent
     * @author Moyuyanli
     * @date 2022/8/20 12:37
     */
    @NotNull
    public static MessageEvent getNextMessageEventFromUser(User user) {
        EventChannel<MessageEvent> channel = GlobalEventChannel.INSTANCE.parentScope(HuYanSession.INSTANCE)
                .filterIsInstance(MessageEvent.class)
                .filter(event -> event.getSender().getId() == user.getId());

        CompletableFuture<MessageEvent> future = new CompletableFuture<>();

        channel.subscribeOnce(MessageEvent.class, EmptyCoroutineContext.INSTANCE,
                ConcurrencyKind.LOCKED, EventPriority.HIGH, future::complete);
        MessageEvent event = null;
        try {
            event = future.get();
        } catch (InterruptedException | ExecutionException e) {
            l.error("获取下一条消息出错!", e);
        }
        assert event != null;
        event.intercept();
        return event;
    }

    @NotNull
    public static Mate getMate(int mateType) {
        switch (mateType) {
            case 2:
                return Mate.VAGUE;
            case 3:
                return Mate.START;
            case 4:
                return Mate.END;
            case 1:
            default:
                return Mate.ACCURATE;
        }
    }

}