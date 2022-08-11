package cn.chahuyun.data;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.entity.GroupList;
import cn.chahuyun.entity.Session;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.utils.MiraiLogger;

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

    public static Map<Integer, GroupList> getGroupListMap(Bot bot) {
        try {
            return groupListMap.get(bot.getId());
        } catch (Exception e) {
            return null;
        }
    }

    public static void setGroupListMap(Map<Long, Map<Integer, GroupList>> groupListMap) {
        StaticData.groupListMap = groupListMap;
    }

    public static Map<String, Session> getSessionMap(Bot bot) {
        try {
            return sessionMap.get(bot.getId());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setSessionMap(Map<Long, Map<String, Session>> sessionMap) {
        StaticData.sessionMap = sessionMap;
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
     * @param bot 所属机器人
     * @param key 群组id
     * @param groupId 群号
     * @return boolean true 存在
     */
    public static boolean isGrouper(Bot bot, int key,long groupId) {
        if (groupListMap.containsKey(bot.getId())) {
            if (groupListMap.get(bot.getId()).containsKey(key)) {
                return groupListMap.get(bot.getId()).get(key).containsGroupId(groupId);
            }
            return false;
        }
        return false;
    }

}
