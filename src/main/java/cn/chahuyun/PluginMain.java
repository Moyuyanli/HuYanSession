package cn.chahuyun;

import cn.chahuyun.Session.DialogueBasic;
import cn.chahuyun.command.CommandManage;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;


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

        //消息监听器 监听 2061954151 的所有消息
        EventChannel<MessageEvent> messageEvent = GlobalEventChannel.INSTANCE.filterIsInstance(MessageEvent.class)
                .filter(event -> event.getBot().getId() == 2061954151L);


        //监听群消息
        messageEvent.subscribeAlways(GroupMessageEvent.class, DialogueBasic::isMessageWhereabouts);


        //监听好友消息
        messageEvent.subscribeAlways(FriendMessageEvent.class, friendMessageEvent -> {
            String message = friendMessageEvent.getMessage().serializeToMiraiCode();
            if (message.equals("噗~")) {
                friendMessageEvent.getSender().sendMessage("噗~");
            }
        });


    }

}