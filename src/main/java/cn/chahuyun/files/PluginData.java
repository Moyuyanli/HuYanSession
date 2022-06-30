package cn.chahuyun.files;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.entity.ScopeInfoBase;
import cn.chahuyun.entity.SessionDataBase;
import cn.chahuyun.enumerate.DataEnum;
import com.alibaba.fastjson.JSONObject;
import net.mamoe.mirai.console.data.Value;
import net.mamoe.mirai.console.data.java.JavaAutoSavePluginData;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.utils.MiraiLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SessionData
 * 对话消息数据
 *
 * @author Zhangjiaxing
 * @description
 * @date 2022/6/16 14:25
 */
public class PluginData extends JavaAutoSavePluginData {

    /**
     * 唯一构造
     */
    public static final PluginData INSTANCE = new PluginData();
    /**
     * 文件名
     */
    public PluginData() {
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

    private MiraiLogger l = HuYanSession.INSTANCE.getLogger();

    /**
     * @return java.util.Map<java.lang.String, cn.chahuyun.entity.SessionDataBase>
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
     * @return net.mamoe.mirai.message.data.MessageChain
     * @description 用于修改本地数据的操作方法
     * @author zhangjiaxing
     * @date 2022/6/20 8:35
     */
    public MessageChain setSessionMap(boolean studyType, int contentType, String key, String value, ScopeInfoBase scopeInfoBase, DataEnum dataEnum) {
        //取出map
        Map<String, String> stringStringMap = this.sessionMap.get();
        //判断数据中是否存在
        if (!stringStringMap.containsKey(key)) {
            //不存在则新建
            SessionDataBase base = new SessionDataBase(key, contentType, value, dataEnum, scopeInfoBase);
            MessageChain messages;
            //如果是多词条，收到调整一下词语结构
            if (studyType) {
                base.setValues(true, value);
                base.setValue("多词条回复");
                messages = new MessageChainBuilder().append("学习多词条回复成功!").build();
            } else {
                messages = new MessageChainBuilder().append("学习触发回复成功!").build();
            }
            String toJSONString = JSONObject.toJSONString(base);
            stringStringMap.put(key, toJSONString);
            return messages;
        } else if (studyType) {
            //存在，多词条添加
            //先取
            String s = stringStringMap.get(key);
            SessionDataBase base = JSONObject.parseObject(s, SessionDataBase.class);
            //加一条信息
            MessageChain messages = base.setValues(true, value);
            String jsonString = JSONObject.toJSONString(base);
            //然后更新
            stringStringMap.put(key, jsonString);
            return messages;
        } else {
            //如果不是多词条，直接新建吧，不是很好判断参数的修改
            SessionDataBase base = new SessionDataBase(key, contentType, value, dataEnum, scopeInfoBase);
            String jsonString = JSONObject.toJSONString(base);
            //覆盖
            stringStringMap.put(key, jsonString);
            return new MessageChainBuilder().append("修改触发词回复成功!").build();
        }
    }


    /**
     * @param param
     * @return net.mamoe.mirai.message.data.MessageChain
     * @description 删除词
     * @author zhangjiaxing
     * @date 2022/6/23 22:36
     */
    public MessageChain delSessionData(String param) {
        if (param == null) {
            return new MessageChainBuilder().append("多词条回复删除失败！").build();
        }
        //分割
        String[] s = param.split(" ");
        //获取map
        Map<String, String> stringStringMap = this.sessionMap.get();
        //判断是不是多词条
        if (s[0].equals("!")) {
            //寻找有没有该条多词条回复
            if (stringStringMap.containsKey(s[1])) {
                //有就开始序列化
                String s1 = stringStringMap.get(s[1]);
                SessionDataBase base = JSONObject.parseObject(s1, SessionDataBase.class);
                //然后删除
                MessageChain messages = base.setValues(false, s[2]);
                String jsonString = JSONObject.toJSONString(base);
                //然后保存
                stringStringMap.put(s[1], jsonString);
                return messages;
            }
        }
        //普通删除
        if (stringStringMap.containsKey(s[0])) {
            stringStringMap.remove(s[0]);
            return new MessageChainBuilder().append("删除关键词成功！").build();
        }
        return new MessageChainBuilder().append("删除关键词失败！").build();
    }

    /**
     * @param key
     * @return void
     * @description 轮询次数+1
     * @author zhangjiaxing
     * @date 2022/6/23 22:43
     */
    public int addPollNum(String key) {
        //获取map
        Map<String, String> stringStringMap = this.sessionMap.get();
        //找到该条轮询
        String s = stringStringMap.get(key);
        SessionDataBase base = JSONObject.parseObject(s, SessionDataBase.class);
        //调取+方法
        int poll = base.getPollAdd();
        String string = JSONObject.toJSONString(base);
        //重新保存
        stringStringMap.put(key, string);
        return poll;
    }

    /**
     * @param aod t 添加 f 删除
     * @param message 欢迎词
     * @return net.mamoe.mirai.message.data.MessageChain
     * @description 群消息欢迎词
     * @author zhangjiaxing
     * @date 2022/6/21 9:06
     */
    public MessageChain setGroupWelcomeMessage(boolean aod, String message) {
        Map<String, String> stringStringMap = this.groupWelcomeMessage.get();
        String[] strings = message.split(":");
        String key = strings[0];
        if (aod) {
            stringStringMap.put(key, message);
            return new MessageChainBuilder().append("欢迎词添加成功！").build();
        } else {
            if (stringStringMap.containsKey(key)) {
                stringStringMap.remove(key);
                return new MessageChainBuilder().append("欢迎词删除成功！").build();
            } else {
                return new MessageChainBuilder().append("没找到该欢迎词哦~").build();
            }
        }
    }

    public Map<String, String> getGroupWelcomeMessage() {
        return groupWelcomeMessage.get();
    }

    /**
     * @description 添加多词条回复
     * @author zhangjiaxing
     * @param key 键
     * @param list 列表
     * @date 2022/6/27 9:38
     * @return net.mamoe.mirai.message.data.MessageChain
     */
    public MessageChain addPolyletMessage(String key, List<String> list) {
        String s = this.sessionMap.get().get(key);

        SessionDataBase base = JSONObject.parseObject(s, SessionDataBase.class);
        boolean b = base.getValues().addAll(list);

        String jsonString = JSONObject.toJSONString(base);
        this.sessionMap.get().put(key, jsonString);

        if (b) {
            return new MessageChainBuilder().append("多词条回复添加成功!").build();
        } else {
            return new MessageChainBuilder().append("多词条回复添加失败!").build();
        }
    }

}