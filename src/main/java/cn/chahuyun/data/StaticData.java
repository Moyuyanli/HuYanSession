package cn.chahuyun.data;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.entity.GroupList;
import cn.chahuyun.entity.Session;
import net.mamoe.mirai.utils.MiraiLogger;

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


    private final static MiraiLogger l = HuYanSession.INSTANCE.getLogger();


    /**
     * 不同机器人的群组map
     */
    private static Map<Long, Map<Integer, GroupList>> groupListMap;

    /**
     * 不同机器人的session map
     */
    private static Map<Long, Map<String, Session>> sessionMap;

    private StaticData() {
    }

    public static Map<Integer, GroupList> getGroupListMap(Long bot) {
        return groupListMap.get(bot);
    }

    public static void setGroupListMap(Map<Long, Map<Integer, GroupList>> groupListMap) {
        StaticData.groupListMap = groupListMap;
    }

    public static Map<String, Session> getSessionMap(Long bot) {
        return sessionMap.get(bot);
    }

    public static void setSessionMap(Map<Long, Map<String, Session>> sessionMap) {
        StaticData.sessionMap = sessionMap;
    }


    /**
     * 是否存在该 key
     * @author Moyuyanli
     * @param bot 所属机器人
     * @param key 键
     * @date 2022/7/13 11:03
     * @return boolean
     */
    public static boolean isSessionKey(long bot, String key) {
        if (sessionMap.containsKey(bot)) {
            return sessionMap.get(bot).containsKey(key);
        }
        return false;
    }

}
