package cn.chahuyun.entity;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.files.ConfigData;
import cn.chahuyun.files.GroupData;
import com.alibaba.fastjson.JSONObject;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.utils.MiraiLogger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.awt.*;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * TimingJobBase
 *
 * @author Zhangjiaxing
 * @description 定时器任务的基本实体
 * @date 2022/7/1 11:25
 */
public class TimingJobBase implements Job {

    private MiraiLogger l = HuYanSession.INSTANCE.getLogger();

    /**
     * 定时器发送消息任务
     * @author zhangjiaxing
     * @param context 数据
     * @date 2022/7/1 14:39
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        int id = (int) context.get("id");
        String name = (String) context.get("name");
        int type = (int) context.get("type");
        String value = (String) context.get("value");
        ArrayList<String> values = JSONObject.parseObject((String) context.get("values"), (Type) List.class);
        int poll = (int) context.get("poll");
        ScopeInfoBase scope = JSONObject.parseObject((String) context.get("scope"),ScopeInfoBase.class);

        l.info("定时器"+id+"-"+name+"执行!");

        long botQq = ConfigData.INSTANCE.getBot();
        Bot bot = Bot.getInstance(botQq);

        if (scope.getGroupType()) {
            java.util.List<Long> longs = GroupData.INSTANCE.getGroupList().get(scope.getScopeNum());
            for (Long aLong : longs) {

                Group group = null;
                try {
                    group = bot.getGroup(aLong);
                } catch (Exception e) {
                    l.error("定时任务"+ id + "获取群失败！");
                    l.error("定时任务"+ id + e.getMessage());
                }
                assert group != null;
                group.sendMessage(value);
            }


        }

    }
}