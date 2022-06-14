package cn.chahuyun.command;

import cn.chahuyun.PluginMain;
import net.mamoe.mirai.console.command.CommandOwner;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.CompositeCommand;
import net.mamoe.mirai.console.command.descriptor.CommandArgumentContext;
import net.mamoe.mirai.console.permission.Permission;
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

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

    @SubCommand("pu")
    @Description("噗~")
    public void pu(CommandSender sender) {
        sender.sendMessage("噗~");
    }



}