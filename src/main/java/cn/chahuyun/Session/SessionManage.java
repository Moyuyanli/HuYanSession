package cn.chahuyun.Session;

import cn.chahuyun.GroupSession;
import cn.chahuyun.data.SessionDataBase;
import cn.chahuyun.data.SessionDataPaging;
import cn.chahuyun.enumerate.DataEnum;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.utils.MiraiLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import static cn.chahuyun.GroupSession.sessionData;

/**
 * 对话信息检测
 *
 * @author Zhangjiaxing
 * @description 检测对话是否合格
 * @date 2022/6/8 9:34
 */
public class SessionManage {

    private static MiraiLogger l = GroupSession.INSTANCE.getLogger();

    private static final int PAGE_SIZE = 5;

    /**
     * 学习正则
     */
    public static String studyPattern = "(学习 [\\w\\u4e00-\\u9fa5]+ [\\w\\u4e00-\\u9fa5]+( ?(精准|模糊|头部|结尾))?)";
    /**
     * 查询正则
     */
    public static String queryPattern = "((查询) ?([\\d\\u4e00-\\u9fa5])*)";

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

    /**
     * @description 学习词汇的方法
     * @author zhangjiaxing
     * @param event 消息事件
     * @date 2022/6/16 20:56
     * @return boolean
     */
    public static boolean studySession(MessageEvent event) {
        String messageString = event.getMessage().contentToString();
        Contact subject = event.getSubject();

        //判断学习语法结构是否正确
        if (!Pattern.matches(studyPattern, messageString)) {
            subject.sendMessage("学习失败！学习结构应为：");
            subject.sendMessage("学习 (触发关键词) (回复内容) [精准|模糊|头部|结尾]");
            return false;
        }

        String[] strings = messageString.split(" ");
        l.info(Arrays.toString(strings));
        if (strings.length == 3 && !strings[2].equals("图片")) {
            //type = 0 为string类回复
            sessionData.put(strings[1],new SessionDataBase(strings[1],0,strings[2],null, DataEnum.ACCURATE));
            subject.sendMessage("学习成功! " + strings[1] + "->"+strings[2]);
            return true;
        }


        return false;
    }

    /**
     * @description 查询所有词汇的方法
     * @author zhangjiaxing
     * @param event 消息事件
     * @date 2022/6/16 20:57
     * @return boolean
     */
    public static boolean querySession(MessageEvent event) {
        String messageString = event.getMessage().contentToString();
        Contact subject = event.getSubject();

        //判断查询语法结构是否正确
        if (!Pattern.matches(queryPattern, messageString)) {
            subject.sendMessage("查询失败！查询结构应为：");
            subject.sendMessage("查询 [页数/关键词]");
            return false;
        }
        //当 消息仅为 查询 时 默认查询第一页消息
        if (messageString.length()<=3) {
            l.info("报错1");
            sessionMessageInInt(1, event);
            return true;
        }
        //当消息 携带页数时
        String[] strings = messageString.split("");
        if (Pattern.matches("\\d", strings[1])) {
            l.info(strings.toString());
            int i = Integer.parseInt(strings[1]);
            sessionMessageInInt(i, event);
            return true;
        }else {

        }

        return false;
    }

    /**
     * @description 根据传递的数量来进行分页显示
     * @author zhangjiaxing
     * @param pageNum 当前页数
     * @param event 消息事件
     * @date 2022/6/16 22:08
     * @return java.lang.Boolean
     */
    private static Boolean sessionMessageInInt(int pageNum, MessageEvent event) {
        //进行分页
        SessionDataPaging dataPaging = SessionDataPaging.queryPageInfo(pageNum, PAGE_SIZE, new ArrayList<>(sessionData.values()));
        //构造消息
        MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
        messageChainBuilder.add("------所有关键词------\n");
        //循环添加内容
        ArrayList<SessionDataBase> list = dataPaging.getList();
        for (SessionDataBase base : list) {
            messageChainBuilder.add("\t"+ base.getKey()+" -> "+ base.getValue()+"\t:"+ base.getDataEnum().getType()+"\n");
        }
        messageChainBuilder.add("------- （"+dataPaging.getPageNum()+"/"+dataPaging.getPageAll()+"） -------");
        //发送消息
        event.getSubject().sendMessage(messageChainBuilder.build());
        return false;

    }

}