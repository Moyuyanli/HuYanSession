package cn.chahuyun.session.manage;

import cn.chahuyun.session.HuYanSession;
import cn.chahuyun.session.utils.HibernateUtil;
import cn.hutool.core.io.FileUtil;
import org.hibernate.HibernateException;
import xyz.cssxsh.mirai.hibernate.MiraiHibernateConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.module.Configuration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static cn.chahuyun.session.HuYanSession.LOGGER;
import static cn.chahuyun.session.constant.Constant.*;

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
    private static final String DEFAULT_H2_BASE_PATH = "jdbc:h2:file:./data/cn.chahuyun.HuYanSession/hibernate.h2";

    public static void init(HuYanSession install) {
        //加载新配置
        MiraiHibernateConfiguration configuration = new MiraiHibernateConfiguration(install);
        Properties properties;
        String log = "";
        //noinspection AlibabaSwitchStatement
        switch (HuYanSession.CONFIG.getDatabaseType()) {
            case MYSQL:
                properties = mySqlBase(configuration);
                log = "MYSQL";
                break;
            case SQLITE:
                LOGGER.warning("暂时不支持sqlite,默认使用H2");
            case H2:
                log = "H2";
            default:
                properties = h2Base(configuration);
        }

        if (false) {
            transfer(install, configuration);
        }

        try {
            if (HuYanSession.CONFIG.isFirstLoad()) {
                HibernateUtil.saveProperties(properties);
            }
            //初始化插件数据库
            HibernateUtil.init(configuration.buildSessionFactory());
        } catch (HibernateException e) {
            LOGGER.error("数据库加载失败，请重新加载！", e);
            return;
        }
        LOGGER.info(log + "数据库初始化成功!");
    }

    /**
     * H2数据库配置
     *
     * @param configuration config
     */
    private static Properties h2Base(MiraiHibernateConfiguration configuration) {
        Properties properties = new Properties();
        properties.setProperty("hibernate.connection.url", H2_BASE_PATH);
        properties.setProperty("hibernate.connection.driver_class", HIBERNATE_CONNECTION_DRIVER_CLASS_H2);
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        properties.setProperty("hibernate.connection.provider_class", "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
        properties.setProperty("hibernate.hikari.connectionTimeout", "180000");
        properties.setProperty("hibernate.connection.isolation", "1");
        properties.setProperty("hibernate.hbm2ddl.auto", "update");
        properties.setProperty("hibernate-connection-autocommit", "true");
        properties.setProperty("hibernate.autoReconnect", "true");
        properties.setProperty("hibernate.connection.username", "");
        properties.setProperty("hibernate.connection.password", "");
        properties.setProperty("hibernate.current_session_context_class", "thread");
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            configuration.setProperty((String) entry.getKey(), (String) entry.getValue());
        }
        return properties;
    }

    /**
     * MySql数据库配置
     *
     * @param configuration config
     */
    private static Properties mySqlBase(MiraiHibernateConfiguration configuration) {
        Properties properties = new Properties();
        properties.setProperty("hibernate.connection.url", MYSQL_BASE_PATH);
        properties.setProperty("hibernate.connection.driver_class", HIBERNATE_CONNECTION_DRIVER_CLASS_MYSQL);
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        properties.setProperty("hibernate.connection.CharSet", "utf8mb4");
        properties.setProperty("hibernate.connection.useUnicode", "true");
        properties.setProperty("hibernate.connection.username", "root");
        properties.setProperty("hibernate.connection.password", HuYanSession.CONFIG.getMysqlPwd());
        properties.setProperty("hibernate.connection.provider_class", "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
        properties.setProperty("hibernate.connection.isolation", "1");
        properties.setProperty("hibernate.hbm2ddl.auto", "update");
        properties.setProperty("hibernate.autoReconnect", "true");
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            configuration.setProperty((String) entry.getKey(), (String) entry.getValue());
        }
        return properties;
    }


    /**
     * 数据迁移
     *
     * @param install 插件
     * @param configuration 配置
     * @author Moyuyanli
     * @date 2023/9/7 14:53
     */
    private static void transfer(HuYanSession install, MiraiHibernateConfiguration configuration) {
        //加载旧配置
        File file = HuYanSession.INSTANCE.resolveConfigFile("hibernate.properties");
        MiraiHibernateConfiguration oldConfiguration = new MiraiHibernateConfiguration(install);
        boolean exists = file.exists();
        if (exists) {
            List<String> strings = FileUtil.readLines(file, "UTF-8");
            for (String string : strings) {
                int i = string.indexOf("=");
                oldConfiguration.setProperty(string.substring(0, i), string.substring(i + 1));
            }
        }

        if (oldConfiguration.getProperty(HIBERNATE_CONNECTION_URL).equals(DEFAULT_H2_BASE_PATH)) {
            exists = false;
        }

        //对比配置
        if (exists && !oldConfiguration.getProperty(HIBERNATE_CONNECTION_URL).equals(configuration.getProperty(HIBERNATE_CONNECTION_URL))) {
            File oldFile = HuYanSession.INSTANCE.resolveConfigFile("hibernate.backup.properties");
            try {
                if (!oldFile.exists()) {
                    if (!oldFile.createNewFile()) {
                        LOGGER.error("旧数据配置文件创建失败,请手动创建!");
                    }
                }
                FileUtil.copy(oldFile, file, true);
                //todo 转移数据报错，目前无法解决
                configuration.restore(oldConfiguration.getProperties());
            } catch (IOException e) {
                LOGGER.error("旧数据迁移失败！");
                return;
            }
            LOGGER.error("旧数据迁移成功！");
        }
    }


}
