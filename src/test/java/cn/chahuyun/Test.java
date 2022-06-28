package cn.chahuyun;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.quartz.TriggerBuilder.newTrigger;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :
 * @Date 2022/6/17 19:35
 */
public class Test {

    public static void main(String[] args) {

        Map<String,String> map = new HashMap<>();
        map.put("卧槽", "abc");
        map.put("卧", "abc");
        map.put("槽", "abc");
        String s = map.keySet().toString();
        System.out.println(s);
        String a = "卧";
        System.out.println(s.indexOf(a));
        String s1 = "abcdef";
        String s2 = "bc";
        String s3 = "abc";
        String s4 = "ef";
        System.out.println(s1.indexOf(s2));
        System.out.println(s1.indexOf(s3));
        System.out.println(s1.indexOf(s4));
        System.out.println(s1.length()-s4.length());

        System.out.println(s1.substring(0,1));

        System.out.println(s1.substring(s1.length() - s4.length()));
        System.out.println(s1.substring(s1.length() - s4.length()));

        String[] strings = new String[2];
        System.out.println(strings.length);

        System.out.println("-------------------");

        try {
            Scheduler defaultScheduler = StdSchedulerFactory.getDefaultScheduler();

            CronScheduleBuilder builder = CronScheduleBuilder.cronSchedule("0/30 * * * * ?");
            CronTrigger trigger = newTrigger()
                    .withIdentity("trigger1", "group1")
                    .withSchedule(builder)
                    .build();

            //定义一个JobDetail  (任务)
//            JobDetail job = JobBuilder.newJob(JobBen.class) //指定干活的类MailJob
//                    .withIdentity("mailjob1", "mailgroup") //定义任务名称和所属任务的分组
//                    .usingJobData("s", "卧槽") //定义传入任务里的属性(key,value)
//                    .build();

            //调度加入这个job
//            defaultScheduler.scheduleJob(job, trigger);

            //启动任务
            defaultScheduler.start();

            //等待20秒，让前面的任务都执行完了之后，再关闭调度器
            Thread.sleep(20000);
            defaultScheduler.shutdown(true);

        } catch (SchedulerException | InterruptedException e) {
            e.printStackTrace();
        }

    }


}


