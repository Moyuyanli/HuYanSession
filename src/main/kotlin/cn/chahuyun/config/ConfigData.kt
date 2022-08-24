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
object ConfigData : AutoSavePluginConfig("config") {

    /**
     * 主人识别
     */
    @ValueDescription("主人qq")
    var owner: Long by value()

    /**
     * 动态消息变量符号
     */
    @ValueDescription("动态消息的变量符号")
    val variableSymbol: String by value("$")

    @ValueDescription("是否开启DEBUG级别日志显示(不建议开启)")
    val debugSwitch:Boolean by value()
    @ValueDescription("刷屏消息的时间间隔(单位:秒)")
    val matchingNumber: Int by value(2)
    @ValueDescription("最大刷屏次数")
    val screen:Int by value(10)


    /**
     * 群号
     */
    @ValueDescription("群管操作检测群号")
    val groupList: MutableList<Long> by value()

    /**
     * 添加检测群
     * @author Moyuyanli
     * @param operate true为+ f -
     * @param group 群号
     * @date 2022/6/30 19:23
     * @return 消息
     */
    fun setGroupList(operate: Boolean, group: Long): Message {
        return if (operate) {
            groupList.add(group)
            PlainText("群  $group 检测添加成功!")
        } else {
            try {
                groupList.remove(group)
                PlainText("群 $group 检测删除成功!")
            } catch (e: Exception) {
                PlainText("没有该群!")
            }
        }
    }

}