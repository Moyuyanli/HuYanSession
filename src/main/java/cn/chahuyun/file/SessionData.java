package cn.chahuyun.file;

import cn.chahuyun.enumerate.DataEnum;
import kotlinx.serialization.Serializable;
import net.mamoe.mirai.console.data.AutoSavePluginData;
import net.mamoe.mirai.message.data.MessageChain;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * SessionData
 * 对话消息数据
 *
 * @author Zhangjiaxing
 * @description
 * @date 2022/6/16 14:25
 */
public class SessionData extends AutoSavePluginData {

    public static final SessionData INSTANCE = new SessionData();

    public SessionData() {
        super("SessionData");
    }

    private ArrayList<SessionDataBase> session;

    public void add(String key, int type, String value, MessageChain messageChain, DataEnum dataEnum) {
        session.add(new SessionDataBase(key, type, value, messageChain, dataEnum));
    }

    public ArrayList<SessionDataBase> getSession() {
        return session;
    }

    public void setSession(ArrayList<SessionDataBase> session) {
        this.session = session;
    }
}