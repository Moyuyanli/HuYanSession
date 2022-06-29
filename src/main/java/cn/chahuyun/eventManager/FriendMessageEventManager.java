package cn.chahuyun.eventManager;

import cn.chahuyun.HuYanSession;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.utils.MiraiLogger;

/**
 * FriendMessageEventManager
 *
 * @author Zhangjiaxing
 * @description 好友消息处理
 * @date 2022/6/28 17:31
 */
public class FriendMessageEventManager {

    public static final FriendMessageEventManager INSTANCE = new FriendMessageEventManager();
    private MiraiLogger l = HuYanSession.INSTANCE.getLogger();

    /**
     * @description 接收到所有好友发送的消息处理
     * @author zhangjiaxing
     * @param event 消息事件
     * @date 2022/6/29 8:40
     */
    public void isMessageType(MessageEvent event) {
        Contact subject = event.getSubject();
        String code = event.getMessage().serializeToMiraiCode();






    }



}