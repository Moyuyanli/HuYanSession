package cn.chahuyun.session.utils;

import cn.chahuyun.session.data.StaticData;
import cn.chahuyun.session.entity.GroupList;
import net.mamoe.mirai.Bot;

import java.util.Map;

/**
 * list 的工具
 *
 * @author Moyuyanli
 * @Date 2022/9/1 23:18
 */
public class ListUtil {

    /**
     * 判断这个群组是否存在
     *
     * @param bot    所属机器人
     * @param listId 群组编号
     * @return boolean 不存在 true
     * @author Moyuyanli
     * @date 2022/7/11 12:13
     */
    public static boolean isContainsList(Bot bot, String listId) {
        Map<String, GroupList> groupListMap;
        try {
            groupListMap = StaticData.getGroupListMap(bot);
            if (groupListMap == null || groupListMap.isEmpty()) {
                return true;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            return true;
        }
        return !groupListMap.containsKey(listId);
    }

}
