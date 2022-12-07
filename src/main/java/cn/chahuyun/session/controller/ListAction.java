package cn.chahuyun.session.controller;

import cn.chahuyun.config.SessionConfig;
import cn.chahuyun.session.data.StaticData;
import cn.chahuyun.session.entity.GroupInfo;
import cn.chahuyun.session.entity.GroupList;
import cn.chahuyun.session.utils.HibernateUtil;
import jakarta.persistence.PersistenceException;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.util.*;
import java.util.stream.Collectors;

import static cn.chahuyun.session.HuYanSession.log;

/**
 * 说明
 * 群组操作
 *
 * @author Moyuyanli
 * @Date 2022/7/9 18:55
 */
public class ListAction {


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
            log.info("数据库群组信息初始化成功!");
            return;
        }
        if (SessionConfig.INSTANCE.getDebugSwitch()) {
            log.info("群组信息更新成功!");
        }

    }

    /**
     * 解析查询参数
     *
     * @param groupLists 查询参数
     * @return java.util.Map<java.lang.Integer, cn.chahuyun.session.entity.GroupList>
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
                listMap.put(bot, new HashMap<>() {{
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

    /**
     * 添加群组
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/7/10 0:25
     */
    public void addGroupListInfo(MessageEvent event) {
        //gr:id id id...
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        Bot bot = event.getBot();

        if (SessionConfig.INSTANCE.getDebugSwitch()) {
            log.info("code-" + code);
        }

        String[] split = code.split("\\s+");
        int key = Integer.parseInt(split[0].split("\\\\?[:：]")[1]);

        StringBuilder reply = new StringBuilder();
        //判断新加的群号在这个群组中是否存在，存在则拼接回复消息
        Map<Integer, GroupList> groupListMap = StaticData.getGroupListMap(bot);
        if (groupListMap != null && groupListMap.containsKey(key)) {
            GroupList groupList = groupListMap.get(key);
            for (int i = 1; i < split.length; i++) {
                long groupId = Long.parseLong(split[i]);
                if (groupList.containsGroupId(groupId)) {
                    reply.append("群").append(groupId).append("已存在\n");
                }
            }
        }

        try {
            //开始添加群组
            HibernateUtil.factory.fromTransaction(session -> {
                GroupList groupList;
                if (groupListMap != null && groupListMap.containsKey(key)) {
                    groupList = groupListMap.get(key);
                } else {
                    groupList = new GroupList(bot.getId(), key);
                }
                for (int i = 1; i < split.length; i++) {
                    long groupId = Long.parseLong(split[i]);
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
            log.error("数据库添加群组失败:", e);
            subject.sendMessage("群组" + key + "添加失败！");
            return;
        }
        //我又开始码屎山了，这TM才开始写啊!T_T
        //如果只加了1个群号，并且群号还存在的话返回这条消息
        if (split.length == 2 && !reply.toString().equals("")) {
            subject.sendMessage("群组" + key + "中" + reply);
            return;
        }
        String message = "群组" + key + "添加群成功！";
        if (!reply.toString().equals("")) {
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
    public void queryGroupListInfo(MessageEvent event) {
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
        Map<Integer, GroupList> groupListMap;
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
                    String groupName;
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


    //==========================================================================================

    /**
     * 删除群组
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/7/10 0:55
     */
    public void deleteGroupListInfo(MessageEvent event) {
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
                } else {
                    assert groupList != null;
                    GroupInfo groupInfo = groupList.getGroups().stream().filter(item -> item.getGroupId() == finalValue)
                            .collect(Collectors.toList()).get(0);
                    groupList.getGroups().remove(groupInfo);
                    session.merge(groupList);
                }
                return true;
            });
        } catch (Exception e) {
            if (e instanceof PersistenceException) {
                log.error("不允许群组为空群组！");
            } else {
                log.error("数据库删除群组失败:", e);
            }
        }
        if (Boolean.FALSE.equals(aBoolean)) {
            subject.sendMessage("群组删除失败!");
            return;
        }
        subject.sendMessage("群组" + key + "删除" + ((value == null) ? "成功!" : value + "群成功!"));
        init(false);
    }


}
