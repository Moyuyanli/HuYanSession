package cn.chahuyun.utils;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.entity.Scope;
import cn.chahuyun.enums.Mate;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.utils.MiraiLogger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :对话消息工具类
 * @Date 2022/7/9 17:14
 */
public class SessionUtil {

    private final static MiraiLogger l = HuYanSession.INSTANCE.getLogger();


    public static void studySession(MessageEvent event) {
        //xx a b [p1] [p2]
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        Bot bot = event.getBot();

        String[] split = code.split("\\s+");
        int type = 0;
        String key = split[1];
        String value = split[2];

        String typePattern = "\\[mirai\\\\:image\\S+]";
        if (Pattern.matches(typePattern, key) || Pattern.matches(typePattern, value)) {
            type = 1;
        }

        Mate mate = Mate.ACCURATE;
        Scope scope = new Scope(bot.getId(),"当前", false, false, subject.getId(), 0);

        //最小分割大小
        int minIndex = 3;
        //大于这个大小就进行参数判断
        if (split.length > minIndex) {
            for (int i = minIndex; i < split.length; i++) {
                String s = split[i];
                switch (s) {
                    case "模糊":
                        mate = Mate.VAGUE;
                        break;
                    case "头部":
                        mate = Mate.START;
                        break;
                    case "结尾":
                        mate = Mate.END;
                        break;
                    case "全局":
                        scope = new Scope(bot.getId(),"全局", true, false, subject.getId(), 0);
                        break;
                    default:
                        String listPattern = "gr\\d+";
                        int listId = Integer.parseInt(s.substring(2));
                        if (Pattern.matches(listPattern, s)) {
                            scope = new Scope(bot.getId(),"群组", false, true, subject.getId(), listId);
                        }
                        break;
                }
            }
        }
        if (subject instanceof User && !scope.isGlobal() && scope.isGroup() ) {
            subject.sendMessage("私发学习请输入作用域！");
            return;
        }


        StringBuffer queryScopeSql = new StringBuffer();
        queryScopeSql.append("SELECT " )
                .append("id ,")
                .append("FROM " )
                .append("scope " )
                .append("WHERE " )
                .append("bot = ")
                .append(bot.getId())
                .append(" AND isgroup = ")
                .append(scope.isGroup())
                .append(" AND isglobal = ")
                .append(scope.isGlobal())
                .append(" AND group = ")
                .append(scope.getGroup())
                .append(" AND list_id = ")
                .append(scope.getListId())
                .append(";");
        StringBuffer insertScopeSql = new StringBuffer();
        insertScopeSql.append("INSERT INTO scope(scope_name,isgroup,isglobal,group,list_id)")
                .append("SELECT ")
                .append(scope.getScopeName()).append(",")
                .append(scope.isGroup()).append(",")
                .append(scope.isGlobal()).append(",")
                .append(scope.getGroup()).append(",")
                .append(scope.getListId()).append(";");

        ResultSet set = SqliteUtil.INSTANCE.queryData(queryScopeSql);
        int scope_id = 0;
        try {
            if (set.next()) {
                scope_id = set.getInt("id");
                SqliteUtil.INSTANCE.closeConnectionAndStatement();
            }else {
                int data = SqliteUtil.INSTANCE.updateData(insertScopeSql);
                if (data == 0) {
                    subject.sendMessage("添加失败，作用域操作失败!");
                    return;
                }
                set = SqliteUtil.INSTANCE.queryData(queryScopeSql);
                while (set.next()) {
                    scope_id = set.getInt("id");
                }
                SqliteUtil.INSTANCE.closeConnectionAndStatement();
            }
        } catch (SQLException e) {
            l.error("数据库查询scope出错:"+e.getMessage());
            e.printStackTrace();
        }

        StringBuffer insertSessionSql = new StringBuffer();
        insertSessionSql.append("INSERT INTO session(bot,type,key,value,mate_id,scope_id)")
                .append("SELECT ")
                .append(bot.getId()).append(",")
                .append(type).append(",")
                .append(key).append(",")
                .append(value).append(",")
                .append(mate.getMateType()).append(",")
                .append(scope_id).append(";");
        int i = SqliteUtil.INSTANCE.updateData(insertSessionSql);
        if (i == 0) {
            subject.sendMessage("学不废!");
            return;
        }
        subject.sendMessage("学废了!");
    }




}
