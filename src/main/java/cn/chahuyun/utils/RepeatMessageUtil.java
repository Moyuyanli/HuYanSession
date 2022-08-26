package cn.chahuyun.utils;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.config.ConfigData;
import cn.chahuyun.data.RepeatMessage;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageUtils;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.MiraiLogger;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * RepeatMessage
 * 重复消息判断
 *
 * @author Moyuyanli
 * @date 2022/8/18 16:03
 */
public class RepeatMessageUtil {

    private final static MiraiLogger l = HuYanSession.INSTANCE.getLogger();
    /**
     * 重写linkedHashMp的清除实体机制
     * 当上一条消息的时间
     * 跟这条消息的时间
     * 相差 config 设定的值时，就会自动清除已保证内存
     */
    private static final Map<String, RepeatMessage> repeatMessageMap = new LinkedHashMap<>(2000, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, RepeatMessage> eldest) {
            RepeatMessage value = eldest.getValue();
            return new Date().getTime() - value.getOldDate().getTime() > 1000L * ConfigData.INSTANCE.getMatchingNumber();
        }
    };

    /**
     * 检测刷屏和机器人冲突
     *
     * @param event 消息事件
     * @return boolean
     * @author Moyuyanli
     * @date 2022/8/18 16:21
     */
    public static boolean isScreen(MessageEvent event) {
        User sender = event.getSender();
        Contact subject = event.getSubject();
        Group group = (Group) subject;

        String mark = group.getId() + "." + sender.getId();
        RepeatMessage repeatMessage;
        if (repeatMessageMap.containsKey(mark)) {
            repeatMessage = repeatMessageMap.get(mark);
        } else {
            repeatMessage = new RepeatMessage(new Date(), 1);
            repeatMessageMap.put(mark, repeatMessage);
            return false;
        }

        long timeThreshold = 1000L * ConfigData.INSTANCE.getMatchingNumber();

        long time = new Date().getTime();
        if (time - repeatMessage.getOldDate().getTime() > timeThreshold) {
            return false;
        }

        repeatMessage.setOldDate(new Date());
        repeatMessage.setNumberOf(repeatMessage.getNumberOf() + 1);

        //刷屏判定次数
        int screen = ConfigData.INSTANCE.getScreen();

        //突破3次
        if (repeatMessage.getNumberOf() >= screen + 3) {
            if (group.getBotPermission() == MemberPermission.MEMBER) {
                return true;
            }
            group.getSettings().setMuteAll(true);
            subject.sendMessage(MessageUtils.newChain().plus(new At(ConfigData.INSTANCE.getOwner()))
                    .plus(new PlainText("检测到有机器人冲突，已开启全体禁言，5秒后将会自动解除！")));

            //延时任务解除禁言
            ScheduledExecutorService botScheduledExecutorService = new ScheduledThreadPoolExecutor(5);
            botScheduledExecutorService.schedule(() -> {
                subject.sendMessage(MessageUtils.newChain()
                        .plus(new At(ConfigData.INSTANCE.getOwner()))
                        .plus(new PlainText("机器人冲突已处理，全体禁言解除！")));
                group.getSettings().setMuteAll(false);
                repeatMessage.setReplyTo(true);
            }, 5, TimeUnit.SECONDS);//线程实现，2、延迟时间 3.单位

            repeatMessageMap.put(mark, repeatMessage);
            return true;
        } else if (repeatMessage.getNumberOf() >= screen) {
            if (group.getBotPermission() == MemberPermission.MEMBER) {
                return true;
            }
            if (!repeatMessage.isReplyTo()) {
                subject.sendMessage("检测到刷屏,已阻止!");
                repeatMessage.setReplyTo(true);
            }
            try {
                group.get(sender.getId()).mute(60);
            } catch (Exception e) {
                l.error("刷屏处理失败!");
                subject.sendMessage("检测到刷屏,阻止失败!");
            }
            repeatMessageMap.put(mark, repeatMessage);
            return true;
        }
        repeatMessageMap.put(mark, repeatMessage);
        return false;
    }

}