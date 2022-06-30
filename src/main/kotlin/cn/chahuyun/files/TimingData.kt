package cn.chahuyun.files

import cn.chahuyun.entity.TimingTaskBase
import com.alibaba.fastjson2.JSONObject
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.buildMessageChain

object TimingData : AutoSavePluginData("TimingTask") {

    private val timingList : MutableMap<Int,String> by value()

    /**
     * 添加定时任务
     * @author zhangjiaxing
     * @param base 定时任务实体
     * @date 2022/6/30 19:24
     */
    fun addTimingList(base: TimingTaskBase) {
        val toJSONString = JSONObject.toJSONString(base)
        var index: Int
        if (timingList.isEmpty()) {
            index = 0
        }
        index = timingList.size
        timingList[index] = toJSONString
    }

    /**
     * 取定时任务的map
     * @author zhangjiaxing
     * @date 2022/6/30 19:25
     * @return 定时任务列表map
     */
    fun readTimingList(): MutableMap<Int,TimingTaskBase> {
        val rTimingList:MutableMap<Int,TimingTaskBase> = mutableMapOf()
        for (entry in timingList) {
            val base = JSONObject.parseObject(entry.value, TimingTaskBase::class.java)
            rTimingList[entry.key] = base
        }
        return rTimingList
    }

    /**
     * 删除定时任务
     * @author zhangjiaxing
     * @param null
     * @date 2022/6/30 19:26
     * @return 消息
     */
    fun deleteTimingList(index:Int):MessageChain{
        return if (timingList.containsKey(index)) {
            timingList.remove(index)
            buildMessageChain { +"定时器 $index 删除成功！" }
        }else{
            buildMessageChain { +"没有找到该定时器！" }
        }
    }

    /**
     * 获取最新定时任务编号
     * @author zhangjiaxing
     * @date 2022/6/30 19:26
     * @return 定时任务编号
     */
    fun getTimingNum():Int{
        return timingList.size
    }

}