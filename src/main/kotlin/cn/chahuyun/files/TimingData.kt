package cn.chahuyun.files

import cn.chahuyun.entity.TimingTaskBase
import com.alibaba.fastjson2.JSONObject
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

object TimingData : AutoSavePluginData("TimingTask") {

    private val timingList : MutableMap<String,String> by value()


    fun setTimingList(base: TimingTaskBase) {
        val toJSONString = JSONObject.toJSONString(base)
        timingList[base.name] = toJSONString
    }


}