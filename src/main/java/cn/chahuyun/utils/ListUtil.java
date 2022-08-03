package cn.chahuyun.utils;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.data.StaticData;
import cn.chahuyun.entity.Group;
import cn.chahuyun.entity.GroupList;
import cn.chahuyun.files.ConfigData;
import cn.hutool.db.Entity;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.utils.MiraiLogger;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.sql.SQLException;
import java.util.*;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :群组操作
 * @Date 2022/7/9 18:55
 */
public class ListUtil {

    private final static MiraiLogger l = HuYanSession.INSTANCE.getLogger();

    /**
     * 加载或者更新群组数据
     *
     * @param type t 加载 f 更新
     * @author Moyuyanli
     * @date 2022/7/10 16:18
     */
    public static void init(boolean type) {
        String queryGroupListSql =
                "SELECT " +
                        "bot," +
                        "list_id 'listId'," +
                        "group_id 'group' " +
                        "FROM " +
                        "list ;";
        try {
            List<Entity> entityList = HuToolUtil.db.query(queryGroupListSql, Entity.parse(GroupList.class));
//            Map<Long, Map<Integer, GroupList>> parseList = parseList(entityList);
//            StaticData.setGroupListMap(parseList);
        } catch (Exception e) {
            if (type) {
                l.error("群组信息初始化失败:" + e.getMessage());
            } else {
                l.error("群组信息更新失败:" + e.getMessage());
            }
            e.printStackTrace();
        }

        if (type) {
            l.info("数据库群组信息初始化成功!");
            return;
        }
        if (ConfigData.INSTANCE.getDebugSwitch()) {
            l.info("群组信息更新成功!");
        }

    }

    /**
     * 加载或者更新群组数据
     *
     * @param type t 加载 f 更新
     * @author Moyuyanli
     * @date 2022/7/10 16:18
     */
    public static void init(boolean type, int number) {

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

        if (type) {
            l.info("数据库群组信息初始化成功!");
            return;
        }
        if (ConfigData.INSTANCE.getDebugSwitch()) {
            l.info("群组信息更新成功!");
        }

    }

    /**
     * 添加群组
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/7/10 0:25
     */
    public static void addList(MessageEvent event) {
        //gr:id id id...
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        Bot bot = event.getBot();

        if (ConfigData.INSTANCE.getDebugSwitch()) {
            l.info("code-" + code);
        }

        String[] split = code.split("\\s+");
        int key = Integer.parseInt(split[0].split("\\\\?[:：]")[1]);
        try {
            HibernateUtil.factory.fromTransaction(session -> {
                GroupList groupList = new GroupList(bot.getId(), key, new ArrayList<Group>());
                for (int i = 1; i < split.length; i++) {
                    String s = split[i];
                    groupList.getGroups().add(new Group(key, Long.parseLong(s)));
                }
                session.persist(groupList);
                return 0;
            });
        } catch (Exception e) {
            l.error("数据库添加群组失败:",e);
            subject.sendMessage("群组" + key + "添加失败！");
            return;
        }
        subject.sendMessage("群组" + key + "添加群成功！");
        init(false);
    }

    /**
     * 查询群组
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/7/10 0:25
     */
    public static void queryList(MessageEvent event) {
        //gr:id?
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        Bot bot = event.getBot();

        String[] split = code.split("\\\\?[:：]");

        int key = 0;
        if (split.length == 2) {
            key = Integer.parseInt(split[1]);
        }
        Map<Integer, GroupList> groupListMap = null;
        try {
            groupListMap = StaticData.getGroupListMap(bot.getId());
            if (groupListMap == null || groupListMap.isEmpty()) {
                subject.sendMessage("没有群组信息!");
                return;
            }
        } catch (NullPointerException e) {
            l.warning("没有群组信息!",e);
            subject.sendMessage("没有群组信息!");
            return;
        }
        ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder(subject);
        forwardMessageBuilder.add(bot, singleMessages -> {
            singleMessages.add("以下为所有查询到的群组↓");
            return null;
        });

        for (GroupList entity : groupListMap.values()) {
            if (key != 0 && key != entity.getListId()) {
                continue;
            }
            forwardMessageBuilder.add(bot, chain -> {
                chain.add("群组编号：" + entity.getListId() + "\n");
                Iterator<Group> iterator = entity.getGroups().iterator();
                while (iterator.hasNext()) {
                    Group next = iterator.next();
                    chain.add(next + "->");
                    String groupName = null;
                    if (bot.getGroup(next.getGroup()) == null) {
                        groupName = "未知群";
                    } else {
                        groupName = Objects.requireNonNull(bot.getGroup(next.getGroup())).getName();
                    }
                    chain.add(groupName);
                    if (iterator.hasNext()) {
                        chain.add("\n");
                    }
                }
                return null;
            });
        }
        subject.sendMessage(forwardMessageBuilder.build());
    }

    /**
     * 删除群组
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/7/10 0:55
     */
    public static void deleteList(MessageEvent event) {
        //-gr:id id?
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        Bot bot = event.getBot();

        String[] split = code.split("\\\\?[:：]");
        String[] strings = split[1].split("\\s+");

        int key = Integer.parseInt(strings[0]);

        int minSplit = 2;
        String value = null;
        boolean type = true;
        if (strings.length == minSplit) {
            value = strings[1];
            type = false;
        }

//        try {
//            HibernateUtil.factory.fromTransaction(session -> {
//                GroupList groupList = new GroupList(bot.getId(), key);
//                new Group(key,)
//
//
//            })
//            if (value != null) {
//                i = HuToolUtil.db.execute(stringBuffer.toString(), bot.getId(), key, value);
//            } else {
//                i = HuToolUtil.db.execute(stringBuffer.toString(), bot.getId(), key);
//            }
//        } catch (SQLException e) {
//            l.error("数据库删除群组失败:" + e.getMessage());
//            e.printStackTrace();
//        }
//        if (i == 0) {
//            subject.sendMessage("群组删除失败!");
//            return;
//        }
        subject.sendMessage("群组" + key + "删除" + ((value == null) ? "成功!" : value + "群成功!"));
        init(false);
    }


    /**
     * 判断这个群组是否存在
     *
     * @param bot     所属机器人
     * @param list_id 群组编号
     * @return boolean 存在 true
     * @author Moyuyanli
     * @date 2022/7/11 12:13
     */
    public static boolean isContainsList(long bot, int list_id) {
        Map<Integer, GroupList> groupListMap;
        try {
            groupListMap = StaticData.getGroupListMap(bot);
            if (groupListMap == null || groupListMap.isEmpty()) {
                return false;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
        return groupListMap.containsKey(list_id);
    }

    /**
     * 解析查询参数
     *
     * @param groupLists 查询参数
     * @return java.util.Map<java.lang.Integer, cn.chahuyun.entity.GroupList>
     * @author Moyuyanli
     * @date 2022/7/10 0:26
     */
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
