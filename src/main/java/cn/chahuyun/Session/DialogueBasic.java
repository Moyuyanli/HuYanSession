package cn.chahuyun.Session;

import cn.chahuyun.Session.Criticaldialog.Test;
import lombok.Data;
import net.mamoe.mirai.event.AbstractEvent;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.events.GroupMessageEvent;


/**
 * 基本对话事件
 *
 * @author Zhangjiaxing
 * @description 触发基本对话
 * @date 2022/6/8 9:18
 */
@Data
public class DialogueBasic {

    public String message;

    public static void isMessageWhereabouts(GroupMessageEvent messages) {
        Test.pingpang(messages);


    }

}