package cn.chahuyun;

import cn.chahuyun.Session.DialogueBasic;
import cn.chahuyun.command.CommandManage;
import cn.chahuyun.config.ConfigData;
import cn.chahuyun.data.ScopeInfo;
import cn.chahuyun.data.SessionData;
import cn.chahuyun.enumerate.DataEnum;
import cn.chahuyun.groupManager.GroupEventManager;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.MemberJoinEvent;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.ArrayList;


/**
 * @description 插件主类
 * @author zhangjiaxing
 * @date 2022/6/16 21:35
 */
public final class HuYanSession extends JavaPlugin {

    /**
     * HuYanSession唯一实例
     */
    public static final HuYanSession INSTANCE = new HuYanSession();

    private HuYanSession() {
        super(new JvmPluginDescriptionBuilder("cn.chahuyun.HuYanSession", "1.6")
                .name("HuYanSession")
                .info("壶言会话-服务于你的群聊!")
                .author("Moyuyanli")
                .build());
    }



    @Override
    public void onEnable() {
        //加载插件，打印日志
        getLogger().info("HuYanSession 加载!");

        //加载配置
        this.reloadPluginConfig(ConfigData.INSTANCE);
        getLogger().info("SessionConfig 已加载！");
        Long owner = ConfigData.INSTANCE.getOwner();
        if (owner == null || owner == 0) {
            getLogger().error("还没有添加主人,请添加主人!");
        } else {
            getLogger().info("主人已设置->"+owner);
        }
        Long bot = ConfigData.INSTANCE.getBot();
        if (bot == null || bot == 0) {
            getLogger().error("还没有添加机器人,请添加机器人后再使用!");
        } else {
            getLogger().info("机器人已设置->"+bot);
        }
        //加载数据
        this.reloadPluginData(SessionData.INSTANCE);
        getLogger().info("SessionData 已加载！");
        //添加默认信息
        SessionData.INSTANCE.setSessionMap(false, 0, "乒", "乓", new ScopeInfo("全局", false, 0L), DataEnum.ACCURATE);

        //注册指令
        CommandManager.INSTANCE.registerCommand(new CommandManage(HuYanSession.INSTANCE), true);
        //群加人检测
        EventChannel<MemberJoinEvent> memberJoinEventEventChannel = GlobalEventChannel.INSTANCE.filterIsInstance(MemberJoinEvent.class)
                .filter(event -> event.getBot().getId() == bot)
                .filter(event -> {
                    ArrayList<Long> groupList = (ArrayList<Long>) ConfigData.INSTANCE.getGroupList();
                    for (Long group : groupList) {
                        if( event.getGroup().getId() == group){
                            return true;
                        }
                    }
                    return false;
                });

        //监听 群加人事件
        memberJoinEventEventChannel.subscribeAlways(MemberJoinEvent.class, GroupEventManager.INSTANCE::onMemberJoinEvent);


        //消息监听器 监听 bot 的所有消息
        EventChannel<MessageEvent> messageEvent = GlobalEventChannel.INSTANCE.filterIsInstance(MessageEvent.class)
                .filter(event -> event.getBot().getId() == bot);

        //监听消息
//        messageEvent.subscribeAlways(MessageEvent.class, DialogueBasic::isMessageWhereabouts);
        messageEvent.subscribeAlways(MessageEvent.class, DialogueBasic.INSTANCE::isMessageWhereabouts);


    }

    @Override
    public void onDisable() {
    }
}