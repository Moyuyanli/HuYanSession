package cn.chahuyun.manager;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.entity.ScopeInfoBase;
import cn.chahuyun.entity.TimingTaskBase;
import cn.chahuyun.files.GroupData;
import cn.chahuyun.files.TimingData;
import kotlin.coroutines.EmptyCoroutineContext;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.ConcurrencyKind;
import net.mamoe.mirai.event.EventPriority;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.utils.MiraiLogger;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TimingManager
 *
 * @author Zhangjiaxing
 * @description 定时任务管理类
 * @date 2022/6/30 9:00
 */
public class TimingManager {

    public static final TimingManager INSTANCE = new TimingManager();
    private final MiraiLogger l = HuYanSession.INSTANCE.getLogger();


    /**
     * 加载定时任务
     * @author zhangjiaxing
     * @date 2022/7/1 15:35
     */
    public void init() {
        Map<Integer, TimingTaskBase> timingTaskBaseMap = TimingData.INSTANCE.readTimingList();
        //读取所有定时器，然后都加载一遍，开启的就给他开启了
        for (Map.Entry<Integer, TimingTaskBase> taskBaseEntry : timingTaskBaseMap.entrySet()) {
            if (taskBaseEntry.getValue().getState()) {
                QuartzManager.addSchdulerJob(taskBaseEntry.getValue());
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

        boolean ooc = code.startsWith("+")||code.startsWith("开启");
        String[] split = code.split("[:：]");
        int key = Integer.parseInt(split[1]);

        TimingTaskBase taskBase = null;
        try {
            taskBase = timingTaskBaseMap.get(key);
            if (taskBase == null) {
                subject.sendMessage("没有找到该定时器！");
                return;
            }
        } catch (Exception e) {
            subject.sendMessage("查找定时器数据时出错！");
            return;
        }
        if (ooc) {
            if (taskBase.getState()) {
                subject.sendMessage("定时器" + taskBase.getName() + "已经是开启状态了，无法开启");
                return;
            }
            if (QuartzManager.updateSchdulerJob(taskBase)) {
                subject.sendMessage("定时器" + taskBase.getName() + "开启成功！");
            } else {
                subject.sendMessage("定时器" + taskBase.getName() + "开启失败！");
            }
            TimingData.INSTANCE.setTimingState(key, true);
        } else {
            if (!taskBase.getState()) {
                subject.sendMessage("定时器" + taskBase.getName() + "已经是关闭状态了，无法关闭");
                return;
            }
            if (QuartzManager.deleteSchedulerJob(taskBase)) {
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
        }else
        if (split.length == 2) {
            int key = Integer.parseInt(split[1]);
            if (!taskBaseMap.containsKey(key)) {
                subject.sendMessage("没有找到该定时器!");
                return;
            }
            mCB.append("查询到该定时任务:"+key+"\n");
            for (Map.Entry<Integer, TimingTaskBase> entry : taskBaseMap.entrySet()) {
                if (entry.getKey() == key) {
                    TimingTaskBase base = entry.getValue();
                    mCB.append("定时器编号:").append(String.valueOf(base.getId())).append("\n")
                            .append("定时器名称:").append(base.getName()).append("\n")
                            .append("定时器频率:").append(base.getTimeResolve() == null ? "目前不支持" : base.getTimeResolve()).append("\n")
                            .append("定时器Cron表达式:").append(base.getCronString()).append("\n")
                            .append("定时器作用域:");
                    ScopeInfoBase scopeInfoBase = base.getScope();
                    if (scopeInfoBase.getType()) {
                        mCB.append("群-").append(scopeInfoBase.getScopeCode() + "\n");
                    } else if (scopeInfoBase.getGroupType()) {
                        mCB.append("群组-").append(scopeInfoBase.getScopeNum() + "\n");
                    } else {
                        mCB.append("全局\n");
                    }
                    mCB.append("定时器的回复内容:").append(base.getValue()).append("\n")
                    .append("定时器状态:").append(base.getState()?"启用中":"关闭");
                }
            }
        }

        subject.sendMessage(mCB.build());

    }


    public void deleteTiming(MessageEvent event) {
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();

        String toString = event.getMessage().contentToString();
        if (!Pattern.matches("删除定时任务[:：]\\d+|%ds[:：]\\d+", toString)) {
            subject.sendMessage("格式错误，指令详情请查看帮助");
            return;
        }

        String[] split = code.split("[:：]");
        int key = Integer.parseInt(split[1]);


        MessageChain messages = TimingData.INSTANCE.deleteTimingList(key);
        subject.sendMessage(messages);

    }


    private String timingName;
    private String timeResolve;
    private String cronString;
    private int scopeNum;
    private String value;

    /**
     * 添加定时任务
     * @author zhangjiaxing
     * @param event 消息事件
     * @param stage 添加步骤编号
     * @date 2022/6/30 19:31
     */
    public void addTiming(MessageEvent event,int stage) {
        Contact subject = event.getSubject();
        String code = event.getMessage().serializeToMiraiCode();


        switch (stage) {
            case 0:
                subject.sendMessage("开始添加定时器，请发送定时器名称：");
                replyTimingMessage(event, stage + 1);
                event.intercept();
                break;
            case 1:
                if (Pattern.matches("[!！]{3}", code)) {
                    return;
                }
                timingName = event.getMessage().contentToString();
                subject.sendMessage("定时器名称设置成功，请发送定时器频率：");
                replyTimingMessage(event, stage + 1);
                event.intercept();
                break;
            case 2:
                if (Pattern.matches("[!！]{3}", code)) {
                    return;
                }
                timingTimeResolve(event, stage);
                event.intercept();
                break;
            case 3:
                if (Pattern.matches("[!！]{3}", code)) {
                    return;
                }
                if (Pattern.matches("[是1对]|确定|确认", code)) {
                    subject.sendMessage("请发送定时器作用群组编号，发送0为全局。");
                    replyTimingMessage(event, stage + 1);
                } else {
                    subject.sendMessage("请重新发送定时器频率：");
                    replyTimingMessage(event,stage-1);
                }
                event.intercept();
                break;
            case 4:
                if (Pattern.matches("[!！]{3}", code)) {
                    return;
                }
                scopeNum = Integer.parseInt(code);
                if (scopeNum != 0) {
                    boolean containsKey = GroupData.INSTANCE.getGroupList().containsKey(scopeNum);
                    if (!containsKey) {
                        subject.sendMessage("没有该群组信息，请检查群组!");
                        return;
                    }
                }
                subject.sendMessage("请发送定时发送的内容：");
                replyTimingMessage(event,stage+1);
                event.intercept();
                break;
            case 5:
                if (Pattern.matches("[!！]{3}", code)) {
                    return;
                }
                value = code;
                stage++;
                break;
            default:break;
        }
        if (stage == 6) {
            ScopeInfoBase scopeInfoBase;
            if (scopeNum == 0) {
                scopeInfoBase = new ScopeInfoBase("全局", false, false, null, 0);
            } else {
                scopeInfoBase = new ScopeInfoBase("群组", false, true, null, scopeNum);
            }
            TimingTaskBase base = new TimingTaskBase(TimingData.INSTANCE.getTimingNum(),
                    timingName,
                    timeResolve,
                    cronString,
                    0,
                    value,
                    null,
                    0,
                    scopeInfoBase
            );
            TimingData.INSTANCE.addTimingList(base);
            subject.sendMessage("定时器添加完成！");
        }
    }

    /**
     * 添加定时任务的重复调用
     * @author zhangjiaxing
     * @param event 消息事件
     * @param stage 添加步骤编号
     * @date 2022/6/30 19:32
     */
    private void replyTimingMessage(MessageEvent event, int stage) {
        GlobalEventChannel.INSTANCE.filterIsInstance(FriendMessageEvent.class)
                .filter(at -> at.getSubject().getId() == event.getSubject().getId())
                .subscribeOnce(FriendMessageEvent.class, EmptyCoroutineContext.INSTANCE, ConcurrencyKind.LOCKED, EventPriority.HIGH, it -> {
                    addTiming(it, stage);
                });
    }

    /**
     * 中文时间匹配正则
     */
    private final String timingStringPattern = "每(\\S+)?(小时|天|周)(早上)?(\\S点|\\d+:\\d+)?(([一二三四五六天日])([到和])?(周[一二三四五六天日])?)?(的(\\S点|\\d+:\\d+))?";

    /**
     * 添加定时任务的时间频率识别
     * @author zhangjiaxing
     * @param event 消息事件
     * @param stage 添加步骤编号
     * @date 2022/6/30 19:32
     * @return boolean
     */
    private boolean timingTimeResolve(MessageEvent event, int stage) {
        Contact subject = event.getSubject();
        String code = event.getMessage().contentToString();

        Matcher matcher = Pattern.compile("\\$cron\\((\\S+ ){5}\\S\\)").matcher(code);
        String group;
        if (matcher.find()) {
            group = matcher.group();
            l.info("group-"+group);
            cronString = spotVariable(group);
            subject.sendMessage("识别到cron表达式->"+cronString+"<-，是否确认？");
            replyTimingMessage(event,stage+1);
            return true;
        }

        matcher = Pattern.compile(timingStringPattern).matcher(code);
        if (!matcher.find()) {
            subject.sendMessage("我不认识该时间频率，更多请查看本插件帮助！");
            replyTimingMessage(event,stage);
            return false;
        }
        group = matcher.group();

        return false;

    }

    /**
     * 识别cron表达式
     * @author zhangjiaxing
     * @param code 消息
     * @date 2022/6/30 19:33
     * @return java.lang.String
     */
    public String spotVariable(String code) {
        String[] split = code.split("\\(");
        String[] strings = split[1].split("\\)");
        return strings[0];
    }

    /**
     * todo 定时任务时间频率的中文识别
     * @author zhangjiaxing
     * @param group 消息
     * @date 2022/6/30 19:33
     * @return java.lang.String
     */
    private String spotCronString(String group) {
        String[] nums = "零 一 二 三 四 五 六 七 八 九 十".split(" ");

        for (int i = 0; i < nums.length; i++) {
            String num = nums[i];
            group = group.replace(num, String.valueOf(i));
        }

        if (group.indexOf("每") != 1) {
            return null;
        }

        group =  group.substring(1);
        Matcher matcher = Pattern.compile("[天周]|(\\d+)").matcher(group);

        return null;

    }

}