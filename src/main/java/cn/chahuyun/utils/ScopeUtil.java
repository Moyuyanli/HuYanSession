package cn.chahuyun.utils;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.entity.Scope;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.utils.MiraiLogger;

import java.sql.SQLException;
import java.util.List;

/**
 * ScopeUtil
 *
 * @author Zhangjiaxing
 * @description 作用域Util
 * @date 2022/7/11 12:16
 */
public class ScopeUtil {

    private final static MiraiLogger l = HuYanSession.INSTANCE.getLogger();

    public static int getScopeId(Bot bot, Scope scope) {
        long scope_id;

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
            list = HuToolUtil.db.query(queryScopeSql,Scope.class,bot.getId(),scope.isGroup(), scope.isGlobal(), scope.getGroup(), scope.getListId());
        } catch (SQLException e) {
            l.error("搜索作用域时失败:" + e.getMessage());
            e.printStackTrace();
            return -1;
        }


        if (list.size() == 0) {
            try {
                scope_id = HuToolUtil.db.executeForGeneratedKey(insertScopeSql, bot.getId(), scope.getScopeName(), scope.isGroup(), scope.isGlobal(), scope.getGroup(), scope.getListId());
            } catch (SQLException e) {
                l.error("添加作用域时失败:" + e.getMessage());
                e.printStackTrace();
                return -1;
            }
        } else {
            scope_id = list.get(0).getId();
        }
        return (int) scope_id;
    }


}