package cn.chahuyun.Session;

import cn.chahuyun.GroupSession;
import cn.chahuyun.enumerate.DataEnum;
import cn.chahuyun.file.SessionData;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.utils.MiraiLogger;

import java.util.regex.Pattern;

import static cn.chahuyun.GroupSession.sessionData;

/**
 * 对话信息检测
 *
 * @author Zhangjiaxing
 * @description 检测对话是否合格
 * @date 2022/6/8 9:34
 */
public class SessionManage {

    private static MiraiLogger l = GroupSession.INSTANCE.getLogger();

    public static String studyPattern = "(学习 [\\u4e00-\\u9fa5]{1,} [\\u4e00-\\u9fa5]{1,}( ?(精准|模糊|头部|结尾))?)";

    public static void SessionDisclose(Event event) {

    }

    /**
     * @description 判断该消息是不是规定字符
     * @author zhangjiaxing
     * @param messageChain 消息链
     * @date 2022/6/8 12:32
     * @return boolean
     */
    public static boolean isString(MessageChain messageChain) {
        return false;
    }


    public static boolean studySession(MessageEvent event) {
        String messageString = event.getMessage().contentToString();
        Contact subject = event.getSubject();
        //判断学习语法结构是否正确
        if (!Pattern.matches(studyPattern, messageString)) {
            subject.sendMessage("学习失败！学习结构应为：");
            subject.sendMessage("学习 (触发关键词) (回复内容) [精准|模糊|头部|结尾]");
            return false;
        }

        String[] strings = messageString.split(" ");
        l.info(strings.toString());
        if (strings.length == 3 && !strings[2].equals("图片")) {
            //type = 0 为string类回复
            sessionData.add(strings[1],0,strings[2],null, DataEnum.ACCURATE);
            subject.sendMessage("学习成功！ " + strings[1]);
            return true;
        }


        return false;
    }


}