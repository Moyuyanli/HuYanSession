package cn.chahuyun.manage;

import cn.hutool.core.util.ClassUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.Set;

/**
 * 数据管理 导出 导入 excel
 * 数据转移 所属 机器人的信息转移
 *
 * @author Moyuyanli
 * @date 2022/10/11 14:57
 */
public class DataManager {

    //todo 所属 机器人的信息转移

    //todo 导出 导入 excel

    /**
     * 导出数据
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/10/16 17:22
     */
    public static void outputData(MessageEvent event) {
        Contact subject = event.getSubject();

        Set<Class<?>> entity = ClassUtil.scanPackage("entity");
        

    }


}
