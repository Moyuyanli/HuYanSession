package cn.chahuyun.session.dialogue;

import cn.chahuyun.session.entity.*;
import net.mamoe.mirai.event.events.MessageEvent;

/**
 * 消息处理场<p/>
 * 所有消息发送均发送到这里，这里对消息进行分类
 *
 * @author Moyuyanli
 * @date 2023/8/31 9:44
 */
public class DialogueProcessing {

    private static final DialogueProcessing INSTANCE = new DialogueProcessing();

    /**
     * 多词条消息对话
     */
    private final ManyDialogue manyDialogue;
    /**
     * 会话对话
     */
    private final SessionDialogue sessionDialogue;
    /**
     * 欢迎词对话
     */
    private final WelcomeDialogue welcomeDialogue;


    private DialogueProcessing() {
        this.manyDialogue = new ManyDialogue();
        this.sessionDialogue = new SessionDialogue();
        this.welcomeDialogue = new WelcomeDialogue();
    }

    /**
     * 获取处理工具
     *
     * @return INSTANCE
     */
    public static DialogueProcessing getInstance() {
        return INSTANCE;
    }

    /**
     * 处理并发送消息
     *
     * @param event   消息事件
     * @param message 消息信息
     */
    public void dialogue(MessageEvent event, BaseMessage message) {
        if (message instanceof Session) {
            Session session = (Session) message;
            sessionDialogue.setSession(session);
            sessionDialogue.setEvent(event);
            sessionDialogue.sendMessage();
        } else if (message instanceof ManySessionInfo) {
            ManySessionInfo manySessionInfo = (ManySessionInfo) message;
            manyDialogue.setManySession(manySessionInfo);
            manyDialogue.setEvent(event);
            manyDialogue.sendMessage();
        } else if (message instanceof GroupWelcomeInfo) {
            GroupWelcomeInfo welcomeMessage = (GroupWelcomeInfo) message;
            welcomeDialogue.setWelcomeMessage(welcomeMessage);
            welcomeDialogue.setEvent(event);
            welcomeDialogue.sendMessage();
        }
    }




}
