package cn.chahuyun;

import cn.chahuyun.Session.DialogueBasic;
import cn.chahuyun.command.CommandManage;
import cn.chahuyun.data.SessionDataBase;
import cn.chahuyun.enumerate.DataEnum;
import cn.chahuyun.file.SessionData;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.ArrayList;


/**
 * @description 插件主类
 * @author zhangjiaxing
 * @date 2022/6/16 21:35
 */
public final class GroupSession extends JavaPlugin {

    /**
     * 唯一实例GroupSession
     */
    public static final GroupSession INSTANCE = new GroupSession();

    public static SessionData sessionData;

    private GroupSession() {
        super(new JvmPluginDescriptionBuilder("cn.chahuyun.GroupSession", "1.0-SNAPSHOT")
                .name("Group Session Console")
                .info("用于服务群的一个综合功能性插件")
                .author("Moyuyanli")
                .build());
    }



    @Override
    public void onEnable() {
        //加载插件，打印日志
        getLogger().info("Group Session Console 加载!");

        //加载数据
        sessionData = SessionData.INSTANCE;
        this.reloadPluginData(sessionData);
        getLogger().info("SessionData 已加载！");
        if (sessionData.getSession() == null) {
            sessionData.setSession(new ArrayList<SessionDataBase>(){
                {
                    add(new SessionDataBase("乒", 0, "乓", null, DataEnum.ACCURATE));
                }
            });
        }


        //注册指令
        CommandManager.INSTANCE.registerCommand(new CommandManage(GroupSession.INSTANCE), true);

        //消息监听器 监听 2061954151 的所有消息
        EventChannel<MessageEvent> messageEvent = GlobalEventChannel.INSTANCE.filterIsInstance(MessageEvent.class)
                .filter(event -> event.getBot().getId() == 2061954151L);



        //监听消息
        messageEvent.subscribeAlways(MessageEvent.class, DialogueBasic::isMessageWhereabouts);


    }

    @Override
    public void onDisable() {
    }
}