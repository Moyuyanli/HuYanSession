package cn.chahuyun.Session;

import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.message.data.MessageChain;

/**
 * 对话信息检测
 *
 * @author Zhangjiaxing
 * @description 检测对话是否合格
 * @date 2022/6/8 9:34
 */
public class SessionManage {


    public static void SessionDisclose(Event event) {

    }

    /**
     * @description 判断该消息是不是规定字符
     * @author zhangjiaxing
     * @param messageChain 消息链
     * @date 2022/6/8 12:32
     * @return boolean
     */
    public static boolean isString(MessageChain messageChain) {
        return false;
    }


    public static boolean studySession(MessageChain chain) {
        String studyPatten = "(学习 [\\u4e00-\\u9fa5]{1,} [\\u4e00-\\u9fa5]{1,}( ?(精准|模糊|头部|结尾))?)";


        return false;
    }


}