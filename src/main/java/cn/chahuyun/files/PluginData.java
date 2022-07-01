package cn.chahuyun.files;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.entity.GroupWelcomeBase;
import cn.chahuyun.entity.ScopeInfoBase;
import cn.chahuyun.entity.SessionDataBase;
import cn.chahuyun.enumerate.DataEnum;
import com.alibaba.fastjson.JSONObject;
import net.mamoe.mirai.console.data.Value;
import net.mamoe.mirai.console.data.java.JavaAutoSavePluginData;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.utils.MiraiLogger;

import java.util.*;

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
     * 删除词
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
     * 群消息欢迎词
     * @param aod t 添加 f 删除
     * @param key 标签
     * @param value 欢迎词内容
     * @return net.mamoe.mirai.message.data.MessageChain
     * @author zhangjiaxing
     * @date 2022/6/21 9:06
     */
    public MessageChain setGroupWelcomeMessage(boolean aod,String key,String value,ScopeInfoBase infoBase) {
        Map<String, String> stringStringMap = this.groupWelcomeMessage.get();
        if (aod) {
            GroupWelcomeBase base = new GroupWelcomeBase(key, value, infoBase);
            String jsonString = JSONObject.toJSONString(base);
            stringStringMap.put(key, jsonString);
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

    /**
     * 获取群欢迎词列表
     * @author zhangjiaxing
     * @param group 群号
     * @date 2022/7/1 11:00
     * @return java.util.List<cn.chahuyun.entity.GroupWelcomeBase>
     */
    public List<GroupWelcomeBase> getGroupWelcomeMessage(Long group) {
        Map<String, String> stringStringMap = groupWelcomeMessage.get();
        //返回指定群的列表
        List<GroupWelcomeBase> groupWelcomeBaseList = new ArrayList<>();
        //判断是否是在该群的欢迎词之内
        for (Map.Entry<String, String> entry : stringStringMap.entrySet()) {
            String value = entry.getValue();
            //序列化
            GroupWelcomeBase base = JSONObject.parseObject(value, GroupWelcomeBase.class);
            //主人
            long owner = ConfigData.INSTANCE.getOwner();
            if (group == owner) {
                groupWelcomeBaseList.add(base);
                //是否是当前群
            }else if (base.getScopeInfo().getType()) {
                if (Objects.equals(base.getScopeInfo().getScopeCode(), group)) {
                    groupWelcomeBaseList.add(base);
                }
            } else {
                //是否是群组
                if (base.getScopeInfo().getGroupType()) {
                    //群组列表
                    List<Long> longs = GroupData.INSTANCE.getGroupList().get(base.getScopeInfo().getScopeNum());
                    //是否在群组内
                    for (Long aLong : longs) {
                        if (Objects.equals(aLong, group)) {
                            groupWelcomeBaseList.add(base);
                        }
                    }
                    //全局
                } else {
                    groupWelcomeBaseList.add(base);
                }
            }

        }
        return groupWelcomeBaseList;
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