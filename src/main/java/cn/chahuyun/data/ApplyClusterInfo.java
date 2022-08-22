package cn.chahuyun.data;

import net.mamoe.mirai.event.events.MemberJoinEvent;
import net.mamoe.mirai.event.events.MemberJoinRequestEvent;
import net.mamoe.mirai.event.events.MessageEvent;

/**
 * ApplyClusterInfo
 * 申请加群信息
 *
 * @author Moyuyanli
 * @date 2022/8/22 17:01
 */
public class ApplyClusterInfo {

    private MemberJoinRequestEvent joinRequestEvent;

    private MemberJoinEvent joinEvent;

    private MessageEvent messageEvent;

    public ApplyClusterInfo() {
    }

    public MemberJoinRequestEvent getJoinRequestEvent() {
        return joinRequestEvent;
    }

    public void setJoinRequestEvent(MemberJoinRequestEvent joinRequestEvent) {
        this.joinRequestEvent = joinRequestEvent;
    }

    public MemberJoinEvent getJoinEvent() {
        return joinEvent;
    }

    public void setJoinEvent(MemberJoinEvent joinEvent) {
        this.joinEvent = joinEvent;
    }

    public MessageEvent getMessageEvent() {
        return messageEvent;
    }

    public void setMessageEvent(MessageEvent messageEvent) {
        this.messageEvent = messageEvent;
    }
}