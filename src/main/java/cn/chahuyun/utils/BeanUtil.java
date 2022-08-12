package cn.chahuyun.utils;

import cn.chahuyun.HuYanSession;
import cn.hutool.db.Entity;
import net.mamoe.mirai.utils.MiraiLogger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * BeanUtil
 *
 * @author Zhangjiaxing
 * @description 实体对象的操作工具类
 * @date 2022/7/12 9:30
 */
public class BeanUtil {

    private final static MiraiLogger l = HuYanSession.INSTANCE.getLogger();

    /**
     * 通过传入的泛型，来实现从entity中获取值然后创建对应对象
     * @author Moyuyanli
     * @param entity entity对象
     * @param tClass 返回对象
     * @date 2022/7/12 10:37
     * @return T
     */
    @Deprecated(since="以弃用，留着当案例")
    public static <T> T  parseEntity(Entity entity, Class<T> tClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Field[] fields = tClass.getDeclaredFields();
        Class<?>[] classes = new Class<?>[fields.length+1];
        classes[0] = long.class;
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            classes[i+1] = field.getType();
        }
        Constructor<T> constructor = tClass.getConstructor(classes);
        Object[] params = new Object[fields.length + 1];
        params[0] = entity.get("bot");
        for (int i = 0; i < fields.length; i++) {
            String fieldName = fields[i].getName();
            Object obj = null;
            try {
                if (int.class.equals(fields[i].getType())) {
                    obj = entity.getInt(fieldName);
                } else if (long.class.equals(fields[i].getType())) {
                    obj = entity.getLong(fieldName);
                } else if (String.class.equals(fields[i].getType())) {
                    obj = entity.getStr(fieldName);
                } else if (boolean.class.equals(fields[i].getType())) {
                    obj = entity.getBool(fieldName);
                } else {
                    obj = entity.getObj(fieldName);
                }
            } catch (Exception ignored) {
            }
            params[i+1] = obj;
        }
        return constructor.newInstance(params);
    }


}