package cn.chahuyun.utils;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.files.ConfigData;
import net.mamoe.mirai.utils.MiraiLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static kotlin.reflect.jvm.internal.impl.builtins.StandardNames.FqNames.string;

/**
 * SqliteUtil
 *
 * @author Moyuyanli
 * @description Sqlite的工具类
 * @date 2022/7/8 16:30
 */
public class SqliteUtil {

    public final static SqliteUtil INSTANCE = new SqliteUtil();
    private final static MiraiLogger l = HuYanSession.INSTANCE.getLogger();

    /**
     * 数据库连接驱动
     */
    private final String CLASS_NAME = "org.sqlite.JDBC";

    /**
     * 数据库连接前缀
     */
    private final String SQL_PATH_PREFIX = "jdbc:sqlite:";

    /**
     * 统一资源
     */
    private Connection connection;
    private Statement statement;

    /**
     * 获取数据库连接
     *
     * @return java.sql.Connection
     * @author Moyuyanli
     * @date 2022/7/8 17:13
     */
    private Connection getConnection() {
        String path = SQL_PATH_PREFIX + HuYanSession.INSTANCE.getDataFolderPath() + "/HuYan.db";
        Connection connection = null;
        try {
            Class.forName(CLASS_NAME);
            connection = DriverManager.getConnection(path);
        } catch (SQLException e) {
            l.error("数据库连接失败:" + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            l.error("数据库驱动加载失败:" + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            l.error("数据库加载失败:" + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * 初始化数据库
     *
     * @author Moyuyanli
     * @date 2022/7/8 21:16
     */
    public void init() {
        Connection connection = getConnection();

        if (connection == null) {
            l.error("数据库初始化失败！");
            return;
        }

        Statement statement = null;
        try {
            statement = connection.createStatement();
        } catch (SQLException e) {
            l.info("数据库创建初始化声明失败:" + e.getMessage());
            e.printStackTrace();
        }
        String createSessionSql =
                "CREATE TABLE IF NOT EXISTS mate(" +
                        "id INTEGER PRIMARY KEY autoincrement NOT NULL," +
                        "mate_name TEXT," +
                        "mate_type INT" +
                        ");" +
                        "CREATE TABLE IF NOT EXISTS list(" +
                        "id INTEGER PRIMARY KEY autoincrement NOT NULL," +
                        "bot INT NOT NULL," +
                        "list_id INT NOT NULL," +
                        "group_id INT NOT NULL" +
                        ");" +
                        "CREATE TABLE IF NOT EXISTS scope(" +
                        "id INTEGER PRIMARY KEY autoincrement NOT NULL," +
                        "bot INT NOT NULL ," +
                        "scope_name TEXT NOT NULL," +
                        "isGroup BLOB DEFAULT FALSE," +
                        "isGlobal BLOB DEFAULT FALSE," +
                        "`group` INT DEFAULT 0," +
                        "list_id INT DEFAULT 0" +
                        "); " +
                        "CREATE TABLE IF NOT EXISTS session(" +
                        "id INTEGER PRIMARY KEY autoincrement NOT NULL," +
                        "bot INT NOT NULL," +
                        "type INT DEFAULT 0 ," +
                        "key TEXT NOT NULL," +
                        "value TEXT NOT NULL," +
                        "list_id INT DEFAULT 0 ," +
                        "scope_id TEXT NOT NULL" +
                        ");";

        try {
            assert statement != null;
            statement.executeUpdate(createSessionSql);
        } catch (SQLException | AssertionError e) {
            l.error("创建表失败 :" + e.getMessage());
            e.printStackTrace();
        }

        if (statement == null) {
            l.error("数据库声明为空!");
            return;
        }

        String mateSql = "SELECT * FROM mate ;";
        String insertMateSql = "INSERT INTO mate(id,mate_name,mate_type) " +
                "SELECT 1,'精准',1 UNION " +
                "SELECT 2,'模糊',2 UNION " +
                "SELECT 3,'头部',3 UNION " +
                "SELECT 4,'结尾',4;";
        try {
            ResultSet set = statement.executeQuery(mateSql);
            if (!set.next()) {
                int update = statement.executeUpdate(insertMateSql);
                int succeed = 4;
                if (update != succeed) {
                    l.error("匹配方式数据初始化失败！");
                    return;
                }
            }
        } catch (SQLException e) {
            l.error("数据库基础数据初始化失败！");
            e.printStackTrace();
            return;
        }

        if (!closeConnectionAndStatement(statement, connection)) {
            l.error("数据库初始化失败!");
            return;
        }
        l.info("数据库初始化成功！");

    }

    /**
     * 关闭数据库连接
     *
     * @param statement  事务
     * @param connection 连接
     * @return boolean
     * @author Moyuyanli
     * @date 2022/7/9 19:11
     */
    private boolean closeConnectionAndStatement(Statement statement, Connection connection) {
        try {
            statement.close();
            connection.close();
        } catch (SQLException e) {
            l.error("数据库连接关闭失败！" + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 关闭数据库连接
     *
     * @return boolean
     * @author Moyuyanli
     * @date 2022/7/9 19:11
     */
    public boolean closeConnectionAndStatement() {
        try {
            statement.close();
            connection.close();
        } catch (SQLException e) {
            l.error("数据库连接关闭失败！" + e.getMessage());
            e.printStackTrace();
            return false;
        }
        l.info("关闭成功!");
        return true;
    }

    /**
     * 操作数据
     *
     * @param string sql语句
     * @return int  0=失败
     * @author Moyuyanli
     * @date 2022/7/9 17:34
     */
    public int updateData(StringBuffer string) {
        String sql = string.toString();
        if (ConfigData.INSTANCE.getDebugSwitch()) {
            l.info("sql-> " + sql);
        }
        Connection connection = getConnection();
        int count = 0;
        if (connection != null) {
            try {
                statement = connection.createStatement();
                if (statement == null) {
                    return count;
                }
                count = statement.executeUpdate(sql);
                closeConnectionAndStatement(statement, connection);
            } catch (SQLException e) {
                l.error("数据操作失败:" + e.getMessage());
                e.printStackTrace();
            }
        }
        return count;
    }

    /**
     * 查询
     * @author Moyuyanli
     * @param stringBuffer sql
     * @date 2022/7/10 0:20
     * @return java.sql.ResultSet
     */
    public ResultSet queryData(StringBuffer stringBuffer) {
        String sql = stringBuffer.toString();
        if (ConfigData.INSTANCE.getDebugSwitch()) {
            l.info("sql-> " + sql);
        }
         connection = getConnection();
        ResultSet resultSet = null;
        if (connection != null) {
            try {
                statement = connection.createStatement();
                if (statement == null) {
                    return null;
                }
                l.info("查询!");
                resultSet = statement.executeQuery(sql);

                l.info("1-"+resultSet.getMetaData());
            } catch (SQLException e) {
                l.error("数据查询失败:" + e.getMessage());
                e.printStackTrace();
            }
        }
        return resultSet;
    }


}