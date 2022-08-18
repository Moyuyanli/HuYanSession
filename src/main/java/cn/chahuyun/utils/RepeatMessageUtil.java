package cn.chahuyun.utils;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.data.RepeatMessage;
import cn.chahuyun.files.ConfigData;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.GroupSettings;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.utils.MiraiLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * RepeatMessage
 * 重复消息判断
 *
 * @author Zhangjiaxing
 * @date 2022/8/18 16:03
 */
public class RepeatMessageUtil {

    private final static MiraiLogger l = HuYanSession.INSTANCE.getLogger();

    private static final Map<Long, RepeatMessage> repeatMessageMap = new HashMap<>();

    /**
     * 检测刷屏和机器人冲突
     *
     * @param event 消息事件
     * @return boolean
     * @author Moyuyanli
     * @date 2022/8/18 16:21
     */
    public static boolean isScreen(MessageEvent event) {
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();

        if (!(subject instanceof Group)) {
            return false;
        }
        long group = subject.getId();

        if (!repeatMessageMap.containsKey(group)) {
            repeatMessageMap.put(group, new RepeatMessage(group, code, 1));
        }

        RepeatMessage repeatMessage = repeatMessageMap.get(group);
        if (repeatMessage.getNumberOf() >= ConfigData.INSTANCE.getScreen()*10) {
            event.intercept();
            Bot bot = event.getBot();
            bot.getGroup(group).getSettings().setMuteAll(true);
            subject.sendMessage("检测到机器人冲突或人为冲突，已阻止！");
            return true;
        }else if (repeatMessage.getNumberOf() >= ConfigData.INSTANCE.getScreen()*5) {
            event.intercept();
            Bot bot = event.getBot();
            bot.getGroup(group).get(event.getSender().getId()).mute(7200);
            subject.sendMessage("检测到机器人冲突，已阻止！");
            return true;
        } else if (repeatMessage.getNumberOf() >= ConfigData.INSTANCE.getScreen()) {
            event.intercept();
            Bot bot = event.getBot();
            bot.getGroup(group).get(event.getSender().getId()).mute(60);
            subject.sendMessage("检测到刷屏，已阻止！");
        }

        if (repeatMessage.getKey().equals(code)) {
            repeatMessage.setNumberOf(repeatMessage.getNumberOf() + 1);
            repeatMessageMap.put(group, repeatMessage);
            return false;
        } else {
            repeatMessage.setKey(code);
            repeatMessage.setNumberOf(1);
            repeatMessageMap.put(group, repeatMessage);
            return false;
        }
    }

}