package cn.chahuyun.Session.Criticaldialog;

import cn.chahuyun.GroupSession;
import net.mamoe.mirai.event.events.MessageEvent;

/**
 * PuDialogue
 *
 * @author Zhangjiaxing
 * @description
 * @date 2022/6/16 10:23
 */
public class PuDialogue {

    /**
     * @description 噗~ -> 噗~
     * @author zhangjiaxing
     * @param messageEvent 消息事件
     * @date 2022/6/16 14:20
     */
    public static void sessionPu(MessageEvent messageEvent) {
        try {
            messageEvent.getSubject().sendMessage("噗~");
        } catch (Exception e) {
            GroupSession.INSTANCE.getLogger().error(e.getMessage());
        }
    }

}