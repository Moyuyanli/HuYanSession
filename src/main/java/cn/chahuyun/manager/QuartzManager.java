package cn.chahuyun.manager;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.entity.TimingTaskBase;
import cn.chahuyun.job.TimingJobBase;
import net.mamoe.mirai.utils.MiraiLogger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;

import java.text.ParseException;

/**
 * @description  定时器消息管理
 * @author  huobing
 * @date  2022/7/1  21:39
 **/
public class QuartzManager {

    private final static MiraiLogger l = HuYanSession.INSTANCE.getLogger();

    private static Scheduler scheduler = null;

    static {
        try {
            //获取唯一的调度器
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            // 启动调度
            scheduler.start();
        } catch (SchedulerException e) {
            l.error("定时任务调度器加载失败");
            l.error(e.getMessage());
        }
    }

    /**
     * 添加定时任务
     * @param timingTaskBase 调度器基类
     */
    public static boolean addSchdulerJob(TimingTaskBase timingTaskBase) {
        try {
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("data", timingTaskBase);
            jobDataMap.put("logger", l);
            // 创建jobDetail实例，绑定Job实现类
            // 指明job的名称，所在组的名称，以及绑定job类
            JobDetail job = JobBuilder.newJob(TimingJobBase.class)
                    .withIdentity(timingTaskBase.getId()+"")
                    .usingJobData(jobDataMap)
                    .build();
            // 定义调度触发规则
            Trigger trigger = null;
            if(timingTaskBase.getCronString() == null){
                //使用simpleTrigger规则
                trigger=TriggerBuilder.newTrigger().withIdentity(timingTaskBase.getId()+"")
                        .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(100).withRepeatCount(timingTaskBase.getPoll()))
                        .startNow().build();
            }else{
                //使用CronTrigger规则
                trigger=TriggerBuilder.newTrigger().withIdentity(timingTaskBase.getId()+"")
                        .withSchedule(CronScheduleBuilder.cronSchedule(timingTaskBase.getCronString()))
                        .startNow().build();
            }
            // 把作业和触发器注册到任务调度中
            scheduler.scheduleJob(job, trigger);
            return true;
        } catch (Exception e) {
            l.error("定时任务调度器加载失败");
            l.error(e.getMessage());
            return false;
        }
    }

    /**
     * 修改定时任务
     * @param timingTaskBase 定时任务基础类
     */
    public static boolean updateSchdulerJob(TimingTaskBase timingTaskBase){
        //获取两个key
        TriggerKey triggerKey = new TriggerKey(timingTaskBase.getId()+"");
        JobKey jobKey = new JobKey(timingTaskBase.getId() + "");
        //cron的新表达式
        CronTriggerImpl trigger = null;
        try {
            //先查看调度器内部有没有
            trigger = (CronTriggerImpl) scheduler.getTrigger(triggerKey);
        } catch (SchedulerException e) {
            l.warning("不存在该定时任务");
            l.warning(e.getMessage());
            return false;
        }

        if (trigger == null) {
            //没有就新建
            addSchdulerJob(timingTaskBase);
        } else {
            try {
                //有就修改，先修改data
                JobDataMap jobDataMap = new JobDataMap();
                jobDataMap.put("data", timingTaskBase);
                //cron表达式
                trigger.setCronExpression(timingTaskBase.getCronString());
                //修改
                scheduler.triggerJob(jobKey,jobDataMap);
                scheduler.rescheduleJob(triggerKey, trigger);
            } catch (ParseException | SchedulerException e) {
                l.error("定时任务添加失败");
                l.error(e.getMessage());
                return false;
            }
        }
        return true;
    }

    /**
     * 删除任务
     * @param timingTaskBase 定时任务基础类
     */
    public static boolean deleteSchedulerJob(TimingTaskBase timingTaskBase) {
        //获取triggerKey
        TriggerKey triggerKey = new TriggerKey(timingTaskBase.getId()+"");
        //先看调度器内部有没有
        CronTriggerImpl trigger = null;
        try {
            trigger = (CronTriggerImpl) scheduler.getTrigger(triggerKey);
        } catch (SchedulerException e) {
            l.warning("不存在该定时任务");
            l.warning(e.getMessage());
            return false;
        }
        //有就删除
        if (trigger != null) {
            try {
                scheduler.unscheduleJob(triggerKey);
                return true;
            } catch (SchedulerException e) {
                l.error("定时任务删除失败");
                l.error(e.getMessage());
                return false;
            }
        }
        return false;
    }
}
