package cn.chahuyun.utils;

import cn.chahuyun.HuYanSession;
import net.mamoe.mirai.utils.MiraiLogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * SqliteUtil
 *
 * @author Zhangjiaxing
 * @description Sqlite的工具类
 * @date 2022/7/8 16:30
 */
public class SqliteUtil {

    private final static MiraiLogger L = HuYanSession.INSTANCE.getLogger();
    private final static SqliteUtil INSTANCE = new SqliteUtil();

    /**
     * 数据库连接驱动
     */
    private final String CLASS_NAME = "org.sqlite.JDBC";

    /**
     * 数据库连接前缀
     */
    private final String SQL_PATH_PREFIX = "jdbc:sqlite:";

    /**
     * 获取数据库连接
     * @author zhangjiaxing
     * @date 2022/7/8 17:13
     * @return java.sql.Connection
     */
    public Connection getConnection() {
        String path = SQL_PATH_PREFIX + HuYanSession.INSTANCE.getDataFolderPath()+"HuYan.db";
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(path);
        } catch (SQLException e) {
            L.info("数据库连接失败:"+e.getMessage());
        }
        return connection;
    }

    public void init() {
        



    }




}