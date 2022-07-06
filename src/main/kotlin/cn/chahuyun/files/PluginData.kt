package cn.chahuyun.files

import cn.chahuyun.HuYanSession
import cn.chahuyun.entity.GroupProhibitBase
import cn.chahuyun.entity.GroupWelcomeBase
import cn.chahuyun.entity.ScopeInfoBase
import cn.chahuyun.entity.SessionDataBase
import cn.chahuyun.enumerate.DataEnum
import cn.chahuyun.files.ConfigData.owner
import cn.chahuyun.files.GroupData.groupList
import com.alibaba.fastjson.JSONObject
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.data.buildMessageChain

/**
 * SessionData
 * 对话消息数据
 *
 * @author Zhangjiaxing
 * @description
 * @date 2022/6/16 14:25
 */
object PluginData : AutoSavePluginData("SessionData") {

    private val l = HuYanSession.INSTANCE.logger

    /**
     * list<SessionDataBase> 对话数据集合
    </SessionDataBase> */
    private val sessionMap: MutableMap<String, String>  by value()


    /**
     * 群欢迎词
     */
    private val groupWelcomeMessage: MutableMap<String, String> by value()

    /**
     * 群禁言词
     */
    val groupProhibitMessage: MutableMap<String, String> by value()

    /**
     * @return java.util.Map<java.lang.String></java.lang.String>, cn.chahuyun.entity.SessionDataBase>
     * @description 获取是sessionDataBase的map
     * @author zhangjiaxing
     * @date 2022/6/17 16:37
     */
    fun loadSessionMap(): Map<String, SessionDataBase> {
        //从data中获取map
        val stringStringMap: Map<String, String> = sessionMap
        //创建一个用来返回的map
        val stringSessionDataBaseMap: MutableMap<String, SessionDataBase> = HashMap()
        //进行遍历
        for (s in stringStringMap.values) {
            //序列化对象
            val dataBase = JSONObject.parseObject(s, SessionDataBase::class.java)
            //添加到返回map中
            stringSessionDataBaseMap[dataBase.key] = dataBase
        }
        return stringSessionDataBaseMap
    }

    /**
     * @return net.mamoe.mirai.message.data.MessageChain
     * @description 用于修改本地数据的操作方法
     * @author zhangjiaxing
     * @date 2022/6/20 8:35
     */
    fun setSessionMap(
        studyType: Boolean,
        contentType: Int,
        key: String,
        value: String?,
        scopeInfoBase: ScopeInfoBase?,
        dataEnum: DataEnum?
    ): MessageChain {
        //取出map
        val stringStringMap = sessionMap
        //判断数据中是否存在
        return if (!stringStringMap.containsKey(key)) {
            //不存在则新建
            val base = SessionDataBase(key, contentType, value, dataEnum, scopeInfoBase)
            val messages: MessageChain
            //如果是多词条，收到调整一下词语结构
            if (studyType) {
                base.setValues(true, value)
                base.value = "多词条回复"
                messages = MessageChainBuilder().append("学习多词条回复成功!").build()
            } else {
                messages = MessageChainBuilder().append("学习触发回复成功!").build()
            }
            val toJSONString = JSONObject.toJSONString(base)
            stringStringMap[key] = toJSONString
            messages
        } else if (studyType) {
            //存在，多词条添加
            //先取
            val s = stringStringMap[key]
            val base = JSONObject.parseObject(s, SessionDataBase::class.java)
            //加一条信息
            val messages = base.setValues(true, value)
            val jsonString = JSONObject.toJSONString(base)
            //然后更新
            stringStringMap[key] = jsonString
            messages
        } else {
            //如果不是多词条，直接新建吧，不是很好判断参数的修改
            val base = SessionDataBase(key, contentType, value, dataEnum, scopeInfoBase)
            val jsonString = JSONObject.toJSONString(base)
            //覆盖
            stringStringMap[key] = jsonString
            MessageChainBuilder().append("修改触发词回复成功!").build()
        }
    }

    /**
     * @param param
     * @return net.mamoe.mirai.message.data.MessageChain
     * 删除词
     * @author zhangjiaxing
     * @date 2022/6/23 22:36
     */
    fun delSessionData(param: String?): MessageChain {
        if (param == null) {
            return MessageChainBuilder().append("多词条回复删除失败！").build()
        }
        //分割
        val s = param.split(" ").toTypedArray()
        //获取map
        val stringStringMap = sessionMap
        //判断是不是多词条
        if (s[0] == "!") {
            //寻找有没有该条多词条回复
            if (stringStringMap.containsKey(s[1])) {
                //有就开始序列化
                val s1 = stringStringMap[s[1]]
                val base = JSONObject.parseObject(s1, SessionDataBase::class.java)
                //然后删除
                val messages = base.setValues(false, s[2])
                val jsonString = JSONObject.toJSONString(base)
                //然后保存
                stringStringMap[s[1]] = jsonString
                return messages
            }
        }
        //普通删除
        if (stringStringMap.containsKey(s[0])) {
            stringStringMap.remove(s[0])
            return MessageChainBuilder().append("删除关键词成功！").build()
        }
        return MessageChainBuilder().append("删除关键词失败！").build()
    }

    /**
     * @param key
     * @return void
     * @description 轮询次数+1
     * @author zhangjiaxing
     * @date 2022/6/23 22:43
     */
    fun addPollNum(key: String): Int {
        //获取map
        val stringStringMap = sessionMap
        //找到该条轮询
        val s = stringStringMap[key]
        val base = JSONObject.parseObject(s, SessionDataBase::class.java)
        //调取+方法
        val poll = base.pollAdd
        val string = JSONObject.toJSONString(base)
        //重新保存
        stringStringMap[key] = string
        return poll
    }

    /**
     * 群消息欢迎词
     * @param aod t 添加 f 删除
     * @param key 标签
     * @param value 欢迎词内容
     * @return net.mamoe.mirai.message.data.MessageChain
     * @author zhangjiaxing
     * @date 2022/6/21 9:06
     */
    fun setGroupWelcomeMessage(aod: Boolean, key: String, value: String?, infoBase: ScopeInfoBase?): MessageChain {
        val stringStringMap = groupWelcomeMessage
        return if (aod) {
            val base = GroupWelcomeBase(key, value, infoBase)
            val jsonString = JSONObject.toJSONString(base)
            stringStringMap[key] = jsonString
            MessageChainBuilder().append("欢迎词添加成功！").build()
        } else {
            if (stringStringMap.containsKey(key)) {
                stringStringMap.remove(key)
                MessageChainBuilder().append("欢迎词删除成功！").build()
            } else {
                MessageChainBuilder().append("没找到该欢迎词哦~").build()
            }
        }
    }

    /**
     * 获取群欢迎词列表
     * @author zhangjiaxing
     * @param group 群号
     * @date 2022/7/1 11:00
     * @return java.util.List<cn.chahuyun.entity.GroupWelcomeBase>
    </cn.chahuyun.entity.GroupWelcomeBase> */
    fun getGroupWelcomeMessage(group: Long): List<GroupWelcomeBase> {
        val stringStringMap: Map<String, String> = groupWelcomeMessage
        //返回指定群的列表
        val groupWelcomeBaseList: MutableList<GroupWelcomeBase> = ArrayList()
        //判断是否是在该群的欢迎词之内
        for ((_, value) in stringStringMap) {
            //序列化
            val base = JSONObject.parseObject(value, GroupWelcomeBase::class.java)
            //主人
            val owner = owner
            if (group == owner) {
                groupWelcomeBaseList.add(base)
                //是否是当前群
            } else if (base.scopeInfo.type) {
                if (base.scopeInfo.scopeCode == group) {
                    groupWelcomeBaseList.add(base)
                }
            } else {
                //是否是群组
                if (base.scopeInfo.groupType) {
                    //群组列表
                    val longs: List<Long> = groupList[base.scopeInfo.scopeNum]!!
                    //是否在群组内
                    for (aLong in longs) {
                        if (aLong == group) {
                            groupWelcomeBaseList.add(base)
                        }
                    }
                    //全局
                } else {
                    groupWelcomeBaseList.add(base)
                }
            }
        }
        return groupWelcomeBaseList
    }

    /**
     * @description 添加多词条回复
     * @author zhangjiaxing
     * @param key 键
     * @param list 列表
     * @date 2022/6/27 9:38
     * @return net.mamoe.mirai.message.data.MessageChain
     */
    fun addPolyglotMessage(key: String, list: List<String?>?): MessageChain {
        val s = sessionMap[key]
        val base = JSONObject.parseObject(s, SessionDataBase::class.java)
        val b = base.values.addAll(list!!)
        val jsonString = JSONObject.toJSONString(base)
        sessionMap[key] = jsonString
        return if (b) {
            MessageChainBuilder().append("多词条回复添加成功!").build()
        } else {
            MessageChainBuilder().append("多词条回复添加失败!").build()
        }
    }

    /**
     * 添加和删除禁言词
     * @author zhangjiaxing
     * @param aod t 添加 f 删除
     * @param key 标识
     * @param base 禁言词实体类
     * @date 2022/7/6 16:51
     * @return 消息
     */
    fun operateGroupProhibitMessage(aod: Boolean, key: String, base: GroupProhibitBase): MessageChain {

        return if (aod) {
            val jsonString = JSONObject.toJSONString(base)
            groupProhibitMessage[key] = jsonString
            buildMessageChain { +"$key 禁言词添加成功!" }
        } else {
            val containsKey = groupProhibitMessage.containsKey(key)
            if (!containsKey) {
                return buildMessageChain { +"没有找到该禁言词 $key !" }
            }
            groupProhibitMessage.remove(key)
            buildMessageChain { +"$key 禁言词删除成功!" }
        }
    }


    /**
     * 获取禁言词map
     * @author zhangjiaxing
     * @date 2022/7/6 17:00
     * @return 禁言词map
     */
    fun loadGroupProhibitMessage(): Map<String, GroupProhibitBase> {
        val groupProhibitBase : MutableMap<String,GroupProhibitBase> = HashMap()
        for (entry in groupProhibitMessage) {
            val parseObject = JSONObject.parseObject(entry.value,GroupProhibitBase::class.java)
            groupProhibitBase[entry.key] = parseObject
        }
        return groupProhibitBase
    }


}