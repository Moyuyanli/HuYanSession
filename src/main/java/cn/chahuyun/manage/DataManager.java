package cn.chahuyun.manage;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.entity.Scope;
import cn.chahuyun.entity.Session;
import cn.chahuyun.utils.HibernateUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.poi.excel.BigExcelWriter;
import cn.hutool.poi.excel.ExcelUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import org.hibernate.query.criteria.JpaCriteriaQuery;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据管理 导出 导入 excel
 * 数据转移 所属 机器人的信息转移
 *
 * @author Moyuyanli
 * @date 2022/10/11 14:57
 */
public class DataManager {

    //todo 所属 机器人的信息转移

    private static boolean status = true;

    /**
     * 将 fromBotId 的数据迁移到 toBotId 中
     * 更改了数据库中的 bot 字段
     *
     * @param fromBotId 所属bot
     * @param toBotId   转移bot
     * @author forDecember
     * @date 2022/10/11 18:43
     */
    public static void transferInfo(Long fromBotId, Long toBotId) {

        if (isStatus()) {
            return;
        }

        // 获取表名
        List<String> tables = HibernateUtil.factory.fromTransaction(session -> session.createNativeQuery("show tables", String.class).list());

        // 穷举实体类
        String[] entity = {"BlackHouse", "Blacklist", "GroupInfo", "GroupList", "GroupProhibited", "GroupWelcomeInfo", "ManySession", "ManySessionInfo", "Power", "QuartzInfo", "Scope", "Session", "WelcomeMessage"};

        // 遍历表，找到与表名对应的实体类并进行映射
        for (String table : tables) {
            String className = null;
            boolean find = false;
            for (String s : entity) {
                // 用contains判断会让 Session 和 ManySession 冲突
                // 类顺序改变会导致 ManySession 和 ManySessionInfo 错误赋值
                // 总之下面这段代码很寄
                if (s.equalsIgnoreCase(table) || s.equalsIgnoreCase(table + "info")) {
                    className = s;
                    find = true;
                    break;
                }
            }
            if (!find) throw new RuntimeException("表与实体类不对应...");

            // 获取'当前遍历到的表'中的'类的实例们'
            String sql = "select * from " + table;
            String entityName = "cn.chahuyun.entity." + className;
            List<?> objects = HibernateUtil.factory.fromTransaction(session -> {
                try {
                    return session.createNativeQuery(sql, Class.forName(entityName)).list();
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            });

            // 暴力注入 bot ...
            for (Object o : objects) {
                try {
                    Field bot = o.getClass().getDeclaredField("bot");
                    bot.setAccessible(true);
                    if (bot.get(o).equals(fromBotId)) {
                        bot.set(o, toBotId);
                        System.out.println(o);
                        HibernateUtil.factory.fromTransaction(session -> session.merge(o));
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

        }
        //延迟两秒保证唯一触发
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        setStatus(true);

    }

    //todo 导出 导入 excel

    /**
     * 导出数据
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/10/16 17:22
     */
    public static void outputData(MessageEvent event) {
        Contact subject = event.getSubject();

//        Set<Class<?>> entity = ClassUtil.scanPackage("*");
        List<Object> entityList = new ArrayList<>();
//        entityList.add(new BlackHouse());
//        entityList.add(new Blacklist());
//        entityList.add(new GroupInfo());
//        entityList.add(new GroupList());
//        entityList.add(new GroupProhibited());
//        entityList.add(new GroupWelcomeInfo());
//        entityList.add(new ManySession());
//        entityList.add(new ManySessionInfo());
//        entityList.add(new Power());
//        entityList.add(new QuartzInfo());
        entityList.add(new Session());
//        entityList.add(new WelcomeMessage());

        //excel 标题
        Map<String, String> alias = new HashMap<>();
        alias.put("bot", "所属bot");
        alias.put("mateInter", "匹配方式");
        alias.put("term", "触发词");
        alias.put("reply", "回复词");
        alias.put("scopeMark", "作用域");

        Class<?>[] classes = ClassUtil.getClasses(entityList.toArray());

        BigExcelWriter writer = ExcelUtil.getBigWriter(HuYanSession.INSTANCE.resolveDataPath("HuYan.xlsx").toString(), "会话信息");
        writer.setOnlyAlias(true);
        for (Class<?> aClass : classes) {
            if (aClass == Scope.class) {
                break;
            }

            List<Object> list = HibernateUtil.factory.fromTransaction(session -> {
                JpaCriteriaQuery<Object> query = session.getCriteriaBuilder().createQuery();
                query.select(query.from(aClass));
                return session.createQuery(query).list();
            });

            writer.setHeaderAlias(alias);
            writer.merge(5, "对话信息");
            writer.write(list, true);
            writer.autoSizeColumnAll();
            writer.close();
        }

    }

    public synchronized static boolean isStatus() {
        if (status) {
            status = false;
            return false;
        } else {
            return true;
        }
    }

    public synchronized static void setStatus(boolean status) {
        DataManager.status = status;
    }


}
