package cn.chahuyun.utils;

import cn.chahuyun.entity.Scope;
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


    /**
     * 判断该作用是是否存在
     *
     * @param scope 作用域
     * @return boolean  true 不存在
     * @author Moyuyanli
     * @date 2022/8/12 15:57
     */
    public static boolean isScopeEmpty(Scope scope) {
        return HibernateUtil.factory.fromTransaction(session -> {
            String id = scope.getId();
            return session.get(Scope.class, id) == null;
        });
    }

    /**
     * 获取作用域
     *
     * @param scopeMark 作用域标识
     * @return cn.chahuyun.entity.Scope
     * @author Moyuyanli
     * @date 2022/8/12 16:00
     */
    public static Scope getScope(String scopeMark){
        return HibernateUtil.factory.fromTransaction(session -> session.get(Scope.class, scopeMark));
    }


}