package cn.chahuyun.utils;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.enums.Mate;
import cn.chahuyun.files.ConfigData;
import cn.hutool.db.Db;
import cn.hutool.db.DbUtil;
import cn.hutool.db.Entity;
import cn.hutool.log.LogFactory;
import cn.hutool.log.level.Level;
import net.mamoe.mirai.utils.MiraiLogger;
import org.sqlite.SQLiteDataSource;

import java.sql.SQLException;
import java.util.List;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :糊涂数据库工具
 * @Date 2022/7/10 19:56
 */
public class HuToolUtil {

    private final static MiraiLogger l = HuYanSession.INSTANCE.getLogger();

    /**
     * 数据库连接驱动
     */
    private static final String CLASS_NAME = "org.sqlite.JDBC";

    /**
     * 数据库连接前缀
     */
    private static final String SQL_PATH_PREFIX = "jdbc:sqlite:";

    public static Db db;

    public static void init() {
        String path = SQL_PATH_PREFIX + HuYanSession.INSTANCE.getDataFolderPath() + "/HuYan.db";

        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(path);
        db = Db.use(dataSource, CLASS_NAME);
        DbUtil.setShowSqlGlobal(ConfigData.INSTANCE.getDebugSwitch(),false,true, Level.DEBUG);

        String createTableMateSql = "CREATE TABLE IF NOT EXISTS mate(" +
                "id INTEGER PRIMARY KEY autoincrement NOT NULL," +
                "mate_name TEXT," +
                "mate_type INT" +
                ");";
        String createTableListSql = "CREATE TABLE IF NOT EXISTS list(" +
                "id INTEGER PRIMARY KEY autoincrement NOT NULL," +
                "bot INT NOT NULL," +
                "list_id INT NOT NULL," +
                "group_id INT NOT NULL" +
                ");";
        String createTableScopeSql = "CREATE TABLE IF NOT EXISTS scope(" +
                "id INTEGER PRIMARY KEY autoincrement NOT NULL," +
                "bot INT NOT NULL ," +
                "scope_name TEXT NOT NULL," +
                "is_Group BLOB DEFAULT FALSE," +
                "is_Global BLOB DEFAULT FALSE," +
                "`group` INT DEFAULT 0," +
                "list_id INT DEFAULT 0" +
                "); ";
        String createTableSessionSql = "CREATE TABLE IF NOT EXISTS session(" +
                "id INTEGER PRIMARY KEY autoincrement NOT NULL," +
                "bot INT NOT NULL," +
                "type INT DEFAULT 0 ," +
                "key TEXT NOT NULL," +
                "value TEXT NOT NULL," +
                "list_id INT DEFAULT 0 ," +
                "scope_id TEXT NOT NULL" +
                ");";
//        String createTableSessionSql =
//        String createTableSessionSql =
//        String createTableSessionSql =


        try {
            db.executeBatch(createTableMateSql, createTableListSql, createTableScopeSql, createTableSessionSql);
        } catch (SQLException e) {
            l.error("初始化数据库表失败:" + e.getMessage());
            e.printStackTrace();
        }
        String queryMateSql = "SELECT * FROM mate";
        try {
            List<Entity> list = db.query(queryMateSql);
            if (list.isEmpty()) {
                String insertMateSql = "INSERT INTO mate(id,mate_name,mate_type) " +
                        "SELECT 1,'精准',1 UNION " +
                        "SELECT 2,'模糊',2 UNION " +
                        "SELECT 3,'头部',3 UNION " +
                        "SELECT 4,'结尾',4;";
                int execute = db.execute(insertMateSql);
                if (execute != 0) {
                    l.info("匹配方式数据初始化成功!");
                }
            }
        } catch (SQLException e) {
            l.error("初始化匹配方式失败:"+e.getMessage());
            e.printStackTrace();
        }
    }


}
