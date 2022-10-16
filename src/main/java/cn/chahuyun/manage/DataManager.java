package cn.chahuyun.manage;

import cn.chahuyun.entity.SessionInfo;
import cn.chahuyun.utils.HibernateUtil;
import net.mamoe.mirai.Bot;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据管理 导出 导入 excel
 * 数据转移 所属 机器人的信息转移
 *
 * @author Moyuyanli
 * @date 2022/10/11 14:57
 */
public class DataManager {

    //todo 所属 机器人的信息转移

    /**
     * 将 fromBotId 的数据迁移到 toBotId 中
     * 更改了数据库中的 bot 字段
     *
     * @param fromBotId
     * @param toBotId
     * @return void
     * @author forDecember
     * @date 2022/10/11 18:43
     */
    public static void transferInfo(Long fromBotId,Long toBotId){

        // 获取表名
        List<String> tables = HibernateUtil.factory
                .fromTransaction(session ->
                        session.createNativeQuery(
                        "show tables",String.class)
                        .list());

        // 穷举实体类
        String[] entity ={
                "BlackHouse",
                "Blacklist",
                "GroupInfo",
                "GroupList",
                "GroupProhibited",
                "GroupWelcomeInfo",
                "ManySession",
                "ManySessionInfo",
                "Power",
                "QuartzInfo",
                "Scope",
                "SessionInfo",
                "WelcomeMessage"
        };

        // 遍历表，找到与表名对应的实体类并进行映射
        for (String table : tables){
            String className=null;
            boolean find=false;
            for (String s : entity) {
                // 用contains判断会让 Session 和 ManySession 冲突
                // 类顺序改变会导致 ManySession 和 ManySessionInfo 错误赋值
                // 总之下面这段代码很寄
                if (s.equalsIgnoreCase(table)||s.equalsIgnoreCase(table+"info")) {
                    className = s;
                    find=true;
                    break;
                }
            }
            if(!find)throw new RuntimeException("表与实体类不对应...");

            // 获取'当前遍历到的表'中的'类的实例们'
            String sql = "select * from "+table;
            String entityName="cn.chahuyun.entity."+className;
            List<?> objects = HibernateUtil.factory.fromTransaction(session -> {
                try {
                    return session.createNativeQuery(sql, Class.forName(entityName)).list();
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            });

            // 暴力注入 bot ...
            for(Object o:objects){
                try {
                    Field bot = o.getClass().getDeclaredField("bot");
                    bot.setAccessible(true);
                    if(bot.get(o).equals(fromBotId)) {
                        bot.set(o, toBotId);
                        System.out.println(o);
                        HibernateUtil.factory.fromTransaction(session -> session.merge(o));
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

        }





    }

    //todo 导出 导入 excel

}
