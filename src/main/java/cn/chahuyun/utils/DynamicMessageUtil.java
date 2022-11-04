package cn.chahuyun.utils;

import cn.chahuyun.config.ConfigData;
import cn.chahuyun.data.ApplyClusterInfo;
import cn.chahuyun.entity.GroupProhibited;
import cn.chahuyun.entity.GroupWelcomeInfo;
import cn.chahuyun.entity.Session;
import cn.hutool.core.lang.Assert;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.GroupEvent;
import net.mamoe.mirai.event.events.MemberJoinEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.chahuyun.HuYanSession.log;
import static cn.chahuyun.utils.ShareUtils.DYNAMIC_MESSAGE_PATTERN;

/**
 * 动态消息工具
 *
 * @author Moyuyanli
 * @Date 2022/8/27 23:20
 */
public class DynamicMessageUtil {


    /**
     * 解析消息中的变量，并识别为 [ MessageChain ]
     * 供发送消息使用
     *
     * @param event   消息事件
     * @param message 解析的消息
     * @param object  附加的参数
     * @return net.mamoe.mirai.message.data.MessageChain
     * @author Moyuyanli & forDecember
     * @date 2022/8/17 14:23
     */
    public static MessageChain parseMessageParameter(MessageEvent event, String message, Object... object) {
        if (message.contains(ConfigData.INSTANCE.getVariableSymbol() + "message(null)")) {
            return null;
        }
        //动态消息 表示正则匹配
        Pattern pattern = Pattern.compile(DYNAMIC_MESSAGE_PATTERN);
        Matcher matcher = pattern.matcher(message);

        // 小D:获取数据库里的正则
        assert object[0] instanceof Session;

        Session sessionInfo = (Session) object[0];

        HashMap<String,String> triggerPatternMap = new HashMap<>(); // 记录捕获到的触发词
        String parsedTrigger = sessionInfo.getTerm();
        parsedTrigger = parsedTrigger.replaceAll("\\$pattern", "");
        Pattern parsedTriggerPattern = Pattern.compile(parsedTrigger);
        Matcher parsedTriggerMatcher = parsedTriggerPattern.
                matcher(event.getMessage().serializeToMiraiCode());
        if (!parsedTriggerMatcher.matches()) throw new AssertionError();
        // ShareUtils中已判断过，后面用不到matches，能走到这肯定是true

        int count = parsedTriggerMatcher.groupCount(); // 记录触发词中正则表达式数量
        for (int i = 1; i <= count; i++) {
            triggerPatternMap.put(String.valueOf(i), parsedTriggerMatcher.group(i));
        }




        //返回消息 构造
        MessageChainBuilder builder = new MessageChainBuilder();

        //记录匹配后的动态消息末尾下标
        int index = 0;

        //开始寻找动态消息
        while (matcher.find()) {
            //匹配到的字符串开始下标
            int start = matcher.start();
            //匹配到的字符串结束下标
            int end = matcher.end();
            //匹配到的 动态消息标识 ' $xx(xx) '
            String group = matcher.group();
            //通过左括号分割
            String[] split = group.split("\\(");
            //截取第一位到最后 -> 去掉 ' $ ' = 动态消息的属性
            String valueType = split[0].substring(1);
            //从第一位截取到倒数第二位 -> 去掉 ' ) ' = 动态消息的内容
            String value = split[1].substring(0, split[1].length() - 1);

            Message messages = null;
            try {
                //进行动态消息的转换
                messages = parseMessage(event, value, valueType,triggerPatternMap, object);
            } catch (IOException e) {
                log.error("转换动态消息出错!", e);
            }
            //从回复消息中的第一位到动态消息标识的第一位 ' $ ' 截取出来
            assert messages != null;
            builder.append(MiraiCode.deserializeMiraiCode(message.substring(index, start)))
                    //再把转换的动态消息拼接
                    .append(messages);
            if (ConfigData.INSTANCE.getDebugSwitch()) {
                log.info("动态消息-" + group + "->" + messages);




            }
            //记录末尾下标
            index = end;
        }
        //如果记录的下标 小于 回复消息的长度 -> 动态消息标识末尾还有字符
        if (index < message.length()) {
            //拼接
            builder.append(MiraiCode.deserializeMiraiCode(message.substring(index)));
        }
        return builder.build();
    }

    /**
     * 解析消息中的变量，并识别为 [ MessageChain ]
     * 欢迎词使用
     *
     * @param event   消息事件
     * @param message 解析的消息
     * @param object  附加的参数
     * @return net.mamoe.mirai.message.data.MessageChain
     * @author Moyuyanli
     * @date 2022/8/17 14:23
     */
    public static MessageChain parseMessageParameter(GroupEvent event, String message, Object... object) {
        if (message.contains(ConfigData.INSTANCE.getVariableSymbol() + "message(null)")) {
            return null;
        }
        Pattern pattern = Pattern.compile(DYNAMIC_MESSAGE_PATTERN);
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
            Message messages = null;
            try {
                messages = DynamicMessageUtil.parseMessage((MemberJoinEvent) event, value, valueType, object);
            } catch (IOException e) {
                log.error("转换动态消息出错!", e);
            }
            builder.append(MiraiCode.deserializeMiraiCode(message.substring(index, start)))
                    .append(messages);
            if (ConfigData.INSTANCE.getDebugSwitch()) {
                log.info("动态消息-" + group + "->" + messages);
            }
            index = end;
        }
        if (index < message.length()) {
            builder.append(MiraiCode.deserializeMiraiCode(message.substring(index)));
        }
        return builder.build();
    }


    /**
     * 解析消息中的变量，并识别为 [ MessageChain ]
     * 定时器使用
     *
     * @param message 解析的消息
     * @param object  附加的参数
     * @return net.mamoe.mirai.message.data.MessageChain
     * @author Moyuyanli & forDecember
     * @date 2022/8/17 14:23
     */
    public static MessageChain parseMessageParameter(String message, Object... object) {
        if (message.contains(ConfigData.INSTANCE.getVariableSymbol() + "message(null)")) {
            return null;
        }
        Pattern pattern = Pattern.compile(DYNAMIC_MESSAGE_PATTERN);
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
            Message messages = null;
            try {
                messages = DynamicMessageUtil.parseMessage(value, valueType, object);
            } catch (IOException e) {
                log.error("转换动态消息出错!", e);
            }
            builder.append(MiraiCode.deserializeMiraiCode(message.substring(index, start)))
                    .append(messages);
            if (ConfigData.INSTANCE.getDebugSwitch()) {
                log.info("动态消息-" + group + "->" + messages);
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
     * 供发送消息使用
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
                return new PlainText("未识别动态消息:" + ConfigData.INSTANCE.getVariableSymbol() + valueType + "(" + value + ")");
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
                        return new PlainText("未识别动态消息:" + ConfigData.INSTANCE.getVariableSymbol() + valueType + "(" + value + ")");
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
                        String specialTitle = ((NormalMember) event.getSender()).getSpecialTitle();
                        if (specialTitle != null) {
                            return new PlainText(specialTitle);
                        }
                        //todo 获取群活跃度头衔
                        ((NormalMember) event.getSender()).getSpecialTitle();
                    case "info":
                        return new PlainText((event.getSender()).queryProfile().toString());
                    default:
                        return new PlainText("未识别动态消息:" + ConfigData.INSTANCE.getVariableSymbol() + valueType + "(" + value + ")");
                }
            case "time":
                return getDynamicTimeMessage(value, valueType);
            case "mate":

                // 小D:将正则匹配到的按需求填充到replay里
                assert object[0] instanceof HashMap;
                HashMap<String,String> triggerPatternMap =
                        (HashMap<String, String>) object[0];
                if (triggerPatternMap.containsKey(value)) {
                    String ansFromTrigger = triggerPatternMap.get(value);
                    return new PlainText(ansFromTrigger);
                }

        }

        return new PlainText("未识别动态消息:" + ConfigData.INSTANCE.getVariableSymbol() + valueType + "(" + value + ")");
    }

    /**
     * 识别动态消息 并转换为 [ Message ]
     * 供加群事件使用
     *
     * @param event     消息事件
     * @param value     变量值
     * @param valueType 变量类型
     * @param object    附加值
     * @return net.mamoe.mirai.message.data.Message
     * @author Moyuyanli
     * @date 2022/8/27 23:23
     */
    private static Message parseMessage(MemberJoinEvent event, String value, String valueType, Object... object) throws IOException {
        GroupWelcomeInfo welcomeMessage = (GroupWelcomeInfo) object[0];
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
                            return new PlainText("动态消息无效!-批准人或邀请人");
                        }
                    default:
                        if (Pattern.matches("\\d+", value)) {
                            NormalMember member = event.getGroup().get(Long.parseLong(value));
                            if (member != null) {
                                return new At(member.getId());
                            }
                        }
                        return new PlainText("未识别动态消息:" + ConfigData.INSTANCE.getVariableSymbol() + valueType + "(" + value + ")");
                }
            case "message":
                switch (value) {
                    case "apply":
                        if (applyClusterInfo.getJoinRequestEvent() == null) {
                            return new PlainText("我是被别人领进来的...");
                        }
                        String message = applyClusterInfo.getJoinRequestEvent().getMessage();
                        return new PlainText(message.isEmpty() ? "这个人什么都没说..." : message);
                    default:
                        return new PlainText("未识别动态消息:" + ConfigData.INSTANCE.getVariableSymbol() + valueType + "(" + value + ")");
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
                        return new PlainText("群欢迎词不支持的动态消息:" + ConfigData.INSTANCE.getVariableSymbol() + valueType + "(" + value + ")");
                    default:
                        return new PlainText("未识别动态消息:" + ConfigData.INSTANCE.getVariableSymbol() + valueType + "(" + value + ")");
                }
            case "time":
                return getDynamicTimeMessage(value, valueType);
        }

        return new PlainText("未识别动态消息:" + ConfigData.INSTANCE.getVariableSymbol() + valueType + "(" + value + ")");
    }


    /**
     * 识别动态消息 并转换为 [ Message ]
     * 供加定时任务使用
     *
     * @param value     变量值
     * @param valueType 变量类型
     * @param object    附加值
     * @return net.mamoe.mirai.message.data.Message
     * @author Moyuyanli
     * @date 2022/8/27 23:23
     */
    private static Message parseMessage(String value, String valueType, Object... object) throws IOException {
        Group group = (Group) object[0];
        switch (valueType) {
            //at this qq
            case "at":
                switch (value) {
                    case "this":
                    case "that":
                        return new PlainText("动态消息无效!-定时器不支持");
                    default:
                        if (Pattern.matches("\\d+", value)) {
                            NormalMember member = group.get(Long.parseLong(value));
                            if (member != null) {
                                return new At(member.getId());
                            }
                        }
                        return new PlainText("未识别动态消息:" + ConfigData.INSTANCE.getVariableSymbol() + valueType + "(" + value + ")");
                }
            case "message":
                switch (value) {
                    case "apply":
                        return new PlainText("动态消息无效!-定时器不支持");
                    default:
                        return new PlainText("未识别动态消息:" + ConfigData.INSTANCE.getVariableSymbol() + valueType + "(" + value + ")");
                }
            case "user":
                switch (value) {
                    case "name":
                    case "id":
                    case "avatar":
                    case "title":
                        return new PlainText("动态消息无效!-定时器不支持");
                    default:
                        return new PlainText("未识别动态消息:" + ConfigData.INSTANCE.getVariableSymbol() + valueType + "(" + value + ")");
                }
            case "time":
                return getDynamicTimeMessage(value, valueType);
        }
        return new PlainText("未识别动态消息:" + ConfigData.INSTANCE.getVariableSymbol() + valueType + "(" + value + ")");
    }

    @NotNull
    private static Message getDynamicTimeMessage(String value, String valueType) {
        if ("now".equals(value)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String format = simpleDateFormat.format(new Date());
            return new PlainText(format);
        }
        String userFormat;
        try {
            SimpleDateFormat userSimpleDateFormat = new SimpleDateFormat(value);
            userFormat = userSimpleDateFormat.format(new Date());
        } catch (Exception e) {
            log.warning("动态消息-时间格式化出错!", e);
            return new PlainText("未识别动态消息:" + ConfigData.INSTANCE.getVariableSymbol() + valueType + "(" + value + ")");
        }
        String trim = userFormat.replace("\\", "").trim();
        return new PlainText(trim);
    }

}
