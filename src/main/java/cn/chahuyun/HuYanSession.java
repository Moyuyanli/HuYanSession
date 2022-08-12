package cn.chahuyun;

import cn.chahuyun.command.Command;
import cn.chahuyun.files.ConfigData;
import cn.chahuyun.utils.HibernateUtil;
import cn.chahuyun.utils.ListUtil;
import cn.chahuyun.utils.SessionUtil;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;
import xyz.cssxsh.mirai.hibernate.MiraiHibernateConfiguration;


/**
 * @description 插件主类
 * @author Moyuyanli
 * @date 2022/6/16 21:35
 */
public final class HuYanSession extends JavaPlugin {

    /**
     * HuYanSession唯一实例
     */
    public static final HuYanSession INSTANCE = new HuYanSession();

    private HuYanSession() {
        super(new JvmPluginDescriptionBuilder("cn.chahuyun.HuYanSession", "2.0")
                .name("HuYanSession")
                .info("壶言会话-服务于你的群聊!")
                .author("Moyuyanli")
                .build());
    }



    @Override
    public void onEnable() {
        getLogger().info("===================HuYanSession===================");
        MiraiHibernateConfiguration configuration = new MiraiHibernateConfiguration(this);
        HibernateUtil.init(configuration);

        GlobalEventChannel.INSTANCE.parentScope(HuYanSession.INSTANCE);
        //加载插件，打印日志
        reloadPluginConfig(ConfigData.INSTANCE);
        getLogger().info("配置config已加载！");

        CommandManager.INSTANCE.registerCommand(Command.INSTANCE,true);
        getLogger().info("指令command已加载！");

        ListUtil.init(true);
        SessionUtil.init(true);

        //注册群消息事件
        GlobalEventChannel.INSTANCE.registerListenerHost(new cn.chahuyun.event.GroupMessageEvent());
        getLogger().info("群消息监听已注册！");




        getLogger().info("壶言会话插件加载完成!");
        getLogger().info("===================HuYanSession===================");
    }

    @Override
    public void onDisable() {
        getLogger().info("HuYanSession已卸载!感谢您的使用!");
    }


}