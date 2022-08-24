package cn.chahuyun.config


import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText


/**
 * 说明
 * 用于管理添加指令管理员
 * 用于判断是否拥有改权限
 *
 * @author Moyuyanli
 * @Description :各类指令权限
 * @Date 2022/6/18 23:33
 */
object BlackListData : AutoSavePluginConfig("blackListConfig") {

    @ValueDescription("是否开启退群自动加入黑名单")
    val isAutoBlackList:Boolean by value()
    @ValueDescription("显示理由")
    val AutoBlackListReason: String by value("退群自动加入黑名单！")


}