package cn.chahuyun.command;

import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.CompositeCommand;
import net.mamoe.mirai.console.command.descriptor.CommandArgumentContext;
import net.mamoe.mirai.console.permission.Permission;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;

/**
 * 基本指令
 *
 * @author Zhangjiaxing
 * @description 基础指令
 * @date 2022/6/8 9:40
 */
public class CommandManage extends CompositeCommand {

    public CommandManage(JvmPlugin jvmPlugin) {
        super(jvmPlugin,
                "m",
                new String[]{}, "管理命令",
                Permission.getRootPermission(),
                CommandArgumentContext.EMPTY);
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



}