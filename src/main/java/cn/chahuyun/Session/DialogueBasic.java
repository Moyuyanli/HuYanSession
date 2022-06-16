package cn.chahuyun.Session;

import cn.chahuyun.Session.Criticaldialog.PuDialogue;
import cn.chahuyun.Session.Criticaldialog.SessionDialogue;
import cn.chahuyun.enumerate.MessEnum;
import cn.chahuyun.file.SessionData;
import cn.chahuyun.file.SessionDataBase;
import lombok.Data;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.ArrayList;
import java.util.regex.Pattern;


/**
 * 基本对话事件
 *
 * @author Zhangjiaxing
 * @description 触发基本对话
 * @date 2022/6/8 9:18
 */
@Data
public class DialogueBasic {

    //回复消息正则
    public static String dialoguePattern = "噗~";

    /**
     * @description 所有消息的入口
     * @author zhangjiaxing
     * @param event 消息监视器
     * @date 2022/6/16 15:25
     * @return void
     */
    public static void isMessageWhereabouts(MessageEvent event) {
        //判断消息传递类型
        MessEnum messEnum = null;
        //获取到的消息
        String messageToString = event.getMessage().contentToString();

        /*
        判断是否是对话类消息
         */
        //获取对话数据
        ArrayList<SessionDataBase> sessionPattern = SessionData.INSTANCE.getSession();
        //创建触发对话结果
        SessionDataBase sessionDataBase = null;
        //循环判断
        for (SessionDataBase base : sessionPattern) {
            //TypeInt = 1 -> 精准匹配
            if (base.getDataEnum().getTypeInt() == 1) {
                if (base.getKey().equals(messageToString)) {
                    messEnum = MessEnum.SESSION;
                    sessionDataBase = base;
                }
            }
        }


        /*
        判断是否是回复消息
         */
        if (Pattern.matches(dialoguePattern,messageToString)) messEnum = MessEnum.REPLY;



        /*
        判断是否触发消息回复
         */
        if (messEnum == null) {
            return;
        }

        //分支
        switch (messEnum.getMessageTypeInt()) {
            //SESSION("会话消息",1)
            case 1:
                SessionDialogue.session(event,sessionDataBase);
                messEnum = null;
                break;
            //COMMAND("指令消息",2)
            case 2:
                messEnum = null;
                break;
            //REPLY("回复消息",3)
            case 3:
                switch (messageToString){
                    case "噗~":
                        PuDialogue.sessionPu(event);
                        break;
                }
                messEnum = null;
                break;
            default:
                messEnum = null;
                break;
        }
    }

}