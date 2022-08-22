package cn.chahuyun.utils;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.config.ConfigData;
import cn.chahuyun.data.ApplyClusterInfo;
import cn.chahuyun.data.StaticData;
import cn.chahuyun.entity.*;
import cn.chahuyun.enums.Mate;
import kotlin.coroutines.EmptyCoroutineContext;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.ConcurrencyKind;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.EventPriority;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.GroupEvent;
import net.mamoe.mirai.event.events.MemberJoinEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.MiraiLogger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
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
     * 解析消息中的变量，并识别为 [ MessageChain ]
     *
     * @param event   消息事件
     * @param message 解析的消息
     * @param object  附加的参数
     * @return net.mamoe.mirai.message.data.MessageChain
     * @author Moyuyanli
     * @date 2022/8/17 14:23
     */
    public static MessageChain parseMessageParameter(MessageEvent event, String message, Object... object) throws IOException {
        if (message.contains("$message(null)")) {
            return null;
        }
        String variablePattern = "\\$\\w+\\((\\S+?)\\)";
        Pattern pattern = Pattern.compile(variablePattern);
        Matcher matcher = pattern.matcher(message);
        MessageChainBuilder builder = new MessageChainBuilder();
        int index = 0;
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            String group = matcher.group();
            String[] split = group.split("\\(");
            String valueType = split[0].substring(1);
            String value = split[1].substring(0, split[1].length() - 1);
            Message messages = parseMessage(event, value, valueType, object);
            builder.append(MiraiCode.deserializeMiraiCode(message.substring(index, start)))
                    .append(messages);
            if (ConfigData.INSTANCE.getDebugSwitch()) {
                l.info("动态消息-" + group + "->" + messages);
            }
            index = end;
        }
        if (index < message.length()) {
            builder.append(MiraiCode.deserializeMiraiCode(message.substring(index)));
        }
        return builder.build();
    }

    public static MessageChain parseMessageParameter(GroupEvent event, String message, Object... object) throws IOException {
        if (message.contains("$message(null)")) {
            return null;
        }
        String variablePattern = "\\$\\w+\\((\\S+?)\\)";
        Pattern pattern = Pattern.compile(variablePattern);
        Matcher matcher = pattern.matcher(message);
        MessageChainBuilder builder = new MessageChainBuilder();
        int index = 0;
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            String group = matcher.group();
            String[] split = group.split("\\(");
            String valueType = split[0].substring(1);
            String value = split[1].substring(0, split[1].length() - 1);
            Message messages = parseMessage((MemberJoinEvent) event, value, valueType, object);
            builder.append(MiraiCode.deserializeMiraiCode(message.substring(index, start)))
                    .append(messages);
            if (ConfigData.INSTANCE.getDebugSwitch()) {
                l.info("动态消息-" + group + "->" + messages);
            }
            index = end;
        }
        if (index < message.length()) {
            builder.append(MiraiCode.deserializeMiraiCode(message.substring(index)));
        }
        return builder.build();
    }


    /**
     * 识别动态变量，并转换为消息 [ Message ]
     *
     * @param event     消息事件
     * @param value     变量值
     * @param valueType 变量类型
     * @param object    附加值
     * @return net.mamoe.mirai.message.data.MessageChain
     * @author Moyuyanli
     * @date 2022/8/17 14:22
     */
    private static Message parseMessage(MessageEvent event, String value, String valueType, Object... object) throws IOException {
        switch (valueType) {
            //at this qq
            case "at":
                if (value.equals("this")) {
                    return new At(event.getSender().getId());
                } else if (Pattern.matches("\\d+", value)) {
                    Contact subject = event.getSubject();
                    if (subject instanceof Group) {
                        NormalMember member = ((Group) subject).get(Long.parseLong(value));
                        if (member != null) {
                            return new At(member.getId());
                        }
                    }
                }
                return new PlainText("未识别动态消息:" + "$" + valueType + "(" + value + ")");
            case "message":
                switch (value) {
                    case "prohibitString":
                    case "jyString":
                        for (Object o : object) {
                            if (o instanceof GroupProhibited) {
                                return new PlainText(((GroupProhibited) o).getProhibitString());
                            }
                        }
                    case "this":
                        return event.getMessage();
                    default:
                        return new PlainText("未识别动态消息:" + "$" + valueType + "(" + value + ")");
                }
            case "user":
                switch (value) {
                    case "name":
                        return new PlainText(event.getSender().getNick());
                    case "id":
                        return new PlainText(event.getSender().getId() + "");
                    case "avatar":
                        return Contact.uploadImage(event.getSubject(), new URL(event.getSender().getAvatarUrl()).openConnection().getInputStream());
                    case "title":
                        return new PlainText(((NormalMember) event.getSender()).getSpecialTitle());
                    default:
                        return new PlainText("未识别动态消息:" + "$" + valueType + "(" + value + ")");
                }
            case "time":
                switch (value) {
                    case "now":
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String format = simpleDateFormat.format(new Date());
                        return new PlainText(format);
                    default:
                        String userFormat;
                        try {
                            SimpleDateFormat userSimpleDateFormat = new SimpleDateFormat(value);
                            userFormat = userSimpleDateFormat.format(new Date());
                        } catch (Exception e) {
                            l.warning("动态消息-时间格式化出错!", e);
                            return new PlainText("未识别动态消息:" + "$" + valueType + "(" + value + ")");
                        }
                        String trim = userFormat.replace("\\", "").trim();
                        return new PlainText(trim);
                }
        }

        return new PlainText("未识别动态消息:" + "$" + valueType + "(" + value + ")");
    }

    private static Message parseMessage(MemberJoinEvent event, String value, String valueType, Object... object) throws IOException {
        WelcomeMessage welcomeMessage = (WelcomeMessage) object[0];
        ApplyClusterInfo applyClusterInfo = (ApplyClusterInfo) object[1];
        switch (valueType) {
            //at this qq
            case "at":
                switch (value) {
                    case "this":
                        return new At(event.getMember().getId());
                    case "that":
                        try {
                            NormalMember invitor = applyClusterInfo.getJoinRequestEvent().getInvitor();
                            if (invitor != null) {
                                return new At(invitor.getId());
                            } else {
                                User sender = applyClusterInfo.getMessageEvent().getSender();
                                return new At(sender.getId());
                            }
                        } catch (Exception e) {
                            return new PlainText("本次动态消息无效!");
                        }
                    default:
                        if (Pattern.matches("\\d+", value)) {
                            NormalMember member = event.getGroup().get(Long.parseLong(value));
                            if (member != null) {
                                return new At(member.getId());
                            }
                        }
                        return new PlainText("未识别动态消息:" + "$" + valueType + "(" + value + ")");
                }
            case "message":
                switch (value) {
                    case "apply":
                        String message = applyClusterInfo.getJoinRequestEvent().getMessage();
                        return new PlainText(message.isEmpty()?"这个人什么都没说...":message);
                    default:
                        return new PlainText("未识别动态消息:" + "$" + valueType + "(" + value + ")");
                }
            case "user":
                switch (value) {
                    case "name":
                        return new PlainText(event.getMember().getNick());
                    case "id":
                        return new PlainText(event.getMember().getId() + "");
                    case "avatar":
                        return Contact.uploadImage(event.getMember(), new URL(event.getMember().getAvatarUrl()).openConnection().getInputStream());
                    case "title":
                        return new PlainText("群欢迎词不支持的动态消息:" + "$" + valueType + "(" + value + ")");
                    default:
                        return new PlainText("未识别动态消息:" + "$" + valueType + "(" + value + ")");
                }
            case "time":
                switch (value) {
                    case "now":
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String format = simpleDateFormat.format(new Date());
                        return new PlainText(format);
                    default:
                        String userFormat;
                        try {
                            SimpleDateFormat userSimpleDateFormat = new SimpleDateFormat(value);
                            userFormat = userSimpleDateFormat.format(new Date());
                        } catch (Exception e) {
                            l.warning("动态消息-时间格式化出错!", e);
                            return new PlainText("未识别动态消息:" + "$" + valueType + "(" + value + ")");
                        }
                        String trim = userFormat.replace("\\", "").trim();
                        return new PlainText(trim);
                }
        }

        return new PlainText("未识别动态消息:" + "$" + valueType + "(" + value + ")");
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
     * @return net.mamoe.mirai.event.events.MessageEvent
     * @author Moyuyanli
     * @date 2022/8/20 12:37
     */
    @NotNull
    public static MessageEvent getNextMessageEventFromUser(User user) throws InterruptedException, ExecutionException {
        EventChannel<MessageEvent> channel = GlobalEventChannel.INSTANCE.parentScope(HuYanSession.INSTANCE)
                .filterIsInstance(MessageEvent.class)
                .filter(event -> event.getSender().getId() == user.getId());

        CompletableFuture<MessageEvent> future = new CompletableFuture<>();

        channel.subscribeOnce(MessageEvent.class, EmptyCoroutineContext.INSTANCE,
                ConcurrencyKind.LOCKED, EventPriority.HIGH, future::complete);
        MessageEvent event = future.get();
        event.intercept();
        return event;
    }

}