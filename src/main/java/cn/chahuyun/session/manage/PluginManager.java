package cn.chahuyun.session.manage;

import cn.chahuyun.session.HuYanSession;
import cn.chahuyun.session.utils.HibernateUtil;
import cn.hutool.core.io.FileUtil;
import xyz.cssxsh.mirai.hibernate.MiraiHibernateConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static cn.chahuyun.session.constant.Constant.*;
import static cn.chahuyun.session.HuYanSession.LOGGER;

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
        //加载旧配置
        File file = HuYanSession.INSTANCE.resolveConfigFile("hibernate.properties");
        List<String> strings = FileUtil.readLines(file, "UTF-8");

        MiraiHibernateConfiguration oldConfiguration = new MiraiHibernateConfiguration(install);
        for (String string : strings) {
            String[] split = string.split("=");
            oldConfiguration.setProperty(split[0], split[1]);
        }

        //加载新配置
        MiraiHibernateConfiguration configuration = new MiraiHibernateConfiguration(install);
        switch (HuYanSession.CONFIG.getDatabaseType()) {
            case MYSQL:
                mySqlBase(configuration);
                break;
            case SQLITE:
                LOGGER.warning("暂时不支持sqlite,默认使用H2");
            case H2:
            default:
                h2Base(configuration);
        }

        //对比配置
        if (!oldConfiguration.getProperty(HIBERNATE_CONNECTION_DRIVER_CLASS).equals(configuration.getProperty(HIBERNATE_CONNECTION_DRIVER_CLASS))) {
//            oldConfiguration.backup(oldConfiguration.getProperties());
            File oldFile = HuYanSession.INSTANCE.resolveConfigFile("hibernate.backup.properties");
            if (oldFile.exists()) {
                try {
                    if (oldFile.createNewFile()) {
                        FileUtil.copy(file, oldFile, true);
                    } else {
                        LOGGER.error("旧数据迁移失败！");
                    }
                } catch (IOException e) {
                    LOGGER.error("旧数据迁移失败！",e);
                }
            }
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
        configuration.setProperty("hibernate.connection.driver_class", HIBERNATE_CONNECTION_DRIVER_CLASS_H2);
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
        configuration.setProperty("hibernate.connection.driver_class", HIBERNATE_CONNECTION_DRIVER_CLASS_MYSQL);
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
