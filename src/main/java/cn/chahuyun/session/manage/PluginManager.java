package cn.chahuyun.session.manage;

import cn.chahuyun.session.HuYanSession;
import cn.chahuyun.session.utils.HibernateUtil;
import xyz.cssxsh.mirai.hibernate.MiraiHibernateConfiguration;

/**
 * 插件管理
 *
 * @author Moyuyanli
 * @date 2023/8/3 14:28
 */
public class PluginManager {
    /**
     * 数据库连接前缀
     */
    private static final String H2_BASE_PATH = "jdbc:h2:file:./data/cn.chahuyun.HuYanSession/HuYan";
    private static final String MYSQL_BASE_PATH = "jdbc:mysql://localhost:3306/huyan?autoReconnect=true";

    public static void init(HuYanSession install) {
        //加载前置
        MiraiHibernateConfiguration configuration = new MiraiHibernateConfiguration(install);
        switch (HuYanSession.CONFIG.getDatabaseType()) {
            case MYSQL:
                mySqlBase(configuration);
                break;
            case SQLITE:
            default:
                h2Base(configuration);
        }
        //初始化插件数据库
        HibernateUtil.init(configuration);
    }

    /**
     * H2数据库配置
     *
     * @param configuration config
     */
    private static void h2Base(MiraiHibernateConfiguration configuration) {
        configuration.setProperty("hibernate.connection.url", H2_BASE_PATH);
        configuration.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        configuration.setProperty("hibernate.connection.provider_class", "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
        configuration.setProperty("hibernate.connection.isolation", "1");
        configuration.setProperty("hibernate.hbm2ddl.auto", "update");
        configuration.setProperty("hibernate-connection-autocommit", "true");
    }
    /**
     * MySql数据库配置
     *
     * @param configuration config
     */
    private static void mySqlBase(MiraiHibernateConfiguration configuration) {
        configuration.setProperty("hibernate.connection.url", MYSQL_BASE_PATH);
        configuration.setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
        configuration.setProperty("hibernate.connection.CharSet", "utf8mb4");
        configuration.setProperty("hibernate.connection.useUnicode", "true");
        configuration.setProperty("hibernate.connection.username", "root");
        configuration.setProperty("hibernate.connection.password", HuYanSession.CONFIG.getMysqlPwd());
        configuration.setProperty("hibernate.connection.provider_class", "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
        configuration.setProperty("hibernate.connection.isolation", "1");
        configuration.setProperty("hibernate.hbm2ddl.auto", "update");
        configuration.setProperty("hibernate.autoReconnect", "true");
    }


}
