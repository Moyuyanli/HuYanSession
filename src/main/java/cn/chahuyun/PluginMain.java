package cn.chahuyun;

import cn.chahuyun.Session.DialogueBasic;
import cn.chahuyun.command.CommandManage;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.events.GroupMessageEvent;



public final class PluginMain extends JavaPlugin {

    public static final PluginMain INSTANCE = new PluginMain();

    private PluginMain() {
        super(new JvmPluginDescriptionBuilder("cn.chahuyun.PluginMain", "1.0-SNAPSHOT")
                .name("Group Session Console")
                .info("用于服务群的一个综合功能性插件")
                .author("Moyuyanli")
                .build());
    }



    @Override
    public void onEnable() {
        //加载插件，打印日志
        getLogger().info("Group Session Console 加载!");

        CommandManager.INSTANCE.registerCommand(new CommandManage(PluginMain.INSTANCE), true);


        //监听群消息
        GlobalEventChannel.INSTANCE.subscribe(GroupMessageEvent.class, event -> {
            DialogueBasic.isMessageWhereabouts(event);
            return ListeningStatus.LISTENING;
        });




    }

}