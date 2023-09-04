package cn.chahuyun.session.controller;

import cn.chahuyun.session.config.SessionConfig;
import cn.chahuyun.session.data.StaticData;
import cn.chahuyun.session.entity.GroupInfo;
import cn.chahuyun.session.entity.GroupList;
import cn.chahuyun.session.utils.HibernateUtil;
import jakarta.persistence.PersistenceException;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.util.*;
import java.util.stream.Collectors;

import static cn.chahuyun.session.HuYanSession.LOGGER;

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
        List<GroupList> lists = HibernateUtil.factory.fromTransaction(session -> {
            //创建构造器
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            //创建实体对应查询器
            JpaCriteriaQuery<GroupList> query = builder.createQuery(GroupList.class);
            JpaRoot<GroupList> from = query.from(GroupList.class);
            query.select(from);
            return session.createQuery(query).list();
        });

        StaticData.setGroupListMap(parseList(lists));

        if (type) {
            LOGGER.info("数据库群组信息初始化成功!");
        } else if (SessionConfig.INSTANCE.getDebugSwitch()) {
            LOGGER.info("群组信息更新成功!");
        }
    }


    /**
     * 添加群组
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/7/10 0:25
     */
    public void addGroupListInfo(MessageEvent event) {
        //gr:key id id...
        String content = event.getMessage().contentToString();
        Contact subject = event.getSubject();
        Bot bot = event.getBot();

        if (SessionConfig.INSTANCE.getDebugSwitch()) {
            LOGGER.info("content->" + content);
        }
        String[] split = content.replace(":", " ").replace("：", " ").split("\\s+");
        String key = split[1];

        MessageChainBuilder reply = new MessageChainBuilder();

        //判断新加的群号在这个群组中是否存在，存在则拼接回复消息
        Map<String, GroupList> groupListMap = StaticData.getGroupListMap(bot);
        GroupList groupList;
        if (groupListMap.containsKey(key)) {
            groupList = groupListMap.get(key);
        } else {
            groupList = new GroupList(bot.getId(), key);
        }
        for (int i = 2; i < split.length; i++) {
            long groupId = Long.parseLong(split[i]);
            if (groupList.containsGroupId(groupId)) {
                reply.append("群").append(Long.toString(groupId)).append("已存在于").append(key).append("中\n");
            } else {
                groupList.getGroups().add(new GroupInfo(bot.getId(), groupId));
                reply.append("群").append(Long.toString(groupId)).append("已添加于").append(key).append("中\n");
            }
        }
        if (groupList.merge()) {
            subject.sendMessage(reply.append("群组信息保存成功~").build());
        } else {
            subject.sendMessage("群组信息保存失败~");
        }

        /*
        以下代码是旧版代码

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
                    GroupInfo groupInfo = new GroupInfo(bot.getId(), groupId);
                    groupList.getGroups().add(groupInfo);
                }
                session.merge(groupList);
                return 0;
            });
        } catch (Exception e) {
            LOGGER.error("数据库添加群组失败:", e);
            subject.sendMessage("群组" + key + "添加失败！");
            return;
        }
        //我又开始码屎山了，这TM才开始写啊!T_T
        //如果只加了1个群号，并且群号还存在的话返回这条消息
        if (split.length == 2 && !"".equals(reply.toString())) {
            subject.sendMessage("群组" + key + "中" + reply);
            return;
        }
        String message = "群组" + key + "添加群成功！";
        if (!"".equals(reply.toString())) {
            message += "其中:\n" + reply;
        }
        subject.sendMessage(message);

         */
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
        String content = event.getMessage().contentToString();
        Contact subject = event.getSubject();
        Bot bot = event.getBot();
        init(false);

        String[] split = content.replace("：", " ").split("\\s+");

        String key = null;
        if (split.length == 2) {
            key = split[1];
        }
        //拿静态资源
        Map<String, GroupList> groupListMap;
        groupListMap = StaticData.getGroupListMap(bot);
        if (groupListMap == null || groupListMap.isEmpty()) {
            subject.sendMessage("没有群组信息!");
            return;
        }
        if (key!=null && !groupListMap.containsKey(key)) {
            subject.sendMessage("没有这个群组信息!");
            return;
        }

        ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder(subject);
        forwardMessageBuilder.add(bot, singleMessages -> {
            singleMessages.add("以下为所有查询到的群组↓");
            return null;
        });

        for (GroupList entity : groupListMap.values()) {
            if (key != null && key.equals(entity.getListId())) {
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



    /**
     * 删除群组
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/7/10 0:55
     */
    public void deleteGroupListInfo(MessageEvent event) {
        //-gr:id id?
        String content = event.getMessage().contentToString();
        Contact subject = event.getSubject();
        Bot bot = event.getBot();

        String[] split = content.replace("：", " ").replace(":", " ").split("\\s+");

        String key = split[1];

        MessageChainBuilder builder = new MessageChainBuilder();

        Map<String, GroupList> groupListMap = StaticData.getGroupListMap(bot);
        if (groupListMap.containsKey(key)) {
            GroupList groupList = groupListMap.get(key);

            if (split.length == 2) {
                groupList.remove();
                builder.append("群组").append(key).append("删除成功!");
            }else {
                for (int i = 2; i < split.length; i++) {
                    long groupId = Long.parseLong(split[i]);
                    if (groupList.containsGroupId(groupId)) {
                        groupList.getGroupInfo(groupId).remove();
                        builder.append("群").append(Long.toString(groupId)).append("删除于").append(key).append("中");
                    } else {
                        builder.append("群").append(Long.toString(groupId)).append("不存在于").append(key).append("中");
                    }
                }
            }
        } else {
            builder.append("群组").append(key).append("不存在!");
        }
        subject.sendMessage(builder.build());
        init(false);
    }



    //====================================private====================================


    /**
     * 解析查询参数
     *
     * @param groupLists 查询参数
     * @return java.util.Map<java.lang.Integer, cn.chahuyun.session.entity.GroupList>
     * @author Moyuyanli
     * @date 2022/7/10 0:26
     */
    private static Map<Long, Map<String, GroupList>> parseList(List<GroupList> groupLists) {
        if (groupLists == null || groupLists.isEmpty()) {
            return null;
        }
        Map<Long, Map<String, GroupList>> listMap = new HashMap<>();
        for (GroupList entity : groupLists) {
            long bot = entity.getBot();
            String listId = entity.getListId();
            if (listMap.containsKey(bot)) {
                listMap.get(bot).put(listId, entity);
            }else {
                listMap.put(bot, new HashMap<>() {{
                    put(listId, entity);
                }});
            }
        }
        return listMap;
    }


}
