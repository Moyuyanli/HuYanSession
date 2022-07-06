package cn.chahuyun.eventManager;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.files.ConfigData;
import cn.chahuyun.manager.ScopeGroupManager;
import cn.chahuyun.manager.TimingManager;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.utils.MiraiLogger;

import java.util.regex.Pattern;

/**
 * FriendMessageEventManager
 *
 * @author Zhangjiaxing
 * @description 好友消息处理
 * @date 2022/6/28 17:31
 */
public class FriendMessageEventManager {

    public static final FriendMessageEventManager INSTANCE = new FriendMessageEventManager();
    private MiraiLogger l = HuYanSession.INSTANCE.getLogger();

    /**
     * 接收到所有好友发送的消息处理
     * @author zhangjiaxing
     * @param event 消息事件
     * @date 2022/6/29 8:40
     */
    public void isMessageType(MessageEvent event) {
        Contact subject = event.getSubject();
        String code = event.getMessage().contentToString();

        long owner = ConfigData.INSTANCE.getOwner();
        if (subject.getId() == owner) {
            //定时任务
            if (Pattern.matches("添加定时任务|%ds", code)) {
                TimingManager.INSTANCE.addTiming(event,0);
            } else if (Pattern.matches("删除定时任务[:：]\\d+|%ds[:：]\\d+",code)) {
                TimingManager.INSTANCE.deleteTiming(event);
            } else if (Pattern.matches("(开启|关闭)定时任务[:：]\\d+|[+-]ds[:：]\\d+", code)) {
                TimingManager.INSTANCE.operateTiming(event);
            } else if (Pattern.matches("查询定时任务|ds[:：](\\d+)?", code)) {
                TimingManager.INSTANCE.checkTiming(event);
            }
            //群组
            if (Pattern.matches("[+-]gr[:：](\\d+)( \\d+)*", code)) {
                if (code.startsWith("+")) {
                    ScopeGroupManager.INSTANCE.addScopeGroup(event);
                } else {
                    ScopeGroupManager.INSTANCE.delScopeGroup(event);
                }
            }else if (Pattern.matches("gr[:：]", code)) {
                ScopeGroupManager.INSTANCE.checkScopeGroup(event);
            }
        }

    }





}