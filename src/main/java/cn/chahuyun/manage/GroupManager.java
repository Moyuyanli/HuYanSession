package cn.chahuyun.manage;

import cn.chahuyun.HuYanSession;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.SingleMessage;
import net.mamoe.mirai.utils.MiraiLogger;

import java.util.regex.Pattern;

/**
 * GroupManager
 * 群管理类
 *
 * @author Zhangjiaxing
 * @date 2022/8/15 12:52
 */
public class GroupManager {


    private final static MiraiLogger l = HuYanSession.INSTANCE.getLogger();


    /**
     * @description 解禁言
     * @author zhangjiaxing
     * @param event 消息事件
     * @date 2022/6/21 16:44
     */
    public static void prohibit(MessageEvent event) {
        Contact subject = event.getSubject();
        String code = event.getMessage().serializeToMiraiCode();
        Bot bot = event.getBot();
        //判断bot的权限
        Group group = (Group) subject;
        boolean botIsAdmin = group.getBotPermission() == MemberPermission.MEMBER;
        if (botIsAdmin) {
            subject.sendMessage("人家还不是管理员哦~");
            return;
        }

        //获取群友对象
        Long qq = null;
        for(SingleMessage s:event.getMessage()){
            if(s instanceof At){
                qq = ((At) s).getTarget();
            }
        }
        if (qq == null) {
            subject.sendMessage("禁言失败，没有这个人呢！");
            return;
        }

        NormalMember member = bot.getGroup(event.getSubject().getId()).get(qq);
        //获取参数
        String[] split = code.split(" ");
        String param = split[1];
        //分解参数
        String type = param.substring(param.length() - 1);
        int timeParam = Integer.parseInt(param.substring(0, param.length() - 1));
        if (timeParam == 0) {
            assert member != null;
            member.unmute();
            subject.sendMessage("解禁成功！");
            return;
        }
        //禁言时间计算
        int time = 0;
        MessageChainBuilder messages = new MessageChainBuilder().append("禁言成功!");
        switch (type) {
            case "s":
                time = timeParam;
                messages.append("禁言:"+timeParam+"秒");
                break;
            case "m":
                time = timeParam*60;
                messages.append("禁言:"+timeParam+"分钟");
                break;
            case "h":
                time = timeParam*60*60;
                messages.append("禁言:"+timeParam+"小时");
                break;
            case "d":
                time = timeParam*60*60*24;
                messages.append("禁言:"+timeParam+"天");
                break;
            default:break;
        }
        //禁言
        assert member != null;
        try {
            member.mute(time);
        } catch (Exception e) {
            if (e instanceof PermissionDeniedException) {
                subject.sendMessage("禁言失败,你机器居然不是管理员???");
            } else {
                subject.sendMessage("出错啦~");
                l.error("出错啦~",e);
            }
        }
        subject.sendMessage(messages.build());
    }
}