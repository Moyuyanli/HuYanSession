package cn.chahuyun.files

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.Value
import net.mamoe.mirai.console.data.java.JavaAutoSavePluginData
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.buildMessageChain

object GroupData : AutoSavePluginData("GroupData") {

    /**
     * 黑名单
     */
    private val blackList: MutableMap<String , String> by value()

    /**
     * 群组信息
     */
    private val groupList : MutableMap<Int , MutableList<Long>> by value()

    /**
     * 添加群组
     * @author zhangjiaxing
     * @param index 群组编号
     * @param group 群号
     * @date 2022/6/30 19:19
     * @return 消息
     */
    fun addGroupList(index: Int, group: Long): MessageChain {
        return if (!groupList.containsKey(index)) {
            groupList[index] = mutableListOf(group)
            buildMessageChain { +"群组 $index 创建并添加群 $group 成功！" }
        } else {
            try {
                groupList[index]?.add(group)
                buildMessageChain { +"群组 $index 添加群 $group 成功！" }
            } catch (e: Exception) {
                buildMessageChain { +"群组 $index 添加群 $group 失败！" }
            }
        }
    }

    /**
     * 删除群组
     * @author zhangjiaxing
     * @param index 群组编号
     * @param group 群号
     * @date 2022/6/30 19:21
     * @return 消息
     */
    fun delGroupList(index: Int, group: Long): MessageChain {
        return if (groupList.containsKey(index)) {
            if (group != null) {
                groupList[index]?.remove(group)
                buildMessageChain { +"群组 $index 删除群 $group 成功！" }
            }else{
                groupList.remove(index)
                buildMessageChain { +"群组 $index 删除成功！" }
            }
        }else{
            buildMessageChain { +"删除失败，没有找到该群组!" }
        }
    }


}