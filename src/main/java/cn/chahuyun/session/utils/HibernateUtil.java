package cn.chahuyun.session.utils;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import xyz.cssxsh.mirai.hibernate.MiraiHibernateConfiguration;

import static cn.chahuyun.session.HuYanSession.log;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :hibernate
 * @Date 2022/7/30 22:47
 */
public class HibernateUtil {


    /**
     * 数据库连接前缀
     */
    private static final String H2_BASE_PATH = "jdbc:h2:file:./data/cn.chahuyun.HuYanSession/HuYan";
    private static final String MYSQL_BASE_PATH = "jdbc:mysql://localhost:3306/huyansession2?autoReconnect=true";

    /**
     * 会话工厂
     */
    public static SessionFactory factory = null;

    /**
     * Hibernate初始化
     *
     * @param configuration Configuration
     * @author Moyuyanli
     * @date 2022/7/30 23:04
     */
    public static void init(MiraiHibernateConfiguration configuration) {
        configuration.setProperty("hibernate.connection.url", MYSQL_BASE_PATH);
//        configuration.scan("cn.chahuyun.session.entity");
        try {
            factory = configuration.buildSessionFactory();
        } catch (HibernateException e) {
            log.error("请删除data中的HuYan.mv.db后重新启动！", e);
            return;
        }
        log.info("H2数据库初始化成功!");
    }


}
