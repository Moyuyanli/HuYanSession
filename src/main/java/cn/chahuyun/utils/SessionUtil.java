package cn.chahuyun.utils;

import cn.chahuyun.entity.Scope;
import cn.chahuyun.enums.Mate;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.regex.Pattern;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :对话消息工具类
 * @Date 2022/7/9 17:14
 */
public class SessionUtil {

    public static void studySession(MessageEvent event) {
        //xx a b [p1] [p2]
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();


        String[] split = code.split("\\s+");
        String key = split[1];
        String value = split[2];

        Mate mate = Mate.ACCURATE;
        Scope scope = new Scope("当前", false, false, subject.getId(), 0);

        //最小分割大小
        int minIndex = 3;
        //大于这个大小就进行参数判断
        if (split.length > minIndex) {
            for (int i = minIndex; i < split.length; i++) {
                String s = split[i];
                switch (s) {
                    case "模糊":
                        mate = Mate.VAGUE;
                        break;
                    case "头部":
                        mate = Mate.START;
                        break;
                    case "结尾":
                        mate = Mate.END;
                        break;
                    case "全局":
                        scope = new Scope("全局", true, false, subject.getId(), 0);
                        break;
                    default:
                        String listPattern = "gr\\d+";
                        if (Pattern.matches(listPattern, s)) {
                            int listId = Integer.parseInt(s.substring(2));
                            scope = new Scope("群组", false, true, subject.getId(), listId);
                        }
                        break;
                }
            }
        } else {
            if (subject instanceof User) {
                subject.sendMessage("私发学习请输入作用域！");
                return;
            }
        }

        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append("INSERT INTO session(");


    }




}
