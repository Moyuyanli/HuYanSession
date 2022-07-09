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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :群组操作
 * @Date 2022/7/9 18:55
 */
public class ListUtil {

    private final static MiraiLogger l = HuYanSession.INSTANCE.getLogger();


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
        if (ConfigData.INSTANCE.getSqlSwitch()) {
            l.info("sql-> "+listPrefixSql);
        }
        int i = SqliteUtil.INSTANCE.updateData(listPrefixSql, null);
        if (i == 0) {
            subject.sendMessage("群组" + key + "添加失败！");
            return;
        }
        subject.sendMessage("群组" + key + "添加" + i + "个群成功！");
    }

    public static void queryList(MessageEvent event) {
        //gr:id?
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        Bot bot = event.getBot();

        String[] split = code.split("\\\\?[:：]");
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("SELECT")
                .append(" list_id 'listId',")
                .append(" group_id 'group',")
                .append("FROM mate")
                .append("WHERE bot = ")
                .append(bot.getId());
        int minList = 2;
        Object listId = null;
        if (split.length == minList) {
            listId = Integer.valueOf(split[1]);
            stringBuffer.append(" AND ")
                    .append("list_id = ?");
        }
        stringBuffer.append(";");

        ResultSet resultSet = SqliteUtil.INSTANCE.queryData(stringBuffer, listId);
        Map<Integer, GroupList> listMap = parseList(resultSet);

        if (listMap.size() == 0) {
            subject.sendMessage("没有查询到群组！");
            return;
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
    }


    private static Map<Integer, GroupList> parseList(ResultSet resultSet) {
        Map<String, GroupList> listMap = new HashMap<>();
//        try {
////            while (resultSet.next()) {
////                listMap.put(resultSet.getInt("listId"))
////            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
        return null;
    }


}
