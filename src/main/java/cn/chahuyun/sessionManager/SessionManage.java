package cn.chahuyun.sessionManager;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.config.PowerConfig;
import cn.chahuyun.data.ScopeInfo;
import cn.chahuyun.data.SessionData;
import cn.chahuyun.data.SessionDataBase;
import cn.chahuyun.data.SessionDataPaging;
import cn.chahuyun.enumerate.DataEnum;
import cn.chahuyun.utils.MessageUtil;
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
    private final MessageUtil messageUtil = MessageUtil.INSTANCE;

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
        Contact subject = event.getSubject();
        String code = event.getMessage().serializeToMiraiCode();
        //验证格式
        boolean studyCommand = messageUtil.isStudyCommand(event);
        if (studyCommand) {
            subject.sendMessage("学习失败!需要帮助请发送帮助!");
            return false;
        }
        //取参添加
        Map<String, Object> param = messageUtil.spotStudyCommandParam(event);
        /*
          studyType 学习类型
          contentType 数据保存类型
          scopeInfo 作用域
          dataEnum 匹配机制
          key k
          value v
         */
        boolean studyType = (boolean) param.get("studyType");
        int contentType = (int) param.get("contentType");
        String key = (String) param.get("key");
        String value = (String) param.get("value");
        DataEnum dataEnum = (DataEnum) param.get("dataEnum");
        ScopeInfo scopeInfo = (ScopeInfo) param.get("scopeInfo");
        MessageChain messages = SessionData.INSTANCE.setSessionMap(studyType, contentType, key, value, scopeInfo, dataEnum);
        subject.sendMessage(messages);
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
            subject.sendMessage("查询 [关键词]");
            return false;
        }
        //当 消息仅为 查询 时 默认查询第一页消息
        if (messageString.length()<=3) {
            SessionDataPaging.INSTANCE.checkSessionList(event);
            return true;
        }

        if (!Pattern.matches("查询 [\\d\\w\\S\\u4e00-\\u9fa5]+", messageString)) {
            return false;
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



        return false;
    }

}