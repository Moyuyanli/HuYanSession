package cn.chahuyun.utils;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.entity.GroupList;
import cn.chahuyun.entity.Scope;
import net.mamoe.mirai.contact.ContactList;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.utils.MiraiLogger;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.util.List;

/**
 * ScopeUtil
 *
 * @author Moyuyanli
 * @description 作用域Util
 * @date 2022/7/11 12:16
 */
public class ScopeUtil {

    private final static MiraiLogger l = HuYanSession.INSTANCE.getLogger();

    /**
     * 判断该作用是是否存在
     *
     * @param scope 作用域
     * @return boolean  t 存在
     * @author Moyuyanli
     * @date 2022/8/12 15:57
     */
    public static boolean isScopeEmpty(Scope scope) {
        List<Scope> scopeList = HibernateUtil.factory.fromTransaction(session -> {
            String id = scope.getId();
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<Scope> query = builder.createQuery(Scope.class);
            JpaRoot<Scope> from = query.from(Scope.class);
            query.select(from);
            query.where(builder.equal(from.get("id"), id));
            return session.createQuery(query).list();
        });
        if (scopeList == null || scopeList.isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * 获取作用域
     *
     * @param scopeMark 作用域标识
     * @return cn.chahuyun.entity.Scope
     * @author Moyuyanli
     * @date 2022/8/12 16:00
     */
    public static Scope getScope(String scopeMark) {
        List<Scope> scopeList = HibernateUtil.factory.fromTransaction(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<Scope> query = builder.createQuery(Scope.class);
            JpaRoot<Scope> from = query.from(Scope.class);
            query.select(from);
            query.where(builder.equal(from.get("id"), scopeMark));
            return session.createQuery(query).list();
        });
        if (scopeList == null || scopeList.isEmpty()) {
            return null;
        }
        return scopeList.get(0);
    }


}