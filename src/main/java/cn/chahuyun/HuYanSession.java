package cn.chahuyun;

import cn.chahuyun.commandManager.CommandManage;
import cn.chahuyun.entity.ScopeInfoBase;
import cn.chahuyun.enumerate.DataEnum;
import cn.chahuyun.eventManager.FriendMessageEventManager;
import cn.chahuyun.eventManager.GroupEventManager;
import cn.chahuyun.eventManager.MessageEventManager;
import cn.chahuyun.files.ConfigData;
import cn.chahuyun.files.GroupData;
import cn.chahuyun.files.PluginData;
import cn.chahuyun.files.TimingData;
import cn.chahuyun.timingManager.TimingManager;
import cn.chahuyun.utils.MessageUtil;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.MemberJoinEvent;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.ArrayList;
import java.util.List;


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
        super(new JvmPluginDescriptionBuilder("cn.chahuyun.HuYanSession", "1.9")
                .name("HuYanSession")
                .info("壶言会话-服务于你的群聊!")
                .author("Moyuyanli")
                .build());
    }



    @Override
    public void onEnable() {

        //加载插件，打印日志
        getLogger().info("================HuYan================");
        getLogger().info("HuYanSession 加载!");

        //加载配置
        this.reloadPluginConfig(ConfigData.INSTANCE);
        getLogger().info("HuYanConfig 已加载！");
        long owner = ConfigData.INSTANCE.getOwner();
        if (owner == 0) {
            getLogger().error("还没有添加主人,请添加主人!");
        } else {
            getLogger().info("主人已设置->"+owner);
        }
        long bot = ConfigData.INSTANCE.getBot();
        if (bot == 0) {
            getLogger().error("还没有添加机器人,请添加机器人后再使用!");
        } else {
            getLogger().info("机器人已设置->"+bot);
        }
        //加载数据
        this.reloadPluginData(GroupData.INSTANCE);
        getLogger().info("GroupData 已加载！");

        this.reloadPluginData(PluginData.INSTANCE);
        getLogger().info("SessionData 已加载！");
        PluginData.INSTANCE.setSessionMap(false, 0, "乒", "乓", new ScopeInfoBase("全局", false,false, null,0), DataEnum.ACCURATE);
        MessageUtil.INSTANCE.init();

        this.reloadPluginData(TimingData.INSTANCE);
        getLogger().info("TimingData 已加载！");
        TimingManager.INSTANCE.init();
        getLogger().info("所有定时器已加载！");

        //注册指令
        CommandManager.INSTANCE.registerCommand(new CommandManage(HuYanSession.INSTANCE), true);
        //群加人检测
        EventChannel<MemberJoinEvent> memberJoinEventEventChannel = GlobalEventChannel.INSTANCE.filterIsInstance(MemberJoinEvent.class)
                .filter(event -> event.getBot().getId() == bot)
                .filter(event -> {
                    List<Long> list = ConfigData.INSTANCE.getGroupList();
                    if (list.size() == 0) {
                        this.getLogger().warning("没有检测群！");
                    }
                    ArrayList<Long> groupList = new ArrayList<Long>(list);
                    for (Long group : groupList) {
                        if( event.getGroup().getId() == group){
                            return true;
                        }
                    }
                    return false;
                });
        getLogger().info("=====================================");
        //监听 群加人事件
        memberJoinEventEventChannel.subscribeAlways(MemberJoinEvent.class, GroupEventManager.INSTANCE::onMemberJoinEvent);


        //消息监听器 监听 bot 的所有消息
        EventChannel<MessageEvent> messageEvent = GlobalEventChannel.INSTANCE.filterIsInstance(MessageEvent.class)
                .filter(event -> event.getBot().getId() == bot);

        /*
         * 监听消息
         * 并发监听，虽说不会冲突，但是会降低一点运行效率
         */
        messageEvent.subscribeAlways(FriendMessageEvent.class, FriendMessageEventManager.INSTANCE::isMessageType);
        messageEvent.subscribeAlways(MessageEvent.class, MessageEventManager.INSTANCE::isMessageWhereabouts);





    }

    @Override
    public void onDisable() {
        getLogger().info("HuYanSession已卸载!");
    }
}