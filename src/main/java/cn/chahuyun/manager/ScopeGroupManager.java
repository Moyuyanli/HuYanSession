package cn.chahuyun.manager;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.files.GroupData;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.utils.MiraiLogger;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :群组指令管理
 * @Date 2022/6/30 19:27
 */
public class ScopeGroupManager {

    public static final ScopeGroupManager INSTANCE = new ScopeGroupManager();
    private final MiraiLogger l = HuYanSession.INSTANCE.getLogger();


    /**
     * 添加群组
     * @author zhangjiaxing
     * @param event 消息事件
     * @date 2022/7/1 9:05
     */
    public void addScopeGroup(MessageEvent event) {
        Contact subject = event.getSubject();
        String code = event.getMessage().contentToString();
        MessageChain messages = null;

        String[] split = code.split(" ");
        //判断参数数量是否符合规则
        if (split.length <= 1) {
            subject.sendMessage("添加群组格式错误！eg：+gr:(群组编号) (群号1) ...");
            return;
        }
        //下标
        int index = Integer.parseInt(split[0].split("[:：]")[1]);
        if (index == 0) {
            subject.sendMessage("群组0默认为全局，不可修改！");
            return;
        }
        //如果多个参数，循环添加
        for (int i = 1; i < split.length; i++) {
            long group = Long.parseLong(split[i]);
            messages = GroupData.INSTANCE.addGroupList(index, group);
        }
        //多个参数的回复词
        if (split.length > 2) {
            messages = new MessageChainBuilder().append("添加群组").append(String.valueOf(index)).append("多个群号成功!").build();
        }
        subject.sendMessage(messages);
    }

    /**
     * 删除群组
     * @author zhangjiaxing
     * @param event 消息事件
     * @date 2022/7/1 9:11
     */
    public void delScopeGroup(MessageEvent event) {
        Contact subject = event.getSubject();
        String code = event.getMessage().serializeToMiraiCode();
        MessageChain messages = null;

        String[] split = code.split(" ");
        //下标
        int index = Integer.parseInt(split[0].split("[:：]")[1]);
        if (index == 0) {
            subject.sendMessage("群组0默认为全局，不可删除！");
            return;
        }
        //如果参数为2，代表删除群组，如果大于2代表删除群组内的群号
        if (split.length == 1) {
            messages = GroupData.INSTANCE.delGroupList(index, -1L);
        }else {
            for (int i = 1; i < split.length; i++) {
                long group = Long.parseLong(split[i]);
                messages = GroupData.INSTANCE.delGroupList(index, group);
            }
        }
        //多个参数的回复词
        if (split.length >= 3) {
            messages = new MessageChainBuilder().append("删除群组").append(String.valueOf(index)).append("多个群号").append("成功!").build();
        }
        subject.sendMessage(messages);
    }

    /**
     * 查看所有群组信息
     * @author zhangjiaxing
     * @param event 消息事件
     * @date 2022/7/1 9:35
     */
    public void checkScopeGroup(MessageEvent event) {
        Contact subject = event.getSubject();
        Bot bot = event.getBot();
        //构造转发消息
        ForwardMessageBuilder FMB = new ForwardMessageBuilder(subject);
        FMB.add(bot, new MessageChainBuilder().append("以下是所有群组信息↓").build());
        //获取群组列表
        Map<Integer, List<Long>> groupList = GroupData.INSTANCE.getGroupList();
        //遍历实现群组信息添加
        for (Map.Entry<Integer, List<Long>> entry : groupList.entrySet()) {
            MessageChainBuilder messages = new MessageChainBuilder();
            messages.append("群组编号：").append(entry.getKey().toString()).append("\n");
            List<Long> value = entry.getValue();
            for (Long group : value) {
                String groupName = "未知";
                try {
                    groupName = Objects.requireNonNull(event.getBot().getGroup(group)).getName();
                } catch (Exception e) {
                    l.warning("机器人没有这个群！");
                }
                messages.append(String.valueOf(group))
                        .append("->")
                        //前面的方法是如果不为空就返回
                        .append(groupName)
                        .append("\n");
            }
            FMB.add(bot,messages.build());
        }
        subject.sendMessage(FMB.build());
    }


}
