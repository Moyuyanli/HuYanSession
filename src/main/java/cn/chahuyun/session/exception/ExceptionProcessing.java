package cn.chahuyun.session.exception;

import cn.chahuyun.session.config.SessionConfig;
import cn.chahuyun.session.HuYanSession;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.BotIsBeingMutedException;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.MessageTooLargeException;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.EventCancelledException;
import net.mamoe.mirai.utils.MiraiLogger;
import org.jetbrains.annotations.NotNull;

/**
 * 异常处理器
 *
 * @author Moyuyanli
 * @date 2023/8/31 15:03
 */
public class ExceptionProcessing extends SimpleListenerHost {

    private static final MiraiLogger LOGGER = HuYanSession.LOGGER;

    public static final ExceptionProcessing INSTANCE = new ExceptionProcessing();

    @Override
    public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception) {
        if (exception instanceof EventCancelledException) {
            LOGGER.error("发送消息被取消:", exception);
        } else if (exception instanceof BotIsBeingMutedException) {
            LOGGER.error("你的机器人被禁言:", exception);
        } else if (exception instanceof MessageTooLargeException) {
            LOGGER.error("发送消息过长:", exception);
        } else if (exception instanceof IllegalArgumentException) {
            LOGGER.error("发送消息为空:", exception);
        } else {
            // 处理事件处理时抛出的异常
            LOGGER.error("出错啦~", exception);
        }

        if (!HuYanSession.CONFIG.getExceptionSwitch()) {
            return;
        }

        try {
            Friend owner = Bot.getInstances().get(0).getFriend(SessionConfig.INSTANCE.getOwner());
            if (owner == null) {
                return;
            }
            owner.sendMessage("壶言会话出错:" + exception.getCause().getCause().toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
