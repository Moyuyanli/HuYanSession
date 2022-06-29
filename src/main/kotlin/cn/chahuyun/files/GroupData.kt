package cn.chahuyun.files

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.Value
import net.mamoe.mirai.console.data.java.JavaAutoSavePluginData
import net.mamoe.mirai.console.data.value

object GroupData : AutoSavePluginData("GroupData") {

    /**
     * 黑名单
     */
    private val blackList: Map<String , String> by value()

    /**
     * 群组信息
     */
    private val groupList : Map<String , List<String>> by value()


}