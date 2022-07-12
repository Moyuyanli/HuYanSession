package cn.chahuyun.utils;

import cn.hutool.db.Entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * BeanUtil
 *
 * @author Zhangjiaxing
 * @description 实体对象的操作工具类
 * @date 2022/7/12 9:30
 */
public class BeanUtil {

    /**
     * 通过传入的泛型，来实现从entity中获取值然后创建对应对象
     * @author Moyuyanli
     * @param entity entity对象
     * @param tClass 返回对象
     * @date 2022/7/12 10:37
     * @return T
     */
    public static <T> T  parseEntity(Entity entity, Class<T> tClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Field[] fields = tClass.getDeclaredFields();
        Class<?>[] classes = new Class<?>[fields.length+1];
        classes[0] = Long.class;
        for (int i = 1; i < fields.length; i++) {
            Field field = fields[i];
            classes[i] = field.getType();
        }
        Constructor<T> constructor = tClass.getConstructor(classes);
        Object[] params = new Object[fields.length + 1];
        params[0] = entity.get("bot");
        for (int i = 1; i < fields.length; i++) {
            String fieldName = fields[i].getName();
            Object obj = null;
            try {
                obj = entity.getObj(fieldName);
            } catch (Exception ignored) {
            }
            params[i] = obj;
        }
        return constructor.newInstance(params);
    }


}