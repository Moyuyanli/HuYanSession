package cn.chahuyun.utils;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.config.ConfigData;
import cn.chahuyun.data.RepeatMessage;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageUtils;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.MiraiLogger;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

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

        repeatMessage.setNumberOf(repeatMessage.getNumberOf() + 1);

        //刷屏判定次数
        int screen = ConfigData.INSTANCE.getScreen();

        if (repeatMessage.getNumberOf() > screen * 1.5) {
            if (repeatMessage.isReplyTo()) {
                subject.sendMessage(MessageUtils.newChain().plus(new At(ConfigData.INSTANCE.getOwner()))
                        .plus(new PlainText("有机器人冲突，已阻止!")));
                repeatMessage.setReplyTo(true);
            }
            group.get(sender.getId()).mute(1200);
            return true;
        } else if (repeatMessage.getNumberOf() >= screen) {
            if (repeatMessage.isReplyTo()) {
                subject.sendMessage("检测到刷屏,已阻止!");
                repeatMessage.setReplyTo(true);
            }
            group.get(sender.getId()).mute(60);
            return true;
        }
        repeatMessageMap.put(mark, repeatMessage);
        return false;
    }

}