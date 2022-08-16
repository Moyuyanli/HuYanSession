package cn.chahuyun.utils;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.data.StaticData;
import cn.chahuyun.entity.GroupList;
import cn.chahuyun.entity.GroupProhibited;
import cn.chahuyun.entity.Scope;
import cn.chahuyun.files.ConfigData;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.utils.MiraiLogger;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GroupProhibitedUtil
 * 违禁词工具
 *
 * @author Zhangjiaxing
 * @date 2022/8/16 14:19
 */
public class GroupProhibitedUtil {

    private final static MiraiLogger l = HuYanSession.INSTANCE.getLogger();

    /**
     * 初始化或加载违禁词
     *
     * @param type true 初始化 false 加载
     * @author Moyuyanli
     * @date 2022/8/16 15:20
     */
    public static void init(boolean type) {
        List<GroupProhibited> groupProhibiteds = null;
        try {
            groupProhibiteds = HibernateUtil.factory.fromTransaction(session -> {
                HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
                JpaCriteriaQuery<GroupProhibited> query = builder.createQuery(GroupProhibited.class);
                JpaRoot<GroupProhibited> from = query.from(GroupProhibited.class);
                query.select(from);
                List<GroupProhibited> list = session.createQuery(query).list();
                for (GroupProhibited groupProhibited : list) {
                    if (groupProhibited.getScopeInfo() == null) {
                        Scope scope = ScopeUtil.getScope(groupProhibited.getScopeMark());
                        groupProhibited.setScopeInfo(scope);
                    }
                }
                return list;
            });
        } catch (Exception e) {
            l.error("出错啦~", e);
        }

        StaticData.setProhibitedMap(parseList(groupProhibiteds));

        if (type) {
            l.info("数据库会话信息初始化成功!");
            return;
        }
        if (ConfigData.INSTANCE.getDebugSwitch()) {
            l.info("会话数据更新成功!");
        }

    }


    public void addProhibited(MessageEvent event) {
        //+wjc:1 (body) [3h|gr1|%(重设回复消息)|ch|jy|hmd3]
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        Bot bot = event.getBot();

        String[] split = code.split("\\\\:");

        String string = split[1];


    }



    /**
     * 解析违禁词数组
     *
     * @param prohibitedList 违禁词list
     * @return java.util.Map<java.lang.Long,java.util.Map<cn.chahuyun.entity.Scope,java.util.List<cn.chahuyun.entity.GroupProhibited>>>
     * @author Moyuyanli
     * @date 2022/8/16 15:19
     */
    private static Map<Long, Map<Scope, List<GroupProhibited>>> parseList(List<GroupProhibited> prohibitedList) {
        if (prohibitedList == null || prohibitedList.isEmpty()) {
            return null;
        }
        Map<Long, Map<Scope, List<GroupProhibited>>> listMap = new HashMap<>();

        for (GroupProhibited entity : prohibitedList) {
            long bot = entity.getBot();
            Scope scope = entity.getScopeInfo();

            if (!listMap.containsKey(bot)) {
                listMap.put(bot, new HashMap<>() {{
                    put(scope, new ArrayList<>(){{add(entity);}});
                }});
                continue;
            }
            if (!listMap.get(bot).containsKey(scope)) {
                listMap.get(bot).get(scope).add(entity);
            }
        }
        return listMap;
    }


}