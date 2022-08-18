package cn.chahuyun.utils;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.data.RepeatMessage;
import cn.chahuyun.files.ConfigData;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.GroupSettings;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageUtils;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.MiraiLogger;

import java.util.HashMap;
import java.util.HashSet;
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
            MessageChain messages = MessageUtils.newChain().plus(new PlainText("检测到机器人刷屏或人为冲突，阻止失败，请求援助"))
                    .plus(new At(ConfigData.INSTANCE.getOwner()));
            subject.sendMessage(messages);
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
        String key = repeatMessage.getKey();
        HashSet<Character> thisMessageChars = new HashSet<Character>();
        for (int i = 0; i < code.length(); i++) {
            thisMessageChars.add(code.charAt(i));
        }
        int matchingNumber = 0;
        for (Character aChar : thisMessageChars) {
            if (key.contains(aChar.toString())) {
                matchingNumber++;
            }
        }

        if (matchingNumber>ConfigData.INSTANCE.getMatchingNumber()) {
            repeatMessage.setNumberOf(repeatMessage.getNumberOf() + 1);
        } else {
            HashSet<Character> setChar = new HashSet<Character>();
            for (int i = 0; i < code.length(); i++) {
                setChar.add(code.charAt(i));
            }
            repeatMessage.setKey(setChar.toString());
            repeatMessage.setNumberOf(1);
        }
        repeatMessageMap.put(group, repeatMessage);
        return false;
    }

}