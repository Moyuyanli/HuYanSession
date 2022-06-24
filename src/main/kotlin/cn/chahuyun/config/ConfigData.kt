package cn.chahuyun.config

import cn.chahuyun.HuYanSession
import com.alibaba.fastjson.JSONArray
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
    /**
     * 机器人识别
     */
    @ValueDescription("机器人识别qq")
    val bot: Long  by value()
    @ValueDescription("是否开启斗地主帮助")
    val douSwitch: Boolean by value(false)
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
     * 权限存储识别法
     */
    @ValueDescription("权限识别")
    val powerList: MutableMap<String, String> by value()

    /**
     * @return java.util.Map<java.lang.String></java.lang.String>, cn.chahuyun.config.PowerConfigBase>
     * @description 获取权限map
     * @author zhangjiaxing
     * @date 2022/6/19 0:51
     */
    fun pickPowerList(): Map<String, PowerConfigBase> {
        val stringPowerConfigBaseHashMap = HashMap<String, PowerConfigBase>()
        val stringStringMap: Map<String, String> = powerList
        for (key in stringStringMap.keys) {
            stringPowerConfigBaseHashMap[key] = JSONArray.parseObject(
                stringStringMap[key], PowerConfigBase::class.java)
        }
        return stringPowerConfigBaseHashMap
    }

    /**
     * @param s     修改类型
     * @param user  用户匹配
     * @param power 权限
     * @return net.mamoe.mirai.message.data.MessageChain
     * @description 根据传递消息进行权限的修改
     * @author zhangjiaxing
     * @date 2022/6/19 2:43
     */
    fun setAdminList(s: String, user: String, power: String): MessageChain {
        //创建返回消息构造器
        val messages = MessageChainBuilder()
        //获取3类数据 添加or删除  用户识别符  权限
        //判断
        return if (s == "+") {
            //添加权限，直接新建然后覆盖，就可以不用从本地重新获取
            val base = PowerConfigBase(user)
            when (power) {
                "admin" -> {
                    base.isAdminPower = true
                    messages.append("权限管理员权限添加成功！")
                }
                "session" -> {
                    base.isSessionPower = true
                    messages.append("会话管理员权限添加成功！")
                }
                "group" -> {
                    base.isGroupPower = true
                    messages.append("群管理员权限添加成功！")
                }
                "all" -> {
                    base.isAdminPower = true
                    base.isSessionPower = true
                    base.isGroupPower = true
                    messages.append("管理员添加成功！")
                }
                else -> messages.append("添加失败，未识别权限！")
            }
            val jsonString = JSONArray.toJSONString(base)
            //添加or覆盖
            powerList[user] = jsonString
            HuYanSession.INSTANCE.logger.info("添加权限: $user $power")
            messages.build()
        } else {
            //先从本地获取数据
            val baseMap: Map<String, String> = powerList
            //创建一个空权限base
            var base: PowerConfigBase? = null
            //查找本地有没有该用户的权限base
            for (k in baseMap.keys) {
                if (k == user) {
                    base = JSONArray.parseObject(baseMap[k], PowerConfigBase::class.java)
                }
            }
            //如果没有，直接返回失败
            if (base == null) {
                messages.append("删除权限失败，没有找到该用户！")
                return messages.build()
            }
            when (power) {
                "admin" -> {
                    base.isAdminPower = false
                    messages.append("权限管理员权限删除成功！")
                }
                "session" -> {
                    base.isSessionPower = false
                    messages.append("会话管理员权限删除成功！")
                }
                "group" -> {
                    base.isGroupPower = false
                    messages.append("群管理员权限删除成功！")
                }
                "all" -> {
                    //当删除全部权限的时候，直接删除该用户的权限base
                    powerList.remove(user)
                    messages.append("管理员删除成功！")
                    messages.append("删除失败，未识别权限！")
                }
                else -> messages.append("删除失败，未识别权限！")
            }
            val jsonString = JSONArray.toJSONString(base)
            //覆盖
            powerList[user] = jsonString
            HuYanSession.INSTANCE.logger.info("删除权限: $user $power")
            messages.build()
        }
    }

    fun setGroupList(operate: Boolean, group: Long): MessageChain {
        return if (operate) {
            groupList.add(group)
            MessageChainBuilder().append("群" + group + "检测添加成功!").build()
        } else {
            try {
                groupList.remove(group)
                MessageChainBuilder().append("群" + group + "检测删除成功!").build()
            } catch (e: Exception) {
                MessageChainBuilder().append("没有该群!").build()
            }
        }
    }

}