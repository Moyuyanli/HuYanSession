package cn.chahuyun.utils;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.data.StaticData;
import cn.chahuyun.entity.GroupList;
import cn.chahuyun.files.ConfigData;
import cn.hutool.db.Entity;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.utils.MiraiLogger;

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
            Map<Long, Map<Integer, GroupList>> parseList = parseList(entityList);
            StaticData.setGroupListMap(parseList);
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

        l.info("code-" + code);

        String[] split = code.split("\\s+");
        int key = Integer.parseInt(split[0].split("\\\\?[:：]")[1]);

        StringBuffer listPrefixSql = new StringBuffer("INSERT INTO list(bot,list_id,group_id)");
        for (int i = 1; i < split.length; i++) {
            String s = split[i];
            if (i + 1 == split.length) {
                listPrefixSql.append("SELECT ").append(bot.getId()).append(",").append(key).append(",").append(s).append(";");
                break;
            }
            listPrefixSql.append("SELECT ").append(bot.getId()).append(",").append(key).append(",").append(s).append(" UNION\n");
        }

        int i = 0;
        try {
            i = HuToolUtil.db.execute(listPrefixSql.toString());
        } catch (SQLException e) {
            l.error("数据库添加群组失败:" + e.getMessage());
            subject.sendMessage("群组" + key + "添加失败！");
            e.printStackTrace();
            return;
        }
        if (i == 0) {
            subject.sendMessage("群组" + key + "添加失败！");
            return;
        }
        subject.sendMessage("群组" + key + "添加" + i + "个群成功！");
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
            subject.sendMessage("没有群组信息!");
            e.printStackTrace();
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
                Iterator<Long> iterator = entity.getGroups().iterator();
                while (iterator.hasNext()) {
                    Long next = iterator.next();
                    chain.add(next + "->");
                    String groupName = null;
                    if (bot.getGroup(next) == null) {
                        groupName = "未知群";
                    } else {
                        groupName = Objects.requireNonNull(bot.getGroup(next)).getName();
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

        String key = strings[0];

        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append("DELETE FROM list WHERE ").append("bot = ? ").append("AND list_id = ? ");

        int minSplit = 2;
        String value = null;
        if (strings.length == minSplit) {
            value = strings[1];
            stringBuffer.append("AND ").append("group_id = ? ;");
        }

        stringBuffer.append(";");

        int i = 0;
        try {
            if (value != null) {
                i = HuToolUtil.db.execute(stringBuffer.toString(), bot.getId(), key,value);
            }else {
                i = HuToolUtil.db.execute(stringBuffer.toString(), bot.getId(), key);
            }
        } catch (SQLException e) {
            l.error("数据库删除群组失败:" + e.getMessage());
            e.printStackTrace();
        }
        if (i == 0) {
            subject.sendMessage("群组删除失败!");
            return;
        }
        subject.sendMessage("群组" + key + "删除" + ((value == null) ? "成功!" : value + "群成功!"));
        init(false);
    }


    /**
     * 判断这个群组是否存在
     * @author Moyuyanli
     * @param bot 所属机器人
     * @param list_id 群组编号
     * @date 2022/7/11 12:13
     * @return boolean 存在 true
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
     * @param entityList 查询参数
     * @return java.util.Map<java.lang.Integer, cn.chahuyun.entity.GroupList>
     * @author Moyuyanli
     * @date 2022/7/10 0:26
     */
    private static Map<Long, Map<Integer, GroupList>> parseList(List<Entity> entityList) {
        Map<Long, Map<Integer, GroupList>> listMap = new HashMap<>();


        for (Entity entity : entityList) {
            long bot = entity.getLong("bot");
            int listId = entity.getInt("listId");
            long group = entity.getInt("group");

            if (!listMap.containsKey(bot)) {
                listMap.put(bot, new HashMap<Integer, GroupList>() {{
                    put(listId, new GroupList(bot, listId, new ArrayList<Long>() {{
                        add(group);
                    }}));
                }});
                continue;
            }
            if (!listMap.get(bot).containsKey(listId)) {
                listMap.get(bot).put(listId, new GroupList(bot, listId, new ArrayList<Long>() {{
                    add(group);
                }}));
                continue;
            }
            listMap.get(bot).get(listId).getGroups().add(group);
        }

        return listMap;
    }


}
