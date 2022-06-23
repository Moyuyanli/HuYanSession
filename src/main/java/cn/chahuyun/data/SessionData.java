package cn.chahuyun.data;

import cn.chahuyun.HuYanSession;
import com.alibaba.fastjson.JSONObject;
import net.mamoe.mirai.console.data.Value;
import net.mamoe.mirai.console.data.java.JavaAutoSavePluginData;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.utils.MiraiLogger;

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

    private MiraiLogger l = HuYanSession.INSTANCE.getLogger();

    /**
     * 文件名
     */
    public SessionData() {
        super("SessionData");
    }

    /**
     * list<SessionDataBase> 对话数据集合
     */
    private final Value<Map<String, String>> sessionMap = typedValue("sessionMap", createKType(Map.class, createKType(String.class), createKType(String.class)));

    /**
     * 群欢迎词
     */
    private final Value<Map<String, String>> groupWelcomeMessage = typedValue("groupWelcomeMessage", createKType(Map.class, createKType(String.class), createKType(String.class)));

    /**
     * 黑名单
     */
    private final Value<Map<Long, Long>> blackList = typedValue("blackList", createKType(Map.class, createKType(Long.class), createKType(Long.class)));



    /**
     * @return java.util.Map<java.lang.String, cn.chahuyun.data.SessionDataBase>
     * @description 获取是sessiondatabase的map
     * @author zhangjiaxing
     * @date 2022/6/17 16:37
     */
    public Map<String, SessionDataBase> getSessionMap() {
        //从data中获取map
        Map<String, String> stringStringMap = INSTANCE.sessionMap.get();
        //创建一个用来返回的map
        Map<String, SessionDataBase> stringSessionDataBaseMap = new HashMap<String, SessionDataBase>();
        //进行遍历
        for (String s : stringStringMap.values()) {
            //序列化对象
            SessionDataBase dataBase = JSONObject.parseObject(s, SessionDataBase.class);
            //添加到返回map中
            stringSessionDataBaseMap.put(dataBase.getKey(), dataBase);
        }
        return stringSessionDataBaseMap;
    }


    /**
     * @param s    + or -
     * @param base 基本对话
     * @return net.mamoe.mirai.message.data.MessageChain
     * @description 用于修改本地数据的操作方法
     * @author zhangjiaxing
     * @date 2022/6/20 8:35
     */
    public MessageChain setSessionMap(String s, SessionDataBase base) {
        //取出map
        Map<String, String> stringStringMap = this.sessionMap.get();
        //判断基本学习添加
        switch (s) {
            case "+":
                //序列化后添加
                String jsonString = JSONObject.toJSONString(base);
                stringStringMap.put(base.getKey(), jsonString);
                return new MessageChainBuilder().append("学习成功! ")
                        .append(MiraiCode.deserializeMiraiCode(base.getKey()))
                        .append(" -> ")
                        .append(MiraiCode.deserializeMiraiCode(base.getValue()))
                        .build();
            case "-":
                //查询之后删除
                if (stringStringMap.containsKey(base.getKey())) {
                    stringStringMap.remove(base.getKey());
                    return new MessageChainBuilder().append("删除成功! ")
                            .append(MiraiCode.deserializeMiraiCode(base.getKey()))
                            .build();
                } else {
                    return new MessageChainBuilder().append("没有找到能够删除的东西啦~")
                            .build();
                }
            case "++":
                //如果数据中没有该多词条回复，则先添加
                if (!stringStringMap.containsKey(base.getKey())) {
                    //序列化后添加
                    //将值保存到多词条里面去，并且将轮询基数存到value里面
                    base.setValues(true, base.getValue());
                    base.setValue("0");

                    String jsonToString = JSONObject.toJSONString(base);
                    //添加
                    stringStringMap.put(base.getKey(), jsonToString);
                    return new MessageChainBuilder().append("学习多词条成功! ")
                            .append(MiraiCode.deserializeMiraiCode(base.getKey()))
                            .append(" -> ")
                            .append("多词条回复")
                            .build();
                }else {
                    //如果有，则取出来，然后添加回复
                    Map<String, SessionDataBase> sessionMap = getSessionMap();
                    //取出
                    SessionDataBase sessionDataBase = sessionMap.get(base.getKey());
                    //添加回复
                    MessageChain messages = sessionDataBase.setValues(true, base.getValue());
                    //反序列化然后覆盖数据
                    String toJSONString = JSONObject.toJSONString(sessionDataBase);
                    stringStringMap.put(base.getKey(), toJSONString);
                    return messages;
                }
            case "--":
                if (stringStringMap.containsKey(base.getKey())) {
                    //如果有，则取出来，然后添加回复
                    Map<String, SessionDataBase> sessionMap = getSessionMap();
                    //取出
                    SessionDataBase sessionDataBase = sessionMap.get(base.getKey());
                    //删除回复
                    MessageChain messages = sessionDataBase.setValues(false, base.getValue());
                    //反序列化然后覆盖数据
                    String toJSONString = JSONObject.toJSONString(sessionDataBase);
                    stringStringMap.put(base.getKey(), toJSONString);
                    return messages;
                }
            default:
                return new MessageChainBuilder().append("数据操作出错了哦！")
                        .build();
        }
    }

    /**
     * @description 将轮询次数+1
     * @author zhangjiaxing
     * @param key 触发词
     * @date 2022/6/23 10:26
     */
    public void addPollNum(String key) {
        //获取数据
        Map<String, String> stringStringMap = this.sessionMap.get();
        //找到该条数据
        String SessionDataBaseJson = stringStringMap.get(key);
        //序列化
        SessionDataBase sessionDataBase = JSONObject.parseObject(SessionDataBaseJson, SessionDataBase.class);
        //改变值
        sessionDataBase.setValue(String.valueOf(Integer.parseInt(sessionDataBase.getValue()) + 1));
        //再存进去
        String toJSONString = JSONObject.toJSONString(sessionDataBase);
        stringStringMap.put(key, toJSONString);
    }

    /**
     * @param s       标签
     * @param message 欢迎词
     * @return net.mamoe.mirai.message.data.MessageChain
     * @description 群消息欢迎词
     * @author zhangjiaxing
     * @date 2022/6/21 9:06
     */
    public MessageChain setGroupWelcomeMessage(String s, String message) {
        Map<String, String> stringStringMap = this.groupWelcomeMessage.get();
        String substring = s.substring(0, 1);
        s = s.substring(1);
        if (substring.equals("+")) {
            stringStringMap.put(s, message);
            return new MessageChainBuilder().append("欢迎词添加成功！").build();
        } else {
            if (stringStringMap.containsKey(s)) {
                stringStringMap.remove(s);
                return new MessageChainBuilder().append("欢迎词删除成功！").build();
            } else {
                return new MessageChainBuilder().append("没找到该欢迎词哦~").build();
            }
        }
    }

    public Map<String, String> getGroupWelcomeMessage() {
        return groupWelcomeMessage.get();
    }
}