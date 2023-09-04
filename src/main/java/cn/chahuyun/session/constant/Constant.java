package cn.chahuyun.session.constant;

import cn.chahuyun.session.HuYanSession;

/**
 * 常量
 *
 * @author Moyuyanli
 * @date 2023/8/31 14:33
 */
public class Constant {

    /**
     * 秃瓢转换后的字符
     */
    public static final String IMAGE_TYPE = "[图片]";

    /**
     * 数据存储地址前缀
     */
    public static final String IMG_PREFIX_ADDRESS = HuYanSession.INSTANCE.getDataFolderPath().toString();



    /**
     * hibernate 驱动类型配置名
     */
    public static final String HIBERNATE_CONNECTION_DRIVER_CLASS = "hibernate.connection.driver_class";
    public static final String HIBERNATE_CONNECTION_DRIVER_CLASS_H2 = "org.h2.Driver";
    public static final String HIBERNATE_CONNECTION_DRIVER_CLASS_MYSQL = "com.mysql.cj.jdbc.Driver";
    /**
     * hibernate 连接地址配置名
     */
    public static final String HIBERNATE_CONNECTION_URL = "hibernate.connection.url";

}
