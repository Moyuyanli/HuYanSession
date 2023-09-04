package cn.chahuyun.session.utils;

import cn.chahuyun.session.HuYanSession;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import org.hibernate.SessionFactory;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :hibernate
 * @Date 2022/7/30 22:47
 */
public class HibernateUtil {


    /**
     * 会话工厂
     */
    public static SessionFactory factory = null;

    /**
     * Hibernate初始化
     *
     * @param factory 数据库工厂
     * @author Moyuyanli
     * @date 2022/7/30 23:04
     */
    public static void init(SessionFactory factory) {
        HibernateUtil.factory = factory;
    }

    /**
     * 通过参数从数据库中查询一个单实例
     *
     * @param params map参数集
     * @param c      对象类
     * @return T  单个实例
     * @author Moyuyanli
     * @date 2023/9/4 14:27
     */
    public static <T> T getSingleResult(Map<String, Object> params, Class<T> c) {
        return HibernateUtil.factory.fromTransaction(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<T> query = builder.createQuery(c);
            JpaRoot<T> from = query.from(c);

            query.select(from);
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                query.where(builder.equal(from.get(entry.getKey()), entry.getValue()));
            }
            return session.createQuery(query).getSingleResultOrNull();
        });
    }

    /**
     * 通过参数从数据库中查询所有实例
     *
     * @param params map参数集
     * @param c      对象类
     * @return List<T>  所有结果
     * @author Moyuyanli
     * @date 2023/9/4 14:27
     */
    public static <T> List<T> getList(Map<String, Object> params, Class<T> c) {
        return HibernateUtil.factory.fromTransaction(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<T> query = builder.createQuery(c);
            JpaRoot<T> from = query.from(c);
            query.select(from);
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                query.where(builder.equal(from.get(entry.getKey()), entry.getValue()));
            }
            return session.createQuery(query).list();
        });
    }


    /**
     * 手动更新数据库配置文件
     *
     * @param properties 配置文件
     * @return t 保存成功
     */
    public static void saveProperties(Properties properties) throws IORuntimeException {
        File tmp = FileUtil.createTempFile();
        FileUtil.appendLines(properties.entrySet(), tmp, "UTF-8");
        File file = HuYanSession.INSTANCE.resolveConfigFile("hibernate.properties");
        FileUtil.copy(tmp, file, true);
    }


}
