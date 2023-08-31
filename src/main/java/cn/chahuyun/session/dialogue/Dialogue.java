package cn.chahuyun.session.dialogue;

import cn.chahuyun.session.entity.BaseMessage;
import net.mamoe.mirai.event.events.MessageEvent;

/**
 * 对话接口
 *
 * @author Moyuyanli
 * @date 2023/8/31 9:27
 */
public interface Dialogue {

    /**
     * 发送消息<p/>
     * 通过对其子类的类型判断实现消息发送
     *
     * @author Moyuyanli
     * @date 2023/8/31 9:39
     */
    void sendMessage();

}
