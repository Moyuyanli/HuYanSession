package cn.chahuyun.session.command

import cn.chahuyun.session.config.SessionConfig
import cn.chahuyun.session.HuYanSession
import cn.chahuyun.session.manage.DataManager
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand

object SessionCommand : CompositeCommand(
    HuYanSession.INSTANCE, "hy",
    description = "壶言指令"
) {
    @SubCommand("transfer")
    @Description("转移bot数据")
    suspend fun CommandSender.transfer(bot: Long, toBot: Long) {
        val instant = Bot.findInstance(toBot)
        if (instant == null) {
            sendMessage("接收信息bot不存在!")
            return
        }
        DataManager.transferInfo(bot, toBot)
    }

    @SubCommand("owner") // 可以设置多个子指令名。此时函数名会被忽略。
    @Description("设置主人")
    suspend fun CommandSender.setOwner(owner: Long) {
        SessionConfig.owner = owner
        sendMessage("主人设置成功!")
    }

    @SubCommand("aGroup") // 可以设置多个子指令名。此时函数名会被忽略。
    @Description("添加检测群")
    suspend fun CommandSender.addGroup(group: Long) {
        val messageChain = SessionConfig.setGroupList(true, group)
        sendMessage(messageChain)
    }

    @SubCommand("dGroup") // 可以设置多个子指令名。此时函数名会被忽略。
    @Description("删除测群")
    suspend fun CommandSender.deleteGroup(group: Long) {
        val messageChain = SessionConfig.setGroupList(false, group)
        sendMessage(messageChain)
    }

    @SubCommand("v") // 可以设置多个子指令名。此时函数名会被忽略。
    @Description("查询当前插件版本")
    suspend fun CommandSender.version() {
        val version = HuYanSession.VERSION
        sendMessage("壶言会话当前版本: $version")
    }

}