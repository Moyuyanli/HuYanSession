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
     * 匹配方式
     */
    private Mate mate;

    /**
     * 触发词
     */
    private String key;

    /**
     * 回复内容
     */
    private String value;

    /**
     * 作用域
     */
    private Scope scope;


    public Session(int type, Mate mate, String key, String value, Scope scope) {
        this.type = type;
        this.mate = mate;
        this.key = key;
        this.value = value;
        this.scope = scope;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Mate getMate() {
        return mate;
    }

    public void setMate(Mate mate) {
        this.mate = mate;
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

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }
}
