package cn.chahuyun.utils;

import net.mamoe.mirai.event.events.MessageEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ShareUtils
 *
 * @author Zhangjiaxing
 * @description 公共工具包
 * @date 2022/7/29 12:40
 */
public class ShareUtils {

    private ShareUtils(){}

    /**
     * 返回是否为退出
     * @author Moyuyanli
     * @param event 消息事件
     * @date 2022/7/29 12:43
     * @return boolean true 退出
     */
    public static boolean isQuit(MessageEvent event) {
        String messagePattern = "^!!!|^！！！";
        Pattern pattern = Pattern.compile(messagePattern);
        Matcher matcher = pattern.matcher(event.getMessage().serializeToMiraiCode());
        return matcher.find();
    }


}