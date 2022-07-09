package cn.chahuyun;

import cn.chahuyun.files.ConfigData;
import cn.chahuyun.utils.SqliteUtil;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.GlobalEventChannel;


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

        //加载插件，打印日志
        getLogger().info("================HuYan================");
        reloadPluginConfig(ConfigData.INSTANCE);
        getLogger().info("config已加载！");

        SqliteUtil.INSTANCE.init();
        getLogger().info("sqlite数据库已加载！");

        //注册群消息事件
        GlobalEventChannel.INSTANCE.registerListenerHost(new cn.chahuyun.event.GroupMessageEvent());





    }

    @Override
    public void onDisable() {
        getLogger().info("HuYanSession已卸载!");
    }
}