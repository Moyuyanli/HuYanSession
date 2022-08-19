package cn.chahuyun.dialogue;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.entity.Session;
import cn.chahuyun.utils.ShareUtils;
import net.mamoe.mirai.contact.BotIsBeingMutedException;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.MessageTooLargeException;
import net.mamoe.mirai.event.events.EventCancelledException;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.utils.MiraiLogger;

import java.io.IOException;
import java.net.URL;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :各种类型的消息对话
 * @Date 2022/7/10 23:31
 */
public class Dialogue {

    public static final Dialogue INSTANCE = new Dialogue();
    private final static MiraiLogger l = HuYanSession.INSTANCE.getLogger();


    private Dialogue() {
    }

    private void test(MessageEvent event) {
        Image image ;
        try {
            image = Contact.uploadImage(event.getSubject(), new URL("").openStream());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        event.getSubject().sendMessage(image);
    }

    /**
     * 会话消息
     *
     * @param event   消息事件
     * @param session 会话消息
     * @author Moyuyanli
     * @date 2022/7/13 21:46
     */
    public void dialogueSession(MessageEvent event, Session session) {
        Contact subject = event.getSubject();
        try {
            if (session.getType() == 5) {
                subject.sendMessage(MessageChain.deserializeFromJsonString(session.getReply()));
            } else if (session.isDynamic()) {
                MessageChain messages = ShareUtils.parseMessageParameter(event, session.getReply(), session);
                if (messages == null) {
                    return;
                }
                subject.sendMessage(messages);
            } else {
                subject.sendMessage(session.getReply());
            }
        } catch (EventCancelledException e) {
            l.error("发送消息被取消:" + e.getMessage());
            e.printStackTrace();
        } catch (BotIsBeingMutedException e) {
            l.error("你的机器人被禁言:" + e.getMessage());
            e.printStackTrace();
        } catch (MessageTooLargeException e) {
            l.error("发送消息过长:" + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            l.error("发送消息为空:" + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            l.error("发送消息错误!!!!:" + e.getMessage());
            e.printStackTrace();
        }
    }

}
