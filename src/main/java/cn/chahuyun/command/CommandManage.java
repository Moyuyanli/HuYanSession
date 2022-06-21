package cn.chahuyun.command;

import cn.chahuyun.config.PowerConfig;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.java.JCompositeCommand;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;

/**
 * 基本指令
 *
 * @author Zhangjiaxing
 * @description 基础指令
 * @date 2022/6/8 9:40
 */
public class CommandManage extends JCompositeCommand {

    public CommandManage(JvmPlugin jvmPlugin) {
        super(jvmPlugin,
                "hy", "壶言管理命令");
    }

    /**
     * @description
     * @author zhangjiaxing
     * @param sender
     * @date 2022/6/15 17:03
     * @return void
     */
    @SubCommand("pu")
    @Description("噗~")
    public void pu(CommandSender sender) {
        sender.sendMessage("噗~");
    }

    @SubCommand({"power"})
    @Description("设置他人添加管理权限")
    public void powerToOther(CommandSender sender,String s ,long group ,long qq ,String power) {
        String user = "m" + group + "." + qq;
        PowerConfig.INSTANCE.setAdminList(s, user, power);
    }

}