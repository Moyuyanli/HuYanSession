package cn.chahuyun.groupManager;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.entity.GroupWelcomeBase;
import cn.chahuyun.entity.ScopeInfoBase;
import cn.chahuyun.files.GroupData;
import cn.chahuyun.files.PluginData;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.MiraiLogger;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GroupManager
 *
 * @author Zhangjiaxing
 * @description 群管指令类
 * @date 2022/6/21 9:28
 */
public class GroupManager {

    public static final GroupManager INSTANCE = new GroupManager();
    private MiraiLogger l = HuYanSession.INSTANCE.getLogger();

    /**
     * 添加删除迎新词正则
     */
    private final String setMessagePattern = "[+-]hyc[:：](\\S+)( \\S+){0,2}";

    private final String prohibitPattern = "(\\[mirai:at:\\d+\\] \\d+[s|d|h|m])";

    /**
     * @description 添加或删除新成员欢迎词
     * @author zhangjiaxing
     * @param event 消息事件
     * @date 2022/6/21 9:41
     */
    public void setGroupWelcomeMessage(MessageEvent event) {

        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        //正则匹配
        Pattern pattern = Pattern.compile(setMessagePattern);
        Matcher matcher = pattern.matcher(code);
        matcher.find();
        //获取匹配内容
        String group = matcher.group();
        //分割
        String[] strings = group.split(" ");
        boolean aod = strings[0].split("[:：]")[0].startsWith("+");
        String key = strings[0].split("[:：]")[1];
        String value = null;
        if (aod) {
            value = strings[1];
        }
        ScopeInfoBase base = new ScopeInfoBase("当前", true,false, subject.getId(), 0);
        if (strings.length >= 3) {
            if (Pattern.matches("全局|gr\\d+", strings[2])) {
                if (strings[2].equals("全局")) {
                    base = new ScopeInfoBase("全局", false, false, subject.getId(), 0);
                } else {
                    int groupNum = Integer.parseInt(strings[2].substring(2));
                    boolean containsKey = GroupData.INSTANCE.getGroupList().containsKey(groupNum);
                    if (!containsKey) {
                        event.getSubject().sendMessage("没有该群组信息，请检查群组!");
                        return;
                    }
                    base = new ScopeInfoBase("群组", false, true, subject.getId(), groupNum);
                }
            } else {
                subject.sendMessage("参数错误，请查看后使用！");
                return;
            }
        }
        MessageChain messages = PluginData.INSTANCE.setGroupWelcomeMessage(aod,key,value,base);
        subject.sendMessage(messages);
    }


    /**
     * @description 查询所有欢迎词
     * @author zhangjiaxing
     * @param event 消息事件
     * @date 2022/6/21 11:15
     */
    public void checkGroupWelcomeMessage(MessageEvent event) {
        Contact subject = event.getSubject();
        //构造转发消息
        ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder(subject);

        MessageChain fastMessages = new MessageChainBuilder().append("以下为查询到的所有的迎新词↓").build();
        //查询所有欢迎词，然后遍历添加，以miraicode码
        ArrayList<GroupWelcomeBase> groupWelcomeMessage = (ArrayList<GroupWelcomeBase>) PluginData.INSTANCE.getGroupWelcomeMessage(event.getSubject().getId());
        MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
        for (GroupWelcomeBase base : groupWelcomeMessage) {
            MessageChain messages = MiraiCode.deserializeMiraiCode(base.getValue());
            messageChainBuilder.append("标识”")
                    .append(base.getKey())
                    .append("”:")
                    .append(messages)
                    .append("\n");
        }
        //构造消息
        MessageChain messageChain = messageChainBuilder.build();
        ForwardMessage message = forwardMessageBuilder.add(event.getBot(), fastMessages)
                .add(event.getBot(), messageChain)
                .build();
        subject.sendMessage(message);
    }

    /**
     * @description 解禁言
     * @author zhangjiaxing
     * @param event 消息事件
     * @date 2022/6/21 16:44
     */
    public void prohibit(MessageEvent event) {
        Contact subject = event.getSubject();
        String code = event.getMessage().serializeToMiraiCode();
        //检测格式
        if (!Pattern.matches(prohibitPattern, code)) {
            subject.sendMessage("禁言失败，格式错误！");
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
        NormalMember member = event.getBot().getGroup(event.getSubject().getId()).get(qq);
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
            subject.sendMessage("禁言失败,你机器居然不是管理员???");
        }
        subject.sendMessage(messages.build());
    }


    public void kick(MessageEvent event) {
        Contact subject = event.getSubject();
        MessageChain message = event.getMessage();

        Long qq = null;
        for (SingleMessage singleMessage : message) {
            if (singleMessage instanceof At) {
                qq =  ((At) singleMessage).getTarget();
            }
        }
        if (qq == null) {
            subject.sendMessage("踢出失败，没有这个人呢！");
            return;
        }
        NormalMember member = event.getBot().getGroup(event.getSubject().getId()).get(qq);

        member.kick("踢出");

    }


}