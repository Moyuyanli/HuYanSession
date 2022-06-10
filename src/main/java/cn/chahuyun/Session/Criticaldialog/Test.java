package cn.chahuyun.Session.Criticaldialog;


import cn.chahuyun.PluginMain;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.utils.MiraiLogger;


/**
 * 乒乓
 *
 * @author Zhangjiaxing
 * @description 用于测试关键词反应是否成功的类
 * @date 2022/6/8 9:16
 */
public class Test {

    public static void pingpang(GroupMessageEvent event) {
//        msg.plus("乓");
        String test = "乒";
        String message = event.getMessage().serializeToMiraiCode();
        MiraiLogger logger = PluginMain.INSTANCE.getLogger();


        if (message.equals(test)) {
            event.getGroup().sendMessage("乓");
        }
    }

}