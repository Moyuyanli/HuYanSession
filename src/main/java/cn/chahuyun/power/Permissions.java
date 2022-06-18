package cn.chahuyun.power;

import cn.chahuyun.GroupSession;
import cn.chahuyun.config.PowerConfig;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.utils.MiraiLogger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :权限操作类
 * @Date 2022/6/19 3:50
 */
public class Permissions {

    public static final Permissions INSTANCE = new Permissions();

    private MiraiLogger l = GroupSession.INSTANCE.getLogger();

    /**
     * @description 用于处理权限消息的字符串数组
     * @author zhangjiaxing
     * @param event 消息事件
     * @date 2022/6/19 1:18
     * @return void
     */
    public void messageToPower(MessageEvent event) {
        //先将发送的消息转换为string
        String string = event.getMessage().contentToString();
        l.info(string);
        //修改类型
        String s = string.substring(0, 1);
        //去掉 修改类型
        string = string.substring(1);
        //通过>分割
        String[] split = string.split(" ");
        //权限类参数
        String power = split[1];
        //匹配正则,获取中段消息的所有数字，疑似qq
        Matcher matcher = Pattern.compile("(@\\d+)").matcher(split[0]);
        //这里需要先进行一次匹配，find
        matcher.find();
        //然后才能通关group获取,然后把@给截取掉
        String qq = matcher.group().substring(1);
        //拼接权限类识别用户字符
        String user = "m"+event.getSubject().getId()+"."+qq;
        //进行设置
        MessageChain messages = PowerConfig.INSTANCE.setAdminList(s,user,power);
        //返回消息
        event.getSubject().sendMessage(messages);
    }



}
