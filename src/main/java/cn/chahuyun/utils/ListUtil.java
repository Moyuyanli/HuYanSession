package cn.chahuyun.utils;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.data.StaticData;
import cn.chahuyun.entity.GroupInfo;
import cn.chahuyun.entity.GroupList;
import cn.chahuyun.config.ConfigData;
import jakarta.persistence.PersistenceException;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.utils.MiraiLogger;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 说明
 * 群组操作
 *
 * @author Moyuyanli
 * @Date 2022/7/9 18:55
 */
public class ListUtil {

    private final static MiraiLogger l = HuYanSession.INSTANCE.getLogger();

    /**
     * 加载或者更新群组数据-Hibernate
     *
     * @param type t 加载 f 更新
     * @author Moyuyanli
     * @date 2022/7/10 16:18
     */
    public static void init(boolean type) {

        Map<Long, Map<Integer, GroupList>> parseList = HibernateUtil.factory.fromTransaction(session -> {
            //创建构造器
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            //创建实体对应查询器
            JpaCriteriaQuery<GroupList> query = builder.createQuery(GroupList.class);
            JpaRoot<GroupList> from = query.from(GroupList.class);
            query.select(from);
            List<GroupList> groupLists = session.createQuery(query).list();
            return parseList(groupLists);
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
    public static void addGroupListInfo(MessageEvent event) {
        //gr:id id id...
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        Bot bot = event.getBot();

        if (ConfigData.INSTANCE.getDebugSwitch()) {
            l.info("code-" + code);
        }

        String[] split = code.split("\\s+");
        int key = Integer.parseInt(split[0].split("\\\\?[:：]")[1]);

        String reply = "";
        //判断新加的群号在这个群组中是否存在，存在则拼接回复消息
        Map<Integer, GroupList> groupListMap = StaticData.getGroupListMap(bot);
        if (groupListMap != null && groupListMap.containsKey(key)) {
            GroupList groupList = groupListMap.get(key);
            for (int i = 1; i < split.length; i++) {
                Long groupId = Long.parseLong(split[i]);
                if (groupList.containsGroupId(groupId)) {
                    reply += "群" + groupId + "已存在\n";
                }
            }
        }

        try {
            //开始添加群组
            HibernateUtil.factory.fromTransaction(session -> {
                GroupList groupList = null;
                if (groupListMap != null && groupListMap.containsKey(key)) {
                    groupList = groupListMap.get(key);
                } else {
                    groupList = new GroupList(bot.getId(), key);
                }
                for (int i = 1; i < split.length; i++) {
                    Long groupId = Long.parseLong(split[i]);
                    if (groupList.containsGroupId(groupId)) {
                        continue;
                    }
                    GroupInfo groupInfo = new GroupInfo(bot.getId(), key, groupId);
                    groupList.getGroups().add(groupInfo);
                }
                session.merge(groupList);
                return 0;
            });
        } catch (Exception e) {
            l.error("数据库添加群组失败:", e);
            subject.sendMessage("群组" + key + "添加失败！");
            return;
        }
        //我又开始码屎山了，这TM才开始写啊!T_T
        //如果只加了1个群号，并且群号还存在的话返回这条消息
        if (split.length == 2 && !reply.equals("")) {
            subject.sendMessage("群组" + key + "中" + reply);
            return;
        }
        String message = "群组" + key + "添加群成功！";
        if (!reply.equals("")) {
            message += "其中:\n" + reply;
        }
        subject.sendMessage(message);
        init(false);
    }

    /**
     * 查询群组
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/7/10 0:25
     */
    public static void queryGroupListInfo(MessageEvent event) {
        //gr:id?
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        Bot bot = event.getBot();
        init(false);

        String[] split = code.split("\\\\?[:：]");

        int key = 0;
        if (split.length == 2) {
            key = Integer.parseInt(split[1]);
        }
        //拿静态资源
        Map<Integer, GroupList> groupListMap = null;
        groupListMap = StaticData.getGroupListMap(bot);
        if (groupListMap == null || groupListMap.isEmpty()) {
            subject.sendMessage("没有群组信息!");
            return;
        }

        if (key != 0 && !groupListMap.containsKey(key)) {
            subject.sendMessage("没有这个群组信息!");
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
                Iterator<GroupInfo> iterator = entity.getGroups().iterator();
                while (iterator.hasNext()) {
                    GroupInfo next = iterator.next();
                    chain.add(next.getGroupId() + "->");
                    String groupName = null;
                    if (bot.getGroup(next.getGroupId()) == null) {
                        groupName = "未知群";
                    } else {
                        groupName = Objects.requireNonNull(bot.getGroup(next.getGroupId())).getName();
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
    public static void deleteGroupListInfo(MessageEvent event) {
        //-gr:id id?
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        Bot bot = event.getBot();

        String[] split = code.split("\\\\?[:：]");
        String[] strings = split[1].split("\\s+");

        int key = Integer.parseInt(strings[0]);

        int minSplit = 2;
        Long value = null;
        boolean type = true;
        if (strings.length == minSplit) {
            value = Long.parseLong(strings[1]);
            type = false;
        }

        if (type) {
            if (!StaticData.isGrouper(bot, key)) {
                subject.sendMessage("没有找到要忘掉的群组~");
                return;
            }
        } else {
            if (!StaticData.isGrouper(bot, key, value)) {
                subject.sendMessage("没有找到要忘掉的群~");
                return;
            }
        }

        Boolean aBoolean = false;
        try {
            boolean finalType = type;
            Long finalValue = value;
            aBoolean = HibernateUtil.factory.fromTransaction(session -> {
                Map<Integer, GroupList> groupListMap = StaticData.getGroupListMap(bot);
                GroupList groupList = null;
                //如果有群组，则先从内存拿到群组
                if (groupListMap != null && groupListMap.containsKey(key)) {
                    groupList = groupListMap.get(key);
                }
                //如果是删除群组
                if (finalType) {
                    session.remove(groupList);
                    return true;
                } else {
                    assert groupList != null;
                    GroupInfo groupInfo = groupList.getGroups().stream().filter(item -> item.getGroupId() == finalValue)
                            .collect(Collectors.toList()).get(0);
                    groupList.getGroups().remove(groupInfo);
                    session.merge(groupList);
                    return true;
                }
            });
        } catch (Exception e) {
            if (e instanceof PersistenceException) {
                l.error("不允许群组为空群组！");
            } else {
                l.error("数据库删除群组失败:", e);
            }
        }
        if (Boolean.FALSE.equals(aBoolean)) {
            subject.sendMessage("群组删除失败!");
            return;
        }
        subject.sendMessage("群组" + key + "删除" + ((value == null) ? "成功!" : value + "群成功!"));
        init(false);
    }


    /**
     * 判断这个群组是否存在
     *
     * @param bot    所属机器人
     * @param listId 群组编号
     * @return boolean 存在 true
     * @author Moyuyanli
     * @date 2022/7/11 12:13
     */
    public static boolean isContainsList(Bot bot, int listId) {
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
        return groupListMap.containsKey(listId);
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
        if (groupLists == null || groupLists.isEmpty()) {
            return null;
        }
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
