package cn.chahuyun.timingManager;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.entity.TimingTaskBase;
import cn.chahuyun.files.TimingData;
import cn.chahuyun.utils.QuartzUtil;
import com.alibaba.fastjson.JSONObject;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.utils.MiraiLogger;

import java.util.Map;

/**
 * TimingManager
 *
 * @author Zhangjiaxing
 * @description 定时任务管理类
 * @date 2022/6/30 9:00
 */
public class TimingManager {

    public static final TimingManager INSTANCE = new TimingManager();
    private MiraiLogger l = HuYanSession.INSTANCE.getLogger();


    /**
     * 加载定时任务
     * @author zhangjiaxing
     * @date 2022/7/1 15:35
     */
    public void init() {
        Map<Integer, TimingTaskBase> timingTaskBaseMap = TimingData.INSTANCE.readTimingList();
        //读取所有定时器，然后都加载一遍，开启的就给他开启了
        for (Map.Entry<Integer, TimingTaskBase> taskBaseEntry : timingTaskBaseMap.entrySet()) {
            if (!taskBaseEntry.getValue().getState()) {
                QuartzUtil.addSchdulerJob(taskBaseEntry.getValue());
            }
        }
    }

    /**
     * 开启或关闭定时器
     * @author zhangjiaxing
     * @param event 消息事件
     * @date 2022/7/1 16:00
     */
    public void operateTiming(MessageEvent event) {
        Contact subject = event.getSubject();
        String code = event.getMessage().serializeToMiraiCode();

        Map<Integer, TimingTaskBase> timingTaskBaseMap = TimingData.INSTANCE.readTimingList();

        boolean ooc = code.startsWith("+");
        String[] split = code.split("[:：]");
        int key = Integer.parseInt(split[1]);

        TimingTaskBase taskBase = null;
        try {
            taskBase = timingTaskBaseMap.get(key);
            String s = JSONObject.toJSONString(taskBase);
            l.info(s);
        } catch (Exception e) {
            subject.sendMessage("没有找到该定时器！");
            return;
        }
        if (ooc) {
            if (taskBase.getState()) {
                subject.sendMessage("定时器" + taskBase.getName() + "已经是开启状态了，无法开启");
            }
            if (QuartzUtil.updateSchdulerJob(taskBase)) {
                subject.sendMessage("定时器" + taskBase.getName() + "开启成功！");
            } else {
                subject.sendMessage("定时器" + taskBase.getName() + "开启失败！");
            }
            TimingData.INSTANCE.setTimingState(key, true);
        } else {
            if (!taskBase.getState()) {
                subject.sendMessage("定时器" + taskBase.getName() + "已经是关闭状态了，无法关闭");
            }
            if (QuartzUtil.deleteSchedulerJob(taskBase)) {
                subject.sendMessage("定时器" + taskBase.getName() + "关闭成功！");
            } else {
                subject.sendMessage("定时器" + taskBase.getName() + "关闭失败！");
            }
            TimingData.INSTANCE.setTimingState(key, false);
        }
    }


    public void checkTiming(MessageEvent event) {
        Contact subject = event.getSubject();
        String code = event.getMessage().serializeToMiraiCode();

        ForwardMessageBuilder fMB = new ForwardMessageBuilder(subject);
        MessageChainBuilder mCB = new MessageChainBuilder();

        Map<Integer, TimingTaskBase> taskBaseMap = TimingData.INSTANCE.readTimingList();

        String[] split = code.split("[：:]");



        if (split.length == 1) {
            mCB.append("所有定时任务↓\n");
            for (Map.Entry<Integer, TimingTaskBase> baseEntry : taskBaseMap.entrySet()) {
                mCB.append(baseEntry.getKey().toString())
                        .append(":")
                        .append(baseEntry.getValue().getName())
                        .append(baseEntry.getValue().getState() ? "-开启" : "-关闭")
                        .append("\n");
            }
            subject.sendMessage(mCB.build());
        }


    }



}