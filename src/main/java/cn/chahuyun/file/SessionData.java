package cn.chahuyun.file;

import cn.chahuyun.GroupSession;
import cn.chahuyun.data.SessionDataBase;
import cn.chahuyun.enumerate.DataEnum;
import com.alibaba.fastjson.JSONArray;
import net.mamoe.mirai.console.data.Value;
import net.mamoe.mirai.console.data.java.JavaAutoSavePluginData;
import net.mamoe.mirai.message.data.MessageChain;

import java.util.HashMap;
import java.util.Map;

/**
 * SessionData
 * 对话消息数据
 *
 * @author Zhangjiaxing
 * @description
 * @date 2022/6/16 14:25
 */
public class SessionData extends JavaAutoSavePluginData {

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
    private final Value<Map<String,String>> sessionMap = typedValue("sessionMap",createKType(Map.class,createKType(String .class),createKType(String.class)));

    public final Value<Map<String, Map<String, Object>>> mapValue = typedValue("mapvalue", createKType(
            Map.class,createKType(String.class), createKType(Map.class, createKType(String.class), createKType(Object.class))
    ));


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
        SessionDataBase dataBase = new SessionDataBase(key, type, value, messageChain, dataEnum);
        String toJSONString = JSONArray.toJSONString(dataBase);
        Map<String, String> stringStringMapMap = INSTANCE.sessionMap.get();
        stringStringMapMap.put(key, toJSONString);
    }

    /**
     * @description 获取是sessiondatabase的map
     * @author zhangjiaxing
     * @date 2022/6/17 16:37
     * @return java.util.Map<java.lang.String,cn.chahuyun.data.SessionDataBase>
     */
    public Map<String, SessionDataBase> getSessionMap() {
        //从data中获取map
        Map<String, String> stringStringMap = INSTANCE.sessionMap.get();
        //创建一个用来返回的map
        Map<String, SessionDataBase> stringSessionDataBaseMap = new HashMap<String, SessionDataBase>();
        //进行遍历
        for (String s : stringStringMap.values()) {
            //反序列化
            SessionDataBase dataBase = JSONArray.parseObject(s, SessionDataBase.class);
            //添加到返回map中
            stringSessionDataBaseMap.put(dataBase.getKey(), dataBase);
        }
        return stringSessionDataBaseMap;
    }


    /**
     * @description 用来存将数据时SessionDataBase的map存入的方法
     * @author zhangjiaxing
     * @param parMap SessionDataBase的map
     * @date 2022/6/17 16:48
     * @return boolean
     */
    public boolean setSessionMap(Map<String, SessionDataBase> parMap) {
        try {
            //data中的stringmap
            Map<String, String> stringStringMap = INSTANCE.sessionMap.get();
            //循环添加
            for (SessionDataBase dataBase : parMap.values()) {
                //序列化
                String toJSONString = JSONArray.toJSONString(dataBase);
                stringStringMap.put(dataBase.getKey(), toJSONString);
            }
            return true;
        } catch (Exception e) {
            GroupSession.INSTANCE.getLogger().error("数据保存出错 ->"+e.getMessage());
            return false;
        }
    }


}