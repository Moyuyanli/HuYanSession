package cn.chahuyun.entity;

import cn.chahuyun.HuYanSession;
import com.alibaba.fastjson.JSONObject;
import net.mamoe.mirai.utils.MiraiLogger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.List;

/**
 * TimingTaskBase
 *
 * @author Zhangjiaxing
 * @description 定时器任务基础实体类
 * @date 2022/6/28 11:12
 */
public class TimingTaskBase {
    /**
     * 定时器id
     */
    private int id;
    /**
     * 定时器任务名称
     */
    private String name;
    /**
     * 定时器时间解析
     */
    private String timeResolve;
    /**
     * 定时器cron
     */
    private String cronString;
    /**
     * 定时器回复内容类型
     */
    private int type;
    /**
     * 定时器回复内容
     */
    private String value;
    /**
     * 定时器多词条回复内容
     */
    private List<String> values;
    /**
     * 轮询次数
     */
    private int poll;
    /**
     * 定时器作用群组
     */
    private ScopeInfoBase scope;
    /**
     * 定时器当前状态
     * false 关闭 true 开启
     */
    private Boolean state;

    private MiraiLogger l = HuYanSession.INSTANCE.getLogger();


    /**
     * 单一调度器
     */
    private Scheduler scheduler ;

    /**
     * 加载定时器
     * @author zhangjiaxing
     * @date 2022/7/1 11:28
     * @return boolean
     */
    public boolean initTiming(){
        try {
            //获取唯一的调度器
            scheduler = StdSchedulerFactory.getDefaultScheduler();
        } catch (SchedulerException e) {
            l.error("定时任务"+ id +"调度器加载失败");
            l.error("定时任务"+ e.getMessage());
        }
        //cron表达式
        CronScheduleBuilder cronSchedule = CronScheduleBuilder.cronSchedule(cronString);
        //定时器
        CronTrigger cronTrigger = TriggerBuilder.newTrigger().withIdentity(name).withSchedule(cronSchedule).build();
        String jsonString = JSONObject.toJSONString(values);
        String toJSONString = JSONObject.toJSONString(scope);
        //任务deta
        JobDetail jobDetail = JobBuilder.newJob(TimingJobBase.class).withIdentity(id + name)
                .usingJobData("id", id)
                .usingJobData("name", name)
                .usingJobData("type", type)
                .usingJobData("value", value)
                .usingJobData("values", jsonString)
                .usingJobData("poll", poll)
                .usingJobData("scope", toJSONString)
                .build();

        try {
            assert scheduler != null;
            scheduler.scheduleJob(jobDetail, cronTrigger);
            return true;
        } catch (SchedulerException e) {
            l.error("定时任务"+ id + "号调度器任务添加失败");
            l.error("定时任务" + e.getMessage());
            return false;
        }
    }

    /**
     * 开启定时器
     * @author zhangjiaxing
     * @date 2022/7/1 15:32
     */
    public boolean startTiming(){
        try {
            scheduler.start();
            state = true;
            return true;
        } catch (SchedulerException e) {
            l.error("定时任务"+ id + "调度器启动失败");
            l.error("定时任务"+ e.getMessage());
            return false;
        }

    }

    /**
     * 关闭定时器
     * @author zhangjiaxing
     * @date 2022/7/1 15:32
     */
    public boolean shutTiming(){
        try {
            scheduler.pauseTrigger(TriggerKey.triggerKey(name));
            state = false;
            return true;
        } catch (SchedulerException e) {
            l.error("定时任务"+ id + "调度器关闭失败");
            l.error("定时任务"+ e.getMessage());
            return false;
        }
    }

    public TimingTaskBase(int id, String name, String timeResolve, String cronString, int type, String value, List<String> values, int poll, ScopeInfoBase scope) {
        this.id = id;
        this.name = name;
        this.timeResolve = timeResolve;
        this.cronString = cronString;
        this.type = type;
        this.value = value;
        this.values = values;
        this.poll = poll;
        this.scope = scope;
        this.state = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimeResolve() {
        return timeResolve;
    }

    public void setTimeResolve(String timeResolve) {
        this.timeResolve = timeResolve;
    }

    public String getCronString() {
        return cronString;
    }

    public void setCronString(String cronString) {
        this.cronString = cronString;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public int getPoll() {
        return poll;
    }

    public void setPoll(int poll) {
        this.poll = poll;
    }

    public ScopeInfoBase getScope() {
        return scope;
    }

    public void setScope(ScopeInfoBase scope) {
        scope = scope;
    }

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }
}