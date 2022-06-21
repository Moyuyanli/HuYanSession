package cn.chahuyun.groupManager;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.data.SessionData;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.ForwardMessage;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.utils.MiraiLogger;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GroupManager
 *
 * @author Zhangjiaxing
 * @description 群管指令类
 * @date 2022/6/21 9:28
 */
public class GroupManager {

    public static final GroupManager INSTANCE = new GroupManager();

    private MiraiLogger l = HuYanSession.INSTANCE.getLogger();

    private final String addMessagePattern = "([\\+\\-][\\d\\w\\u4e00-\\u9fa5]+[:：][\\d\\w\\S\\u4e00-\\u9fa5]+)";


    /**
     * @description 添加或删除新成员欢迎词
     * @author zhangjiaxing
     * @param event 消息事件
     * @date 2022/6/21 9:41
     * @return void
     */
    public void setGroupWelcomeMessage(MessageEvent event) {

        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        //正则匹配
        Pattern pattern = Pattern.compile(addMessagePattern);
        Matcher matcher = pattern.matcher(code);
        matcher.find();
        //获取匹配内容
        String group = matcher.group();
        //分割
        String[] strings = group.split("[:：]");
        String s = strings[0];
        String string = null;
        if (strings.length == 2) {
            string = strings[1];
        }
        //跟miraicode冲突了 T_T
        if (strings.length == 4) {
            string = strings[1] + ":" + strings[2]+":"+strings[3];
        }
        MessageChain messages = SessionData.INSTANCE.setGroupWelcomeMessage(s, string);

        subject.sendMessage(messages);
    }


    /**
     * @description 查询所有欢迎词
     * @author zhangjiaxing
     * @param event 消息事件
     * @date 2022/6/21 11:15
     * @return void
     */
    public void checkGroupWelcomeMessage(MessageEvent event) {
        Contact subject = event.getSubject();
        //构造转发消息
        ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder(subject);

        MessageChain fastMessages = new MessageChainBuilder().append("以下为查询到的所有的迎新测↓").build();
        //查询所有欢迎词，然后遍历添加，以miraicode码
        Map<String, String> groupWelcomeMessage = SessionData.INSTANCE.getGroupWelcomeMessage();
        MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
        for (String s : groupWelcomeMessage.keySet()) {
            MessageChain messages = MiraiCode.deserializeMiraiCode(groupWelcomeMessage.get(s));
            messageChainBuilder.append("标识”")
                    .append(s)
                    .append("”:")
                    .append(messages)
                    .append("\n");
        }
        //构造消息
        MessageChain messageChain = messageChainBuilder.build();
        ForwardMessage message = forwardMessageBuilder.add(event.getBot(), fastMessages)
                .add(event.getBot(), messageChain)
                .build();
        subject.sendMessage(message);
    }


}