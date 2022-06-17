package cn.chahuyun.Session.Criticaldialog;

import cn.chahuyun.data.SessionDataBase;
import cn.chahuyun.file.SessionData;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.code.MiraiCode;

import java.util.ArrayList;

import static cn.chahuyun.GroupSession.sessionData;

/**
 * SessionDialogue
 *
 * @author Zhangjiaxing
 * @description 关键词对话的消息触发
 * @date 2022/6/16 14:36
 */
public class SessionDialogue {

    /**
     * @description 传递消息监视和指定的关键词对话
     * @author zhangjiaxing
     * @param messageEvent 消息事件
     * @param sessionDataBase 对话类
     * @date 2022/6/16 15:17
     */
    public static void session(MessageEvent messageEvent,SessionDataBase sessionDataBase) {
        ArrayList<SessionDataBase> session = new ArrayList<>(sessionData.values());
        //type = 0 为string类回复
        if (sessionDataBase.getType() == 0) {
            messageEvent.getSubject().sendMessage(MiraiCode.deserializeMiraiCode(sessionDataBase.getValue()));
        }

    }

}