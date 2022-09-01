package cn.chahuyun;

import cn.chahuyun.command.Command;
import cn.chahuyun.config.BlackListData;
import cn.chahuyun.config.ConfigData;
import cn.chahuyun.controller.*;
import cn.chahuyun.event.GroupEventListener;
import cn.chahuyun.event.MessageEventListener;
import cn.chahuyun.utils.HibernateUtil;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.utils.MiraiLogger;
import xyz.cssxsh.mirai.hibernate.MiraiHibernateConfiguration;


/**
 * @author Moyuyanli
 * @description 插件主类
 * @date 2022/6/16 21:35
 */
public final class HuYanSession extends JavaPlugin {

    /**
     * HuYanSession唯一实例
     */
    public static final HuYanSession INSTANCE = new HuYanSession();

    public static final MiraiLogger log = INSTANCE.getLogger();


    private HuYanSession() {
        super(new JvmPluginDescriptionBuilder("cn.chahuyun.HuYanSession", "2.0")
                .name("HuYanSession")
                .info("壶言会话-服务于你的群聊!")
                .author("Moyuyanli")
                .dependsOn("xyz.cssxsh.mirai.plugin.mirai-hibernate-plugin", false)
                .build());
    }


    @Override
    public void onEnable() {
        getLogger().info("HuYanSession2 当前版本: v2.0.0.4-alpha");

        getLogger().info("===================HuYanSession2===================");
        MiraiHibernateConfiguration configuration = new MiraiHibernateConfiguration(this);
        HibernateUtil.init(configuration);

        EventChannel<Event> channel = GlobalEventChannel.INSTANCE.parentScope(HuYanSession.INSTANCE);
        //加载插件，打印日志
        reloadPluginConfig(ConfigData.INSTANCE);
        reloadPluginConfig(BlackListData.INSTANCE);
        getLogger().info("插件配置已加载！");
        if (ConfigData.INSTANCE.getOwner() == 0) {
            getLogger().warning("主人还没有设置，请设置主人!");
        }
        getLogger().info("主人已设置:" + ConfigData.INSTANCE.getOwner());

        CommandManager.INSTANCE.registerCommand(Command.INSTANCE, true);
        getLogger().info("插件指令已加载！");

        ListAction.init(true);
        SessionAction.init(true);
        PowerAction.init(true);
        GroupProhibitedAction.init(true);
        ManySessionAction.init(true);
        QuartzAction.init(true);

        //注册群消息事件
        channel.registerListenerHost(new MessageEventListener());
        getLogger().info("群消息监听已注册！");
        channel.registerListenerHost(new GroupEventListener());
        getLogger().info("群动作监听已注册！");


        getLogger().info("壶言会话插件加载完成!");
        getLogger().info("===================HuYanSession2===================");
    }

    @Override
    public void onDisable() {
        getLogger().info("HuYanSession已卸载!感谢您的使用!");
    }


}