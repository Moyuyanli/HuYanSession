package cn.chahuyun.session.utils;

import cn.chahuyun.session.entity.Scope;

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
     * @return cn.chahuyun.session.entity.Scope
     * @author Moyuyanli
     * @date 2022/8/12 16:00
     */
    public static Scope getScope(String scopeMark) {
        Scope scope = HibernateUtil.factory.fromTransaction(session -> session.get(Scope.class, scopeMark));
        if (scope != null) {
            return scope;
        }

        String[] split = scopeMark.split("\\.");
        long bot = Long.parseLong(split[0]);
        if (split.length == 1) {
            return new Scope(bot, "全局", true, false, 0, 0);
        } else {
            String s = split[1];
            if (s.contains("gr")) {
                String gr = s.replace("gr", "");
                int anInt = Integer.parseInt(gr);
                return new Scope(bot, "群组", false, true, 0, anInt);
            } else {
                return new Scope(bot, "当前群", false, false, Long.parseLong(s), 0);
            }
        }
    }


}