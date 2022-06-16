package cn.chahuyun.Session;

import cn.chahuyun.GroupSession;
import cn.chahuyun.Session.Criticaldialog.PuDialogue;
import cn.chahuyun.Session.Criticaldialog.SessionDialogue;
import cn.chahuyun.data.SessionDataBase;
import cn.chahuyun.enumerate.MessEnum;
import lombok.Data;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.utils.MiraiLogger;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.chahuyun.GroupSession.sessionData;


/**
 * 基本对话事件
 *
 * @author Zhangjiaxing
 * @description 触发基本对话
 * @date 2022/6/8 9:18
 */
@Data
public class DialogueBasic {

    private static MiraiLogger l = GroupSession.INSTANCE.getLogger();

    /**
     * 指令正则
     */
    public static String commandPattern = "学习|修改|查询|删除";
    /**
     * 回复消息正则
     */
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
        ArrayList<SessionDataBase> sessionPattern = (ArrayList<SessionDataBase>) sessionData.getSession();
        //创建触发对话结果
        SessionDataBase sessionDataBase = null;
        //循环判断
        for (SessionDataBase base : sessionPattern) {
            //TypeInt = 1 -> 精准匹配
            if (base.getDataEnum().getTypeInt() == 1) {
                if (base.getKey().equals(messageToString)) {
                    l.info("匹配对话成功 "+base.getKey()+" -> "+base.getValue());
                    messEnum = MessEnum.SESSION;
                    sessionDataBase = base;
                }
            }
        }

        /*
        判断是否是指令消息
         */
        Matcher matcher = Pattern.compile(commandPattern).matcher(messageToString);
        if (matcher.find() && matcher.end()==2) messEnum = MessEnum.COMMAND;


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
                if (messageToString.indexOf("学习") == 0) {
                    l.info("学习指令");
                    SessionManage.studySession(event);
                } else if (messageToString.indexOf("查询") == 0) {
                    l.info("查询指令");
                    SessionManage.querySession(event);
                }
                messEnum = null;
                break;
            //REPLY("回复消息",3)
            case 3:
                switch (messageToString){
                    case "噗~":
                        PuDialogue.sessionPu(event);
                        break;
                    default:
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