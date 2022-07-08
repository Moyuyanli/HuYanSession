package cn.chahuyun.utils;

import cn.chahuyun.HuYanSession;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.utils.MiraiLogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

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
            Class.forName(CLASS_NAME);
            connection = DriverManager.getConnection(path);
        } catch (SQLException e) {
            L.error("数据库连接失败:"+e.getMessage());
        } catch (ClassNotFoundException e) {
            L.error("数据库驱动加载失败:"+e.getMessage());
        }
        return connection;
    }

    /**
     * 初始化数据库
     * @author zhangjiaxing
     * @date 2022/7/8 21:16
     */
    public void init() {
        Connection connection = getConnection();

        if (connection == null) {
            L.error("数据库初始化失败！");
            return;
        }

        Statement statement = null;
        try {
            statement = connection.createStatement();
        } catch (SQLException e) {
            L.info("数据库创建初始化事务失败:" + e.getMessage());
        }
        String createSessionSql = "CREATE TABLE session(" +
                "id INT PRIMARY KEY autoincrement NOT NULL," +
                "bot INT NOT NULL," +
                "type INT default 0 ," +
                "key TEXT NOT NULL," +
                "value TEXT NOT NULL," +
                "scopeName TEXT NOT NULL," +
                "isGroup BLOB NOT NULL," +
                "isGlobal BLOB NOT NULL," +
                "group INT NOT NULL," +
                "listId INT default 0)";

        try {
            assert statement != null;
            statement.executeUpdate(createSessionSql);
        } catch (SQLException | AssertionError e) {
            L.error("创建表 session 失败 :" + e.getMessage() );
        }

        if (statement == null) {
            L.error("数据库事务为空!");
            return;
        }
        if (!closeConnectionAndStatement(statement, connection)) {
            L.error("数据库初始化失败!");
            return;
        }
        L.info("数据库初始化成功！");

    }


    private boolean closeConnectionAndStatement(Statement statement, Connection connection) {
        try {
            statement.close();
            connection.close();
        } catch (SQLException e) {
            L.error("数据库连接关闭失败！" + e.getMessage());
            return false;
        }
        return true;
    }

    public int addData(long bot,String sql,) {

    }


}