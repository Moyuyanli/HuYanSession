package cn.chahuyun.session.dialogue;

import cn.chahuyun.session.HuYanSession;
import cn.chahuyun.session.entity.BaseMessage;
import net.mamoe.mirai.event.events.MessageEvent;

/**
 * 抽象对话类
 *
 * @author Moyuyanli
 * @date 2023/8/31 9:41
 */
public class AbstractDialogue implements Dialogue{

    /**
     * 消息事件
     */
    private MessageEvent event;

    /**
     * 发送消息<p/>
     * 通过对其子类的类型判断实现消息发送
     *
     * @author Moyuyanli
     * @date 2023/8/31 9:39
     */
    @Override
    public void sendMessage() {
        HuYanSession.LOGGER.warning("请实现消息发送！");
    }


    public MessageEvent getEvent() {
        return event;
    }

    public void setEvent(MessageEvent event) {
        this.event = event;
    }

}
