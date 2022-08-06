package cn.chahuyun.utils;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.entity.GroupList;
import net.mamoe.mirai.utils.MiraiLogger;
import org.hibernate.SessionFactory;
import xyz.cssxsh.mirai.hibernate.MiraiHibernateConfiguration;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :hibernate
 * @Date 2022/7/30 22:47
 */
public class HibernateUtil {


    private final static MiraiLogger l = HuYanSession.INSTANCE.getLogger();

    /**
     * 数据库连接驱动
     */
    private static final String CLASS_NAME = "org.h2.Driver";

    /**
     * 数据库连接前缀
     */
    private static final String SQL_PATH_PREFIX = "jdbc:h2:file:";

    /**
     * 会话工厂
     */
    public static SessionFactory factory = null;

    /**
     * Hibernate初始化
     * @author Moyuyanli
     * @param configuration Configuration
     * @date 2022/7/30 23:04
     */
    public static void init(MiraiHibernateConfiguration configuration) {
        String path = SQL_PATH_PREFIX + "./data/cn.chahuyun.HuYanSession/HuYan";
        configuration.setProperty("hibernate.connection.url", path);
        configuration.scan("cn.chahuyun.entity");
        configuration.addAnnotatedClass(GroupList.class);
        factory = configuration.buildSessionFactory();
        l.info("H2数据库初始化成功!");
    }





}
