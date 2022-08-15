package cn.chahuyun.utils;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.files.ConfigData;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.SingleMessage;
import net.mamoe.mirai.utils.MiraiLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ShareUtils
 *
 * @author Zhangjiaxing
 * @description 公共工具包
 * @date 2022/7/29 12:40
 */
public class ShareUtils {

    private final static MiraiLogger l = HuYanSession.INSTANCE.getLogger();

    private static final Map<String, Integer> map = new HashMap<>();

    private ShareUtils() {
    }

    /**
     * 返回是否为退出
     *
     * @param event 消息事件
     * @return boolean true 退出
     * @author Moyuyanli
     * @date 2022/7/29 12:43
     */
    public static boolean isQuit(MessageEvent event) {
        String messagePattern = "^!!!|^！！！";
        Pattern pattern = Pattern.compile(messagePattern);
        Matcher matcher = pattern.matcher(event.getMessage().serializeToMiraiCode());
        return matcher.find();
    }

    /**
     * 添加下N条忽略消息
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/8/15 9:19
     */
    public static void spotPause(MessageEvent event) {
        //! pause @bot num?
        MessageChain message = event.getMessage();
        String contentToString = message.contentToString();
        Contact subject = event.getSubject();
        Bot thisBot = event.getBot();
        User sender = event.getSender();

        long botQq = 0;
        for (SingleMessage singleMessage : message) {
            if (singleMessage instanceof At) {
                botQq = ((At) singleMessage).getTarget();
            }
        }
        Bot bot = Bot.getInstance(botQq);
        if (bot != thisBot) {
            return;
        }

        String[] split = contentToString.split(" +");
        int num = 1;
        if (split.length == 3) {
            num = Integer.parseInt(split[2]);
        }

        String mark = botQq + "." + sender.getId();

        map.put(mark, num);
        subject.sendMessage(bot.getNick() + "(" + botQq + ")开始忽略接下来你的 " + num + " 条消息");
    }

    /**
     * 判断该用户的下一条消息是否忽略
     *
     * @param event 消息事件
     * @return boolean  true 忽略下一条消息
     * @author Moyuyanli
     * @date 2022/8/15 9:18
     */
    public static boolean isPause(MessageEvent event) {
        Bot bot = event.getBot();
        User sender = event.getSender();

        String mark = bot.getId() + "." + sender.getId();

        if (map.containsKey(mark)) {
            Integer integer = map.get(mark);
            l.info("integer-"+integer);
            if (integer > 0) {
                map.put(mark, integer - 1);
                return true;
            }
            return false;
        }
        return false;
    }

}