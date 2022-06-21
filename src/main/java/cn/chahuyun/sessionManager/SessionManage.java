package cn.chahuyun.sessionManager;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.config.PowerConfig;
import cn.chahuyun.data.ScopeInfo;
import cn.chahuyun.data.SessionData;
import cn.chahuyun.data.SessionDataBase;
import cn.chahuyun.data.SessionDataPaging;
import cn.chahuyun.enumerate.DataEnum;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.utils.MiraiLogger;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 对话信息检测
 *
 * @author Zhangjiaxing
 * @description 检测对话是否合格
 * @date 2022/6/8 9:34
 */
public class SessionManage {

    public static final SessionManage INSTANCE = new SessionManage();

    private MiraiLogger l = HuYanSession.INSTANCE.getLogger();

    /**
     * 学习正则
     */
    public String studyPattern = "(学习\\s+[\\d\\w\\S\\u4e00-\\u9fa5]+\\s+[\\d\\w\\S\\u4e00-\\u9fa5]+(\\s?(精准|模糊|头部|结尾))?(\\s?(当前|全局)?))";
    /**
     * 查询正则
     */
    public String queryPattern = "(查询 ?([\\d\\w\\S\\u4e00-\\u9fa5])*)";
    /**
     * 删除正则
     */
    public String deletePattern = "(删除 ?([\\d\\w\\S\\u4e00-\\u9fa5])*)";


    /**
     * @description 学习词汇的方法
     * @author zhangjiaxing
     * @param event 消息事件
     * @date 2022/6/16 20:56
     * @return boolean
     */
    public boolean studySession(MessageEvent event) {
        String messageString = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();

        //判断学习语法结构是否正确
        if (!Pattern.matches(studyPattern, messageString)) {
            subject.sendMessage("学习失败!学习结构应为：");
            subject.sendMessage("学习 (触发关键词) (回复内容) [精准|模糊|头部|结尾]");
            return false;
        }

        //过滤换行
        messageString = messageString.replace("\\n", " ");

        //分割学习数据
        String[] strings = messageString.split("\\s+");
        String key  = strings[1];
        String value = strings[2];
        //匹配类型
        DataEnum dataEnum = DataEnum.ACCURATE;
        ScopeInfo scopeInfo = new ScopeInfo("当前", true, event.getSubject().getId());
        if (strings.length >= 4) {
            String matches = strings[3];
            switch (matches) {
                case "模糊":
                    dataEnum = DataEnum.VAGUE;break;
                case "头部":
                    dataEnum = DataEnum.START;break;
                case "结尾":
                    dataEnum = DataEnum.END;break;
                case "全局":
                    //如果省略参数只填写匹配范围，用来识别匹配范围
                    scopeInfo = new ScopeInfo("全局", false, null);
                default:break;
            }
        }
        //匹配范围
        if (strings.length == 5) {
            Long owner = PowerConfig.INSTANCE.getOwner();
            if (event.getSender().getId() == owner && strings[4].equals("全局")) {
                scopeInfo = new ScopeInfo("全局", false, null);
            }
        }
        //关键词类型 0-string 1其他
        int type = 0;
        if (Pattern.matches("\\[mirai:(\\w)+:[{}\\d\\w-.]+\\]",value)) {
            type = 1;
        }

        l.info(Arrays.toString(strings));
        //新建对话信息
        SessionDataBase base = new SessionDataBase(key, type, value, dataEnum, scopeInfo);
        //put进存储数据
        MessageChain returnMessChain = SessionData.INSTANCE.setSessionMap("+", base);
        subject.sendMessage(returnMessChain);
        return true;
    }



    /**
     * @description 查询所有词汇的方法
     * @author zhangjiaxing
     * @param event 消息事件
     * @date 2022/6/16 20:57
     * @return boolean
     */
    public boolean querySession(MessageEvent event) {
        String messageString = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();

        //判断查询语法结构是否正确
        if (!Pattern.matches(queryPattern, messageString)) {
            subject.sendMessage("查询失败!查询结构应为：");
            subject.sendMessage("查询 [页数/关键词]");
            return false;
        }
        //当 消息仅为 查询 时 默认查询第一页消息
        if (messageString.length()<=3) {
            SessionDataPaging.INSTANCE.checkSessionList(event);
            return true;
        }

        //关键词查询
        String[] strings = messageString.split(" ");
        Map<String, SessionDataBase> sessionDataBaseMap = SessionData.INSTANCE.getSessionMap();
        if (strings.length >= 2 && sessionDataBaseMap.containsKey(strings[1])) {
            subject.sendMessage(new MessageChainBuilder().append("查询成功! ")
                    .append(MiraiCode.deserializeMiraiCode(strings[1]))
                    .append(" -> ")
                    .append(MiraiCode.deserializeMiraiCode(sessionDataBaseMap.get(strings[1]).getValue()))
                    .build());
            return true;
        } else {
            subject.sendMessage("查询失败! 没有找到我能说的话！");
        }


        return false;
    }


    /**
     * @description 删除一个关键词
     * @author zhangjiaxing
     * @param event 消息触发事件
     * @date 2022/6/17 19:44
     * @return java.lang.Boolean
     */
    public Boolean deleteSession(MessageEvent event) {
        String messageString = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        //删除语法结构查询
        if (!Pattern.matches(deletePattern, messageString)) {
            subject.sendMessage("删除失败!删除结构为:");
            subject.sendMessage("删除 (触发类容)");
        }
        //分割
        String[] strings = messageString.split(" ");
        //只需要关键词的key数组中有这个词就行，map不需要知道它在那
        if (strings.length == 2) {
            //随便建一个，只要携带key
            SessionDataBase base = new SessionDataBase(strings[1], 0, null, null, null);
            MessageChain messageChain = SessionData.INSTANCE.setSessionMap("-", base);
            subject.sendMessage(messageChain);
            return true;
        }
        return false;
    }

}