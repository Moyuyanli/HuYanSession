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
    /**
     * 当前插件版本
     */
    public static final String VERSION = "2.2.5";

    private HuYanSession() {
        super(new JvmPluginDescriptionBuilder("cn.chahuyun.HuYanSession", VERSION)
                .name("HuYanSession")
                .info("壶言会话-服务于你的群聊!")
                .author("Moyuyanli")
                .dependsOn("xyz.cssxsh.mirai.plugin.mirai-hibernate-plugin", false)
                .build());
    }


    @Override
    public void onEnable() {
        log.info("===================HuYanSession2===================");
        log.info("    //    / /      \\\\    / /                   //   ) )                                                 ");
        log.info("   //___ / /        \\\\  / /  ___       __     ((         ___      ___      ___     ( )  ___       __    ");
        log.info("  / ___   / //   / / \\\\/ / //   ) ) //   ) )    \\\\     //___) ) ((   ) ) ((   ) ) / / //   ) ) //   ) ) ");
        log.info(" //    / / //   / /   / / //   / / //   / /       ) ) //         \\ \\      \\ \\    / / //   / / //   / /  ");
        log.info("//    / / ((___( (   / / ((___( ( //   / / ((___ / / ((____   //   ) ) //   ) ) / / ((___/ / //   / /   ");
        log.info("HuYanSession2 当前版本: " + "v"+VERSION);
        MiraiHibernateConfiguration configuration = new MiraiHibernateConfiguration(this);
        HibernateUtil.init(configuration);
        EventChannel<Event> channel = GlobalEventChannel.INSTANCE.parentScope(HuYanSession.INSTANCE);
        //加载插件，打印日志
        reloadPluginConfig(ConfigData.INSTANCE);
        reloadPluginConfig(BlackListData.INSTANCE);
        getLogger().info("插件配置已加载！");
        if (ConfigData.INSTANCE.getOwner() == 0) {
            getLogger().warning("主人还没有设置，请设置主人!");
        } else {
            log.info("主人已设置:" + ConfigData.INSTANCE.getOwner());
        }

        CommandManager.INSTANCE.registerCommand(Command.INSTANCE, true);
        log.info("插件指令已加载！");

        ListAction.init(true);
        SessionAction.init(true);
        PowerAction.init(true);
        GroupProhibitedAction.init(true);
        ManySessionAction.init(true);
        QuartzAction.init();

        //注册群消息事件
        channel.registerListenerHost(new MessageEventListener());
        log.info("群消息监听已注册！");
        channel.registerListenerHost(new GroupEventListener());
        log.info("群动作监听已注册！");


        log.info("壶言会话插件加载完成!");
        log.info("===================HuYanSession2===================");
    }

    @Override
    public void onDisable() {
        getLogger().info("HuYanSession已卸载!感谢您的使用!");
    }


}