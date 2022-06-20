package cn.chahuyun.data;


import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.util.ArrayList;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :会话分页显示
 * @Date 2022/6/16 21:31
 */
public class SessionDataPaging {

    public static final SessionDataPaging INSTANCE = new SessionDataPaging();

    /**
     * @description 查询所有消息，并且分类
     * @author zhangjiaxing
     * @param event 消息事件
     * @date 2022/6/20 13:25
     * @return boolean
     */
    public boolean checkSessionList(MessageEvent event) {
        //创建转发消息构造
        ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder(event.getSubject());
        //获取机器人
        Bot bot = event.getBot();
        //创建消息分类构造
        MessageChainBuilder table = new MessageChainBuilder();
        MessageChainBuilder accurate = new MessageChainBuilder();
        MessageChainBuilder vague = new MessageChainBuilder();
        MessageChainBuilder start = new MessageChainBuilder();
        MessageChainBuilder end = new MessageChainBuilder();
        MessageChainBuilder other = new MessageChainBuilder();
        table.append("以下为所有查询到的触发关键词结果↓");
        forwardMessageBuilder.add(bot, table.build());
        accurate.append("所有的精准匹配触发消息:\n");
        vague.append("所有的模糊匹配触发消息:\n");
        start.append("所有的头部匹配触发消息:\n");
        end.append("所有的结尾匹配触发消息:\n");
        other.append("所有的其他匹配触发消息:\n");
        //获取全部消息
        ArrayList<SessionDataBase> values = new ArrayList<>(SessionData.INSTANCE.getSessionMap().values()) ;
        for (SessionDataBase base : values) {
            //判断触发类别
            String trigger = "全局触发";
            long groupId = event.getSubject().getId();
            if (base.getScopeInfo().getType() && base.getScopeInfo().getScopeCode() == groupId) {
                trigger = "当前群触发";
            } else if (base.getScopeInfo().getType()) {
                trigger = "其他群触发";
            }
            //判断消息类别
            if (base.getType() == 0) {
                //判断匹配机制
                switch (base.getDataEnum()) {
                    case ACCURATE:
                        accurate.append(base.getKey() + " ==> " + base.getValue() + " -> " + trigger + "\n");
                        break;
                    case VAGUE:
                        vague.append(base.getKey() + " ==> " + base.getValue() + " -> " + trigger + "\n");
                        break;
                    case START:
                        start.append(base.getKey() + " ==> " + base.getValue() + " -> " + trigger + "\n");
                        break;
                    case END:
                        end.append(base.getKey() + " ==> " + base.getValue() + " -> " + trigger + "\n");
                        break;
                    default:
                        break;
                }
            } else {
                other.append(MiraiCode.deserializeMiraiCode(base.getKey()))
                        .append(" ==> ")
                        .append(MiraiCode.deserializeMiraiCode(base.getValue()))
                        .append(" -> ")
                        .append(trigger)
                        .append(":")
                        .append(base.getDataEnum().getType())
                        .append("\n");
            }
        }
        forwardMessageBuilder.add(bot, accurate.build());
        forwardMessageBuilder.add(bot, vague.build());
        forwardMessageBuilder.add(bot, start.build());
        forwardMessageBuilder.add(bot, end.build());
        forwardMessageBuilder.add(bot, other.build());
        event.getSubject().sendMessage(forwardMessageBuilder.build());
        return true;
    }

}
