package cn.chahuyun.dialogue;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.entity.*;
import cn.chahuyun.manage.GroupManager;
import cn.chahuyun.utils.GroupWelcomeInfoUtil;
import cn.chahuyun.utils.ManySessionUtil;
import cn.chahuyun.utils.ShareUtils;
import net.mamoe.mirai.contact.BotIsBeingMutedException;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.MessageTooLargeException;
import net.mamoe.mirai.event.events.EventCancelledException;
import net.mamoe.mirai.event.events.MemberJoinEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.utils.MiraiLogger;

import java.io.IOException;
import java.net.URL;
import java.util.List;

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
        Image image;
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
                subject.sendMessage(MiraiCode.deserializeMiraiCode(session.getReply()));
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

    /**
     * 多词条消息
     *
     * @param event   消息事件
     * @param session 多词条消息
     * @author Moyuyanli
     * @date 2022/7/13 21:46
     */
    public void dialogueSession(MessageEvent event, ManySessionInfo session) {
        Contact subject = event.getSubject();
        ManySession reply;
        List<ManySession> manySessions = session.getManySessions();
        int size = manySessions.size();
        if (session.isRandom()) {
            reply = manySessions.get((int) (Math.random() * size));
        } else {
            int pollingNumber = session.getPollingNumber();
            reply = manySessions.get(pollingNumber < size ? pollingNumber : pollingNumber % size);
        }
        try {
            if (reply.isOther()) {
                subject.sendMessage(MessageChain.deserializeFromJsonString(reply.getReply()));
            } else if (reply.isDynamic()) {
                MessageChain messages = ShareUtils.parseMessageParameter(event, reply.getReply(), session);
                if (messages == null) {
                    return;
                }
                subject.sendMessage(messages);
            } else {
                subject.sendMessage(MiraiCode.deserializeMiraiCode(reply.getReply()));
            }
            ManySessionUtil.increase(session);
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


    /**
     * 群欢迎词消息
     *
     * @param group       群事件
     * @param welcomeInfo 欢迎消息
     * @author Moyuyanli
     * @date 2022/7/13 21:46
     */
    public void dialogueSession(MemberJoinEvent group, GroupWelcomeInfo welcomeInfo) {
        Group subject = group.getGroup();
        String mark = group.getGroup().getId() + "." + group.getMember().getId();
        try {
            List<WelcomeMessage> welcomeMessages = welcomeInfo.getWelcomeMessages();
            WelcomeMessage welcomeMessage;
            //随机
            int size = welcomeMessages.size();
            if (welcomeInfo.isRandom()) {
                int index = (int) (Math.random() * size);
                welcomeMessage = welcomeMessages.get(index);
            } else {
                int pollingNumber = welcomeInfo.getPollingNumber();
                //获取轮询的结尾
                welcomeMessage = welcomeMessages.get(pollingNumber < size ? pollingNumber : pollingNumber % size);
                GroupWelcomeInfoUtil.increase(welcomeInfo);
            }
            switch (welcomeMessage.getType()) {
                case 0:
                    subject.sendMessage(MiraiCode.deserializeMiraiCode(welcomeMessage.getWelcomeMessage()));
                    break;
                case 1:
                    MessageChain messages = ShareUtils.parseMessageParameter(group, welcomeMessage.getWelcomeMessage(), welcomeInfo, GroupManager.map.get(mark));
                    subject.sendMessage(messages);
                    break;
                case 2:
                    subject.sendMessage(MessageChain.deserializeFromJsonString(welcomeMessage.getWelcomeMessage()));
                    break;
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
        } finally {
            GroupManager.map.remove(mark);
        }
    }

}
