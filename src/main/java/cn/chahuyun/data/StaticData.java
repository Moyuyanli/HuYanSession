package cn.chahuyun.data;

import cn.chahuyun.entity.*;
import net.mamoe.mirai.Bot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :静态资源，减少数据库调用
 * @Date 2022/7/10 14:56
 */
public class StaticData {


    /**
     * 不同机器人
     * 不同群组
     * 群组信息
     */
    private static Map<Long, Map<Integer, GroupList>> groupListMap;

    /**
     * 不同机器人
     * 不同触发词
     * 消息信息
     */
    private static Map<Long, Map<String, Session>> sessionMap;
    /**
     * 不同机器人
     * 不同触发词
     * 多词条消息
     */
    private static Map<Long, Map<String, ManySessionInfo>> manySession;

    /**
     * 不同机器人
     * 不同人
     * 权限
     */
    private static Map<Long, Map<String, Power>> powerMap;

    /**
     * 不同机器人
     * 不同作用域
     * 多条违禁词
     */
    private static Map<Long, Map<Scope, List<GroupProhibited>>> prohibitedMap;

    private StaticData() {
    }

    public static Map<Integer, GroupList> getGroupListMap(Bot bot) {

        if (groupListMap == null) {
            groupListMap = new HashMap<>();
            return new HashMap<>();
        }
        if (groupListMap.containsKey(bot.getId())) {
            return groupListMap.get(bot.getId());
        }
        return new HashMap<>();
    }

    public static Map<Integer, GroupList> getGroupListMap(long bot) {

        if (groupListMap == null) {
            groupListMap = new HashMap<>();
            return new HashMap<>();
        }
        if (groupListMap.containsKey(bot)) {
            return groupListMap.get(bot);
        }
        return new HashMap<>();
    }

    public static void setGroupListMap(Map<Long, Map<Integer, GroupList>> groupListMap) {
        StaticData.groupListMap = groupListMap;
    }

    public static Map<String, Session> getSessionMap(Bot bot) {
        if (sessionMap == null) {
            sessionMap = new HashMap<>();
            return new HashMap<>();
        }
        if (sessionMap.containsKey(bot.getId())) {
            return sessionMap.get(bot.getId());
        }
        return new HashMap<>();
    }

    public static void setSessionMap(Map<Long, Map<String, Session>> sessionMap) {
        StaticData.sessionMap = sessionMap;
    }


    public static Map<Scope, List<GroupProhibited>> getProhibitedMap(Bot bot) {
        if (prohibitedMap == null) {
            prohibitedMap = new HashMap<>();
        }
        if (prohibitedMap.containsKey(bot.getId())) {
            return prohibitedMap.get(bot.getId());
        }
        return new HashMap<>();
    }

    public static void setProhibitedMap(Map<Long, Map<Scope, List<GroupProhibited>>> prohibitedMap) {
        StaticData.prohibitedMap = prohibitedMap;
    }

    /**
     * 是否存在该会话 key
     *
     * @param bot 所属机器人
     * @param key 键
     * @return boolean
     * @author Moyuyanli
     * @date 2022/7/13 11:03
     */
    public static boolean isSessionKey(Bot bot, String key) {
        if (sessionMap == null) {
            sessionMap = new HashMap<>();
        }
        if (sessionMap.containsKey(bot.getId())) {
            return sessionMap.get(bot.getId()).containsKey(key);
        }
        return false;
    }

    /**
     * 查询是否存在该群组
     *
     * @param bot 所属机器人
     * @param key 群组id
     * @return boolean true 存在
     * @author Moyuyanli
     * @date 2022/8/11 14:15
     */
    public static boolean isGrouper(Bot bot, int key) {
        if (groupListMap.containsKey(bot.getId())) {
            return groupListMap.get(bot.getId()).containsKey(key);
        }
        return false;
    }

    /**
     * 判断有没有对应群
     *
     * @param bot     所属机器人
     * @param key     群组id
     * @param groupId 群号
     * @return boolean true 存在
     */
    public static boolean isGrouper(Bot bot, int key, long groupId) {
        if (groupListMap.containsKey(bot.getId())) {
            if (groupListMap.get(bot.getId()).containsKey(key)) {
                return groupListMap.get(bot.getId()).get(key).containsGroupId(groupId);
            }
            return false;
        }
        return false;
    }


    public static Map<String, Power> getPowerMap(Bot bot) {
        if (powerMap == null) {
            powerMap = new HashMap<>();
            return new HashMap<>();
        }
        if (powerMap.containsKey(bot.getId())) {
            return powerMap.get(bot.getId());
        }
        return new HashMap<>();
    }

    public static void setPowerMap(Map<Long, Map<String, Power>> powerMap) {
        StaticData.powerMap = powerMap;
    }

    /**
     * 获取多词条消息map
     *
     * @param bot 所属机器人
     * @return
     */
    public static Map<String, ManySessionInfo> getManySession(Bot bot) {
        if (manySession == null) {
            manySession = new HashMap<>();
        }
        if (manySession.containsKey(bot.getId())) {
            return manySession.get(bot.getId());
        }
        return new HashMap<>();
    }

    public static void setManySession(Map<Long, Map<String, ManySessionInfo>> manySession) {
        StaticData.manySession = manySession;
    }
}
