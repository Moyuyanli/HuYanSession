package cn.chahuyun;

import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;


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

        String path = getDataFolderPath().toString();
        getLogger().info("p-"+path);


    }

    @Override
    public void onDisable() {
        getLogger().info("HuYanSession已卸载!");
    }
}