package cn.chahuyun.command

import cn.chahuyun.HuYanSession
import cn.chahuyun.files.ConfigData
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand

object Command : CompositeCommand(
    HuYanSession.INSTANCE,"hy",
    description = "壶言指令"
) {

    @SubCommand("owner") // 可以设置多个子指令名。此时函数名会被忽略。
    @Description("添加检测群")
    suspend fun CommandSender.setOwner(owner:Long) {
        ConfigData.owner = owner
        sendMessage("主人设置成功!")
    }

    @SubCommand("aGroup") // 可以设置多个子指令名。此时函数名会被忽略。
    @Description("添加检测群")
    suspend fun CommandSender.addGroup(group :Long) {
        val messageChain = ConfigData.setGroupList(true, group)
        sendMessage(messageChain)
    }

    @SubCommand("dGroup") // 可以设置多个子指令名。此时函数名会被忽略。
    @Description("删除测群")
    suspend fun CommandSender.deleteGroup(group :Long) {
        val messageChain = ConfigData.setGroupList(false, group)
        sendMessage(messageChain)
    }

}