package cn.chahuyun.session.event;

import cn.chahuyun.session.config.BlackListData;
import cn.chahuyun.session.config.SessionConfig;
import cn.chahuyun.session.HuYanSession;
import cn.chahuyun.session.exception.ExceptionProcessing;
import cn.chahuyun.session.manage.GroupManager;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.GroupEvent;
import net.mamoe.mirai.event.events.MemberJoinEvent;
import net.mamoe.mirai.event.events.MemberJoinRequestEvent;
import net.mamoe.mirai.event.events.MemberLeaveEvent;
import net.mamoe.mirai.utils.MiraiLogger;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * GroupEventListener
 * 群事件
 *
 * @author Moyuyanli
 * @date 2022/8/22 10:34
 */
public class GroupEventListener extends SimpleListenerHost {

    private static final MiraiLogger LOGGER = HuYanSession.INSTANCE.getLogger();

    @Override
    public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception) {
        ExceptionProcessing.INSTANCE.handleException(context, exception);
    }

    /**
     * 其他群事件
     */
    @EventHandler
    public void onMessage(@NotNull GroupEvent event) { // 可以抛出任何异常, 将在 handleException 处理
        MemberPermission botPermission = event.getGroup().getBotPermission();
        if (botPermission == MemberPermission.MEMBER) {
            return;
        }
        if (event instanceof MemberJoinEvent) {
            MemberJoinEvent memberJoinEvent = (MemberJoinEvent) event;
            if (GroupManager.detectBlackList(memberJoinEvent)) {
                return;
            }
            GroupManager.userJoinGroup(memberJoinEvent);
        }


    }

    /**
     * 申请加入事件
     */
    @EventHandler
    public void onMessageTwo(@NotNull MemberJoinRequestEvent event) { // 可以抛出任何异常, 将在 handleException 处理
        long groupId = event.getGroupId();
        //判断机器在这个群里的权限
        MemberPermission botPermission = Objects.requireNonNull(event.getGroup()).getBotPermission();
        if (botPermission == MemberPermission.MEMBER) {
            return;
        }
        //是否是检测群
        if (!SessionConfig.INSTANCE.getGroupList().contains(groupId)) {
            return;
        }
        //是否在黑名单
        if (GroupManager.detectBlackList(event)) {
            return;
        }
        //入群申请
        if (SessionConfig.INSTANCE.getRequestSwitch()) {
            GroupManager.userRequestGroup(event);
        }
    }

    /**
     * 退群事件
     */
    @EventHandler
    public void onMessageThree(@NotNull MemberLeaveEvent event) { // 可以抛出任何异常, 将在 handleException 处理
        long groupId = event.getGroupId();
        MemberPermission botPermission = event.getGroup().getBotPermission();
        if (botPermission == MemberPermission.MEMBER) {
            return;
        }
        if (!SessionConfig.INSTANCE.getGroupList().contains(groupId)) {
            return;
        }
        String nick = event.getMember().getNick();
        event.getGroup().sendMessage(String.format("%s离开了我们...", nick));
        if (BlackListData.INSTANCE.isAutoBlackList()) {
            GroupManager.autoAddBlackList(event);
        }
    }


}