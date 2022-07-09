package cn.chahuyun.utils;

import cn.chahuyun.HuYanSession;
import net.mamoe.mirai.utils.MiraiLogger;

import java.sql.*;
import java.util.ArrayList;
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
     * 获取数据库连接
     * @author Moyuyanli
     * @date 2022/7/8 17:13
     * @return java.sql.Connection
     */
    private Connection getConnection() {
        String path = SQL_PATH_PREFIX + HuYanSession.INSTANCE.getDataFolderPath()+"/HuYan.db";
        Connection connection = null;
        try {
            Class.forName(CLASS_NAME);
            connection = DriverManager.getConnection(path);
        } catch (SQLException e) {
            l.error("数据库连接失败:"+e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            l.error("数据库驱动加载失败:"+e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * 初始化数据库
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
            l.info("数据库创建初始化事务失败:" + e.getMessage());
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
            l.error("创建表失败 :" + e.getMessage() );
            e.printStackTrace();
        }

        if (statement == null) {
            l.error("数据库事务为空!");
            return;
        }
//        closeConnectionAndStatement(statement, connection);
//        connection = getConnection();
        String mateSql = "SELECT * FROM mate ;";
        String insertMateSql = "INSERT INTO mate(id,mate_name,mate_type) " +
                "SELECT 1,'精准',1 UNION " +
                "SELECT 2,'模糊',2 UNION " +
                "SELECT 3,'头部',3 UNION " +
                "SELECT 4,'结尾',4;";
        try {
//            statement = connection.createStatement();
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
     * @author Moyuyanli
     * @param statement 事务
     * @param connection 连接
     * @date 2022/7/9 19:11
     * @return boolean
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
     * 操作数据
     * @author Moyuyanli
     * @param string sql语句
     * @param params where参数
     * @date 2022/7/9 17:34
     * @return int  0=失败
     */
    public int updateData(StringBuffer string,Object[] params) {
        String sql = string.toString();
        Connection connection = getConnection();
        int count = 0;
        if (connection != null) {
            try {
                PreparedStatement statement = connection.prepareStatement(sql);
                if (statement == null) {
                    return count;
                }
                for (int i = 0; params != null && i < params.length; i++) {
                    Object param = params[i];
                    statement.setObject(i,param);
                }
                count = statement.executeUpdate();
                closeConnectionAndStatement(statement, connection);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return count;
    }

    public ResultSet queryData(StringBuffer stringBuffer, Object ...params ) {
        String sql = string.toString();
        Connection connection = getConnection();
        ResultSet resultSet = null;
        if (connection != null) {
            try {
                PreparedStatement statement = connection.prepareStatement(sql);
                if (statement == null) {
                    return null;
                }
                for (int i = 0; params != null && i < params.length; i++) {
                    Object param = params[i];
                    statement.setObject(i,param);
                }
                resultSet = statement.executeQuery();
                closeConnectionAndStatement(statement, connection);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return resultSet;
    }


}