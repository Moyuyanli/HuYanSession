package cn.chahuyun.utils;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.entity.Scope;
import cn.chahuyun.enums.Mate;
import cn.hutool.db.Entity;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.utils.MiraiLogger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
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

        String typePattern = "\\[mirai:image\\S+]";
        if (Pattern.matches(typePattern, key) || Pattern.matches(typePattern, value)) {
            type = 1;
        }

        Mate mate = Mate.ACCURATE;
        Scope scope = new Scope(bot.getId(),"当前", false, false, subject.getId(), -1);

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
                        scope = new Scope(bot.getId(),"全局", true, false, subject.getId(), -1);
                        break;
                    default:
                        String listPattern = "gr\\d+";
                        if (Pattern.matches(listPattern, s)) {
                            int listId = Integer.parseInt(s.substring(2));
                            if (!ListUtil.isContainsList(bot.getId(), listId)) {
                                subject.sendMessage("该群组不存在!");
                                return;
                            }
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

        String queryScopeSql =
                "SELECT id " +
                "FROM scope " +
                "WHERE bot = ? " +
                        "AND is_group = ? " +
                        "AND is_global = ? " +
                        "AND `group` = ? " +
                        "AND list_id = ?;";
        String insertScopeSql =
                "INSERT INTO scope(bot,scope_name,is_group,is_global,`group`,list_id)"+
                "VALUES(?,?,?,?,?,?);";

        List<Scope> list = null;
        try {
            l.info("前");
            list = HuToolUtil.db.query(queryScopeSql,Scope.class,bot.getId(),scope.isGroup(), scope.isGlobal(), scope.getGroup(), scope.getListId());
            l.info("后");
        } catch (SQLException e) {
            l.error("搜索作用域时失败:" + e.getMessage());
            subject.sendMessage("学不废,布吉岛该往哪里发!");
            e.printStackTrace();
            return;
        }
        long scope_id = 0;
        if (list.size() == 0) {
            try {
                scope_id = HuToolUtil.db.executeForGeneratedKey(insertScopeSql, bot.getId(), scope.getScopeName(), scope.isGroup(), scope.isGlobal(), scope.getGroup(), scope.getListId());
            } catch (SQLException e) {
                l.error("添加作用域时失败:" + e.getMessage());
                subject.sendMessage("学不废,忘记料该往哪里发!");
                e.printStackTrace();
                return;
            }
        } else {
            scope_id = list.get(0).getId();
        }

        String insertSessionSql =
        "INSERT INTO session(bot,type,key,value,mate_id,scope_id)"+
        "VALUES( ?, ?, ? ,? ,?, ?) ;";

        int i = 0;
        try {
            i = HuToolUtil.db.execute(insertSessionSql, bot.getId(), type, key, value, mate.getMateType(), scope_id);
        } catch (SQLException e) {
            l.error("添加对话失败:" + e.getMessage());
            subject.sendMessage("学不废!");
            e.printStackTrace();
            return;
        }
        if (i == 0) {
            subject.sendMessage("学不废!");
            return;
        }
        subject.sendMessage("学废了!");
    }




}
