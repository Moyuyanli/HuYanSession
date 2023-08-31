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
object SessionConfig : AutoSavePluginConfig("config") {

    /**
     * 主人识别
     */
    @ValueDescription("主人qq")
    var owner: Long by value()

    /**
     * 多个bot之间是否互相响应
     */
    @ValueDescription("多个bot之间是否互相响应")
    val botsSwitch: Boolean by value(false)

    /**
     * 入群申请开关
     */
    @ValueDescription("入群申请开关")
    val requestSwitch: Boolean by value(true)


    /**
     * 动态消息变量符号
     */
    @ValueDescription("动态消息的变量符号")
    val variableSymbol: String by value("$")

    @ValueDescription("图片消息本地缓存")
    val localCache : Boolean by value(false)

    @ValueDescription("是否开启DEBUG级别日志显示(不建议开启)")
    val debugSwitch: Boolean by value()

    @ValueDescription("异常输出消息开关")
    val exceptionSwitch: Boolean by value(false)

    @ValueDescription("刷屏消息的时间间隔(单位:秒)")
    val matchingNumber: Int by value(2)

    @ValueDescription("最大刷屏次数")
    val screen: Int by value(10)

    @ValueDescription("刷屏禁言时间(秒)")
    val forbiddenTime: Int by value(60)


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

    @ValueDescription("数据库类型:H2(H2/MYSQL/SQLITE)")
    var databaseType : BaseType by value(BaseType.H2)

    @ValueDescription("MySQL连接默认库为huyan，用户root")
    var mysqlPwd : String by value("123456")

    enum class BaseType{
        H2,SQLITE,MYSQL
    }

}