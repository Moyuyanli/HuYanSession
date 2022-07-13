package cn.chahuyun.entity;

import cn.chahuyun.enums.Mate;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :会话消息实体类
 * @Date 2022/7/8 21:20
 */
public class Session extends Base {

    /**
     * 内容类型
     */
    private int type;


    /**
     * 触发词
     */
    private String key;

    /**
     * 回复内容
     */
    private String value;

    /**
     * 匹配方式
     */
    private Mate mate;

    /**
     * 作用域
     */
    private Scope scope;


    public Session(long bot, int type, String key, String value, Mate mate, Scope scope) {
        super(bot);
        this.type = type;
        this.key = key;
        this.value = value;
        this.mate = mate;
        this.scope = scope;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Mate getMate() {
        return mate;
    }

    public void setMate(Mate mate) {
        this.mate = mate;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }
}
