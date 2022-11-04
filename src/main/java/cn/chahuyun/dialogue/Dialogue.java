package cn.chahuyun.dialogue;

import cn.chahuyun.controller.GroupWelcomeInfoAction;
import cn.chahuyun.controller.ManySessionAction;
import cn.chahuyun.entity.*;
import cn.chahuyun.manage.GroupManager;
import cn.chahuyun.utils.DynamicMessageUtil;
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
import net.mamoe.mirai.message.data.SingleMessage;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static cn.chahuyun.HuYanSession.log;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :各种类型的消息对话
 * @Date 2022/7/10 23:31
 */
public class Dialogue {

    public static final Dialogue INSTANCE = new Dialogue();


    private Dialogue() {
    }

    /**
     * 案例
     *
     * @author Moyuyanli
     * @date 2022/9/1 23:38
     */
    private void test(MessageEvent event) {

        for (SingleMessage singleMessage : event.getMessage()) {
            if (singleMessage instanceof Image) {
                byte[] md5 = ((Image) singleMessage).getMd5();
            }
        }

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
     * @param event       消息事件
     * @param sessionInfo 会话消息
     * @author Moyuyanli
     * @date 2022/7/13 21:46
     */
    public void dialogueSession(MessageEvent event, Session sessionInfo) {
        Contact subject = event.getSubject();

        if (sessionInfo.getType() == 5) {
            subject.sendMessage(MessageChain.deserializeFromJsonString(sessionInfo.getReply()));
        } else if (sessionInfo.isDynamic()) {
            MessageChain messages = DynamicMessageUtil.parseMessageParameter(event, sessionInfo.getReply(), sessionInfo,event.getMessage());
            if (messages == null) {
                return;
            }
            subject.sendMessage(messages);
        } else {
            subject.sendMessage(MiraiCode.deserializeMiraiCode(sessionInfo.getReply()));
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

        if (reply.isOther()) {
            subject.sendMessage(MessageChain.deserializeFromJsonString(reply.getReply()));
        } else if (reply.isDynamic()) {
            MessageChain messages = DynamicMessageUtil.parseMessageParameter(event, reply.getReply(), session);
            if (messages == null) {
                return;
            }
            subject.sendMessage(messages);
        } else {
            subject.sendMessage(MiraiCode.deserializeMiraiCode(reply.getReply()));
        }
        ManySessionAction.increase(session);

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
                GroupWelcomeInfoAction.increase(welcomeInfo);
            }
            switch (welcomeMessage.getType()) {
                case 0:
                    subject.sendMessage(MiraiCode.deserializeMiraiCode(welcomeMessage.getWelcomeMessage()));
                    break;
                case 1:
                    MessageChain messages = DynamicMessageUtil.parseMessageParameter(group, welcomeMessage.getWelcomeMessage(), welcomeInfo, GroupManager.map.get(mark));
                    assert messages != null;
                    subject.sendMessage(messages);
                    break;
                case 2:
                    subject.sendMessage(MessageChain.deserializeFromJsonString(welcomeMessage.getWelcomeMessage()));
                    break;
            }

        } catch (EventCancelledException e) {
            log.error("发送消息被取消:", e);
        } catch (BotIsBeingMutedException e) {
            log.error("你的机器人被禁言:", e);
        } catch (MessageTooLargeException e) {
            log.error("发送消息过长:", e);
        } catch (IllegalArgumentException e) {
            log.error("发送消息为空:", e);
        } catch (Exception e) {
            log.error("发送消息错误!!!!:", e);
        } finally {
            GroupManager.map.remove(mark);
        }
    }

}
