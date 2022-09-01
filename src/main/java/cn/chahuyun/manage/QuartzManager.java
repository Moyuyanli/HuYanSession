package cn.chahuyun.manage;

import cn.chahuyun.data.StaticData;
import cn.chahuyun.entity.QuartzInfo;
import cn.chahuyun.job.TimingJob;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;

import static cn.chahuyun.HuYanSession.log;

/**
 * @author huobing
 * @description 定时器消息管理
 * @date 2022/7/1  21:39
 **/
public class QuartzManager {

    public final static QuartzManager INSTANCE = new QuartzManager();

    private static Scheduler scheduler = null;

    static {
        try {
            //获取唯一的调度器
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            // 启动调度
            scheduler.start();
        } catch (SchedulerException e) {
            log.error("定时任务调度器加载失败", e);
        }
    }

    /**
     * 添加任务
     *
     * @param quartzInfo 定时器信息
     */
    public static boolean addSchedulerJob(QuartzInfo quartzInfo) {
        try {
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("data", quartzInfo);
            jobDataMap.put("logger", log);
            jobDataMap.put("groupList", StaticData.getGroupListMap(quartzInfo.getBot()));
            // 创建jobDetail实例，绑定Job实现类
            // 指明job的名称，所在组的名称，以及绑定job类
            JobDetail job = JobBuilder.newJob(TimingJob.class)
                    .withIdentity(String.valueOf(quartzInfo.getId()))
                    .usingJobData(jobDataMap)
                    .build();
            // 定义调度触发规则
            Trigger trigger;
            if (quartzInfo.getCronString() == null) {
                //使用simpleTrigger规则
                trigger = TriggerBuilder.newTrigger().withIdentity(String.valueOf(quartzInfo.getId()))
                        .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(100).withRepeatCount(quartzInfo.getPollingNumber()))
                        .startNow().build();
            } else {
                //使用CronTrigger规则
                trigger = TriggerBuilder.newTrigger().withIdentity(String.valueOf(quartzInfo.getId()))
                        .withSchedule(CronScheduleBuilder.cronSchedule(quartzInfo.getCronString()))
                        .startNow().build();
            }
            // 把作业和触发器注册到任务调度中
            scheduler.scheduleJob(job, trigger);
            return true;
        } catch (Exception e) {
            log.error("定时任务调度器加载失败", e);
            return false;
        }
    }


    /**
     * 删除任务
     *
     * @param quartzInfo 定时任务实体
     */
    public static boolean deleteSchedulerJob(QuartzInfo quartzInfo) {
        //获取triggerKey
        TriggerKey triggerKey = new TriggerKey(String.valueOf(quartzInfo.getId()));
        //先看调度器内部有没有
        CronTriggerImpl trigger;
        try {
            trigger = (CronTriggerImpl) scheduler.getTrigger(triggerKey);
        } catch (SchedulerException e) {
            log.warning("不存在该定时任务", e);
            return false;
        }
        //有就删除
        if (trigger != null) {
            try {
                scheduler.unscheduleJob(triggerKey);
                return true;
            } catch (SchedulerException e) {
                log.error("定时任务删除失败", e);
                return false;
            }
        }
        return false;
    }
}
