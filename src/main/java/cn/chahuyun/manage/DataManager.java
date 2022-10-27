package cn.chahuyun.manage;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.entity.*;
import cn.chahuyun.utils.HibernateUtil;
import cn.hutool.poi.excel.BigExcelWriter;
import cn.hutool.poi.excel.ExcelUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
//        List<Object> entityList = new ArrayList<>();
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
//        entityList.add(new Session());
//        entityList.add(new WelcomeMessage());

        BigExcelWriter writer = ExcelUtil.getBigWriter(HuYanSession.INSTANCE.resolveDataPath("HuYan.xlsx").toString(), "session");

        //sessionExcel 显示字段
        Map<String, String> sessionAlias = new LinkedHashMap<>();
        sessionAlias.put("bot", "所属bot");
        sessionAlias.put("mateInter", "匹配方式");
        sessionAlias.put("term", "触发词");
        sessionAlias.put("reply", "回复词");
        sessionAlias.put("scopeMark", "作用域");
        //强制输出指定字段
        writer.setOnlyAlias(true);

        List<Session> sessions = HibernateUtil.factory.fromTransaction(session -> {
            JpaCriteriaQuery<Session> query = session.getCriteriaBuilder().createQuery(Session.class);
            query.select(query.from(Session.class));
            List<Session> sessionListTemp = session.createQuery(query).list();
            for (Session entity : sessionListTemp) {
                if (entity.getType() == 5) {
                    entity.setReply("转发消息 或 音频消息");
                }
            }
            return sessionListTemp;
        });
        //设定列标题
        writer.setHeaderAlias(sessionAlias);
        //设定标题
        writer.merge(4, "对话信息");
        //写入数据
        writer.write(sessions, true);
        //自动格式化宽度
        writer.autoSizeColumnAll();

        //切换Sheet
        writer.setSheet("manySessionInfo");

        Map<String, String> manySessionInfoAlias = new LinkedHashMap<>();
        manySessionInfoAlias.put("id", "id");
        manySessionInfoAlias.put("bot", "所属bot");
        manySessionInfoAlias.put("mateType", "匹配方式");
        manySessionInfoAlias.put("random", "是否随机");
        manySessionInfoAlias.put("scopeMark", "作用域");

        writer.setHeaderAlias(manySessionInfoAlias);
        writer.merge(4, "多回复消息主信息");

        List<ManySessionInfo> manySessionInfos = HibernateUtil.factory.fromTransaction(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<ManySessionInfo> query = builder.createQuery(ManySessionInfo.class);
            query.select(query.from(ManySessionInfo.class));
            return session.createQuery(query).list();
        });
        writer.write(manySessionInfos, true);
        writer.autoSizeColumnAll();


        writer.setSheet("manySession");

        Map<String, String> manySessionAlias = new LinkedHashMap<>();
        manySessionAlias.put("id", "id");
        manySessionAlias.put("bot", "所属bot");
        manySessionAlias.put("ManySession_ID", "匹配多词条主表id");
        manySessionAlias.put("reply", "回复内容");

        writer.setHeaderAlias(manySessionAlias);
        writer.merge(4, "多回复消息内容信息");

        List<ManySession> manySessions = HibernateUtil.factory.fromTransaction(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<ManySession> query = builder.createQuery(ManySession.class);
            query.select(query.from(ManySession.class));
            List<ManySession> manySessionsTemp = session.createQuery(query).list();
            for (ManySession manySession : manySessionsTemp) {
                if (manySession.isOther()) {
                    manySession.setReply("转发消息 或 音频消息");
                }
            }
            return manySessionsTemp;
        });
        List<ManySession> collect = manySessions.stream().filter(it -> it.getQuartzMessage_ID() == null).collect(Collectors.toList());

        writer.write(collect, true);
        writer.autoSizeColumnAll();

        writer.setSheet("quartzInfo");

        Map<String, String> quartzInfoAlias = new LinkedHashMap<>();
        quartzInfoAlias.put("id", "id");
        quartzInfoAlias.put("bot", "所属bot");
        quartzInfoAlias.put("name", "定时器名称");
        quartzInfoAlias.put("status", "是否开启");
        quartzInfoAlias.put("String", "定时器cron表达式");
        quartzInfoAlias.put("polling", "是否轮询");
        quartzInfoAlias.put("random", "是否随机");
        quartzInfoAlias.put("reply", "回复消息");
        quartzInfoAlias.put("scopeMark", "作用域标识");

        writer.setHeaderAlias(quartzInfoAlias);
        writer.merge(8, "定时器信息");

        List<QuartzInfo> quartzInfos = HibernateUtil.factory.fromTransaction(session -> {
            JpaCriteriaQuery<QuartzInfo> query = session.getCriteriaBuilder().createQuery(QuartzInfo.class);
            query.select(query.from(QuartzInfo.class));
            List<QuartzInfo> quartzInfoListTemp = session.createQuery(query).list();
            for (QuartzInfo quartzInfo : quartzInfoListTemp) {
                if (quartzInfo.isOther()) {
                    quartzInfo.setReply("转发消息 或 音频消息");
                }
            }
            return quartzInfoListTemp;
        });

        writer.write(quartzInfos, true);
        writer.autoSizeColumnAll();

        writer.setSheet("quartzSession");

        Map<String, String> quartzSessionAlias = new LinkedHashMap<>();
        quartzSessionAlias.put("id", "id");
        quartzSessionAlias.put("bot", "所属bot");
        quartzSessionAlias.put("QuartzMessage_ID", "匹配定时器主表id");
        quartzSessionAlias.put("reply", "回复内容");

        writer.setHeaderAlias(quartzSessionAlias);
        writer.merge(4, "多回复消息内容信息");

        List<ManySession> quarztManySessions = HibernateUtil.factory.fromTransaction(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<ManySession> query = builder.createQuery(ManySession.class);
            query.select(query.from(ManySession.class));
            List<ManySession> manySessionsTemp = session.createQuery(query).list();
            for (ManySession manySession : manySessionsTemp) {
                if (manySession.isOther()) {
                    manySession.setReply("转发消息 或 音频消息");
                }
            }
            return manySessionsTemp;
        });
        List<ManySession> quartzSession = quarztManySessions.stream().filter(it -> it.getManySession_ID() == null).collect(Collectors.toList());

        writer.write(quartzSession, true);
        writer.autoSizeColumnAll();

        writer.setSheet("groupList");

        Map<String, String> groupListAlias = new LinkedHashMap<>();
        groupListAlias.put("bot", "所属bot");
        groupListAlias.put("listId", "群组编号");
        groupListAlias.put("groups", "群列表");

        writer.setHeaderAlias(groupListAlias);
        writer.merge(2, "群组信息");

        List<GroupList> groupLists = HibernateUtil.factory.fromTransaction(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<GroupList> query = builder.createQuery(GroupList.class);
            query.select(query.from(GroupList.class));
            return session.createQuery(query).list();
        });
        writer.write(groupLists, true);
        writer.autoSizeColumnAll();

        //todo 到群欢迎词了
        writer.setSheet("groupWelcome");


        writer.close();


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
