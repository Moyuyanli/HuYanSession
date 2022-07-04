package cn.chahuyun.job;

import cn.chahuyun.entity.ScopeInfoBase;
import cn.chahuyun.entity.TimingTaskBase;
import cn.chahuyun.files.ConfigData;
import cn.chahuyun.files.GroupData;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.utils.MiraiLogger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

/**
 * TimingJobBase
 *
 * @author Zhangjiaxing
 * @description 定时器任务的基本实体
 * @date 2022/7/1 11:25
 */
public class TimingJobBase implements Job {


    /**
     * 定时器发送消息任务
     * @author zhangjiaxing
     * @param context 数据
     * @date 2022/7/1 14:39
     */
    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        TimingTaskBase base = (TimingTaskBase) jobDataMap.get("data");

        MiraiLogger l = (MiraiLogger) jobDataMap.get("logger");

        ScopeInfoBase scope = base.getScope();

        l.info("定时器"+base.getId()+"-"+base.getName()+"执行!");
        System.out.println("定时器"+base.getId()+"-"+base.getName()+"执行!");
        long botQq = ConfigData.INSTANCE.getBot();
        Bot bot = Bot.getInstance(botQq);

        if (scope.getGroupType()) {
            java.util.List<Long> longs = GroupData.INSTANCE.getGroupList().get(scope.getScopeNum());
            for (Long aLong : longs) {

                Group group = null;
                try {
                    group = bot.getGroup(aLong);
                } catch (Exception e) {
                    l.error("定时任务"+ base.getId() + "获取群失败！");
                    l.error("定时任务"+ base.getId() + e.getMessage());
                }
                assert group != null;
                group.sendMessage(MiraiCode.deserializeMiraiCode(base.getValue()));
            }

        }

    }
}