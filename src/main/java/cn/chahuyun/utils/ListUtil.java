package cn.chahuyun.utils;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.entity.GroupList;
import cn.chahuyun.files.ConfigData;
import jdk.nashorn.internal.runtime.regexp.joni.Config;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.utils.MiraiLogger;

import java.sql.ResultSet;
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
     * 添加群组
     * @author Moyuyanli
     * @param event 消息事件
     * @date 2022/7/10 0:25
     */
    public static void addList(MessageEvent event) {
        //gr:id id id...
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        Bot bot = event.getBot();

        l.info("code-"+code);

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

        int i = SqliteUtil.INSTANCE.updateData(listPrefixSql);
        if (i == 0) {
            subject.sendMessage("群组" + key + "添加失败！");
            return;
        }
        subject.sendMessage("群组" + key + "添加" + i + "个群成功！");
    }

    /**
     * 查询群组
     * @author Moyuyanli
     * @param event 消息事件
     * @date 2022/7/10 0:25
     */
    public static void queryList(MessageEvent event) throws SQLException {
        //gr:id?
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        Bot bot = event.getBot();

        String[] split = code.split("\\\\?[:：]");
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("SELECT")
                .append(" list_id 'listId',")
                .append(" group_id 'group' ")
                .append("FROM list ")
                .append("WHERE bot = ")
                .append(bot.getId());
        int minList = 2;
        Integer listId = null;
        if (split.length == minList) {
            listId = Integer.valueOf(split[1]);
            stringBuffer.append(" AND ")
                    .append("list_id = ")
                    .append(listId);
        }
        stringBuffer.append(";");


        ResultSet resultSet = SqliteUtil.INSTANCE.queryData(stringBuffer);
        Map<Integer, GroupList> listMap = parseList(resultSet);
        SqliteUtil.INSTANCE.closeConnectionAndStatement();
        if (listMap == null || listMap.size() == 0) {
            subject.sendMessage("没有查询到群组！");
            return;
        }

        if (ConfigData.INSTANCE.getDebugSwitch()) {
            l.info("群组数据-> "+listMap.keySet());
        }

        ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder(subject);
        forwardMessageBuilder.add(bot, singleMessages -> {singleMessages.add("以下为所有查询到的群组↓");return null;});

        for (Map.Entry<Integer, GroupList> entity : listMap.entrySet()) {
            forwardMessageBuilder.add(bot, chain -> {
                chain.add("群组编号："+entity.getKey()+"\n");
                Iterator<Long> iterator = entity.getValue().getGroups().iterator();
                while (iterator.hasNext()) {
                    Long next = iterator.next();
                    chain.add(next+"->");
                    String groupName = null;
                    if (bot.getGroup(next) == null) {
                        groupName = "未知群";
                    } else  {
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
     * @author Moyuyanli
     * @param event 消息事件
     * @date 2022/7/10 0:55
     */
    public static void deleteList(MessageEvent event) {
        //-gr:id id?
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        Bot bot = event.getBot();

        String[] split = code.split("\\\\?[:：]");
        String key = split[1];

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("DELETE FROM list WHERE ")
                .append("bot = ")
                .append(bot)
                .append("AND list_id = ")
                .append(key);
        int minSplit = 3;
        String value = null;
        if (split.length == minSplit) {
            value = split[2];
            stringBuffer.append("AND")
                    .append("group_id = ")
                    .append(value);
        }
        stringBuffer.append(";");

        int i = SqliteUtil.INSTANCE.updateData(stringBuffer);
        if (i != 0) {
            subject.sendMessage("群组" + key + "删除" + ((value == null) ? "成功!" : value + "群成功!"));
            return;
        }
        subject.sendMessage("群组删除失败!");
    }

    /**
     * 解析查询参数
     * @author Moyuyanli
     * @param resultSet 查询参数
     * @date 2022/7/10 0:26
     * @return java.util.Map<java.lang.Integer,cn.chahuyun.entity.GroupList>
     */
    private static Map<Integer, GroupList> parseList(ResultSet resultSet) {
        Map<Integer, GroupList> listMap = new HashMap<>();
        try {
            Map<Integer, List<Long>> map = new HashMap<>();
            while (resultSet.next()) {
                int listId = resultSet.getInt("listId");
                long group = resultSet.getInt("group");
                if (!map.containsKey(listId)) {
                    map.put(listId, new ArrayList<Long>() {{
                        add(group);
                    }});
                    continue;
                }
                map.get(listId).add(group);
            }
            for (Map.Entry<Integer, List<Long>> entry : map.entrySet()) {
                GroupList groupList = new GroupList(entry.getKey(), entry.getValue());
                listMap.put(entry.getKey(), groupList);
            }
        } catch (SQLException e) {
            l.error("群组信息查询失败!");
            e.printStackTrace();
            return null;
        }
        return listMap;
    }


}
