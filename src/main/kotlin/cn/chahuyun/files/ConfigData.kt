package cn.chahuyun.files


import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageChainBuilder


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
    val owner: Long by value()

    @ValueDescription("是否开启斗地主帮助")
    val douSwitch: Boolean by value()
    @ValueDescription("是否开启详细帮助链接提示")
    val linkSwitch: Boolean  by value()
    @ValueDescription("是否开启帮助在群内显示")
    val helpSwitch: Boolean  by value()

    /**
     * 群号
     */
    @ValueDescription("群管操作检测群号")
    val groupList: MutableList<Long> by value()

    /**
     * 添加检测群
     * @author zhangjiaxing
     * @param operate true为+ f -
     * @param group 群号
     * @date 2022/6/30 19:23
     * @return 消息
     */
    fun setGroupList(operate: Boolean, group: Long): MessageChain {
        return if (operate) {
            groupList.add(group)
            MessageChainBuilder().append("群  $group 检测添加成功!").build()
        } else {
            try {
                groupList.remove(group)
                MessageChainBuilder().append("群 $group 检测删除成功!").build()
            } catch (e: Exception) {
                MessageChainBuilder().append("没有该群!").build()
            }
        }
    }

}