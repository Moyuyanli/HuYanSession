package cn.chahuyun.session.controller;

import cn.chahuyun.session.config.SessionConfig;
import cn.chahuyun.session.data.RepeatMessage;
import cn.chahuyun.session.manage.PluginRuntime;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageUtils;
import net.mamoe.mirai.message.data.PlainText;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static cn.chahuyun.session.HuYanSession.LOGGER;

/**
 * RepeatMessage
 * 重复消息判断
 *
 * @author Moyuyanli
 * @date 2022/8/18 16:03
 */
public class RepeatMessageAction {

    /**
     * 重写linkedHashMp的清除实体机制
     * 当上一条消息的时间
     * 跟这条消息的时间
     * 相差 config 设定的值时，就会自动清除已保证内存
     */
    private static final Cache<String, RepeatMessage> REPEAT_MESSAGES = Caffeine.newBuilder()
            .maximumSize(Math.max(1L, SessionConfig.INSTANCE.getRuntimeCacheMaximumSize()))
            .expireAfterAccess(Math.max(1, SessionConfig.INSTANCE.getMatchingNumber()), TimeUnit.SECONDS)
            .build();

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
        RepeatMessage repeatMessage = REPEAT_MESSAGES.getIfPresent(mark);
        if (repeatMessage == null) {
            REPEAT_MESSAGES.put(mark, new RepeatMessage(new Date(), 1));
            return false;
        }

        synchronized (repeatMessage) {
            repeatMessage.setOldDate(new Date());
            repeatMessage.setNumberOf(repeatMessage.getNumberOf() + 1);
            int screen = SessionConfig.INSTANCE.getScreen();

            if (repeatMessage.getNumberOf() >= screen + 3) {
                if (group.getBotPermission() == MemberPermission.MEMBER) {
                    return true;
                }
                // Only the first event crossing this threshold schedules recovery.
                if (repeatMessage.getNumberOf() == screen + 3) {
                    group.getSettings().setMuteAll(true);
                    subject.sendMessage(MessageUtils.newChain().plus(new At(SessionConfig.INSTANCE.getOwner()))
                            .plus(new PlainText("检测到有机器人冲突，已开启全体禁言，5秒后将会自动解除！")));
                    PluginRuntime.schedule(() -> {
                        try {
                            group.getSettings().setMuteAll(false);
                            subject.sendMessage(MessageUtils.newChain()
                                    .plus(new At(SessionConfig.INSTANCE.getOwner()))
                                    .plus(new PlainText("机器人冲突已处理，全体禁言解除！")));
                        } catch (Exception exception) {
                            LOGGER.warning("解除全体禁言失败: " + exception.getMessage());
                        }
                    }, 5, TimeUnit.SECONDS);
                }
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
                    group.get(sender.getId()).mute(SessionConfig.INSTANCE.getForbiddenTime());
                } catch (Exception e) {
                    LOGGER.error("刷屏处理失败!");
                    subject.sendMessage("检测到刷屏,阻止失败!");
                }
                return true;
            }
        }
        return false;
    }

    public static void clear() {
        REPEAT_MESSAGES.invalidateAll();
        REPEAT_MESSAGES.cleanUp();
    }

}
