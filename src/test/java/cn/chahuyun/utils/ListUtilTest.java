package cn.chahuyun.utils;

import cn.chahuyun.data.StaticData;
import cn.chahuyun.entity.GroupList;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ListUtilTest {

    @Test
    void init() {

        Map<Long, Map<Integer, GroupList>> parseList = HibernateUtil.factory.fromTransaction(session -> {
            //创建构造器
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            //创建实体对应查询器
            JpaCriteriaQuery<GroupList> query = builder.createQuery(GroupList.class);

            JpaRoot<GroupList> from = query.from(GroupList.class);

            query.select(from);
            query.where(builder.equal(from.get("groups").get("bot"), from.get("bot")));
            query.where(builder.equal(from.get("groups").get("list_id"), from.get("list_id")));

            return parseList(session.createQuery(query).list());
        });


        StaticData.setGroupListMap(parseList);

        Assertions.assertTrue(parseList.isEmpty());

    }


    private static Map<Long, Map<Integer, GroupList>> parseList(List<GroupList> groupLists) {
        Map<Long, Map<Integer, GroupList>> listMap = new HashMap<>();

        for (GroupList entity : groupLists) {
            long bot = entity.getBot();
            int listId = entity.getListId();

            if (!listMap.containsKey(bot)) {
                listMap.put(bot, new HashMap<Integer, GroupList>() {{
                    put(listId, entity);
                }});
                continue;
            }
            if (!listMap.get(bot).containsKey(listId)) {
                listMap.get(bot).put(listId, entity);
            }
        }
        return listMap;
    }

}