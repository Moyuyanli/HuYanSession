package cn.chahuyun.event;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.manage.GroupManager;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.GroupEvent;
import net.mamoe.mirai.event.events.MemberJoinEvent;
import net.mamoe.mirai.event.events.MemberJoinRequestEvent;
import net.mamoe.mirai.utils.MiraiLogger;
import org.jetbrains.annotations.NotNull;

/**
 * GroupEventListener
 * 群事件
 *
 * @author Moyuyanli
 * @date 2022/8/22 10:34
 */
public class GroupEventListener extends SimpleListenerHost {

    private static final MiraiLogger l = HuYanSession.INSTANCE.getLogger();

    @Override
    public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception) {
        // 处理事件处理时抛出的异常
        l.error("出错啦~", exception);
    }

    /**
     * 其他群事件
     */
    @EventHandler
    public void onMessage(@NotNull GroupEvent event) throws Exception { // 可以抛出任何异常, 将在 handleException 处理
        if (event instanceof MemberJoinEvent) {
            GroupManager.userJoinGroup((MemberJoinEvent) event);
        }


    }

    /**
     * 申请加入事件
     */
    @EventHandler
    public void onMessageTwo(@NotNull MemberJoinRequestEvent event) throws Exception { // 可以抛出任何异常, 将在 handleException 处理
        GroupManager.userRequestGroup(event);
    }

}