package cn.chahuyun.file;

import cn.chahuyun.data.SessionDataBase;
import cn.chahuyun.enumerate.DataEnum;
import net.mamoe.mirai.console.data.AutoSavePluginData;
import net.mamoe.mirai.message.data.MessageChain;

import java.util.ArrayList;
import java.util.List;

/**
 * SessionData
 * 对话消息数据
 *
 * @author Zhangjiaxing
 * @description
 * @date 2022/6/16 14:25
 */
public class SessionData extends AutoSavePluginData {

    /**
     * 唯一构造
     */
    public static final SessionData INSTANCE = new SessionData();

    /**
     * 文件名
     */
    public SessionData() {
        super("SessionData");
    }

    /**
     * list<SessionDataBase> 对话数据集合
     */
    private List<SessionDataBase> session;

    /**
     * @description 添加对话数据方法
     * @author zhangjiaxing
     * @param key 触发词
     * @param type 触发类型 0-string回复 1-其他回复
     * @param value string回复词
     * @param messageChain 其他回复所有类
     * @param dataEnum 匹配类型
     * @date 2022/6/16 21:12
     */
    public void add(String key, int type, String value, MessageChain messageChain, DataEnum dataEnum) {
        session.add(new SessionDataBase(key, type, value, messageChain, dataEnum));
    }

    public List<SessionDataBase> getSession() {
        return session;
    }

    public void setSession(ArrayList<SessionDataBase> session) {
        this.session = session;
    }
}