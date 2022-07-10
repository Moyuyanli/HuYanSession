package cn.chahuyun.entity;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :作用域实体
 * @Date 2022/7/8 21:24
 */
public class Scope extends Base{

    /**
     * 作用域名称
     */
    private String scopeName;

    /**
     * 是否全局
     */
    private boolean isGlobal;

    /**
     * 是否群组
     */
    private boolean isGroup;

    /**
     * 群号-`当前`使用
     */
    private long group;

    /**
     * 群组编号-`群组`使用
     */
    private int listId;


    public Scope(long bot, String scopeName, boolean isGlobal, boolean isGroup, long group, int listId) {
        super(bot);
        this.scopeName = scopeName;
        this.isGlobal = isGlobal;
        this.isGroup = isGroup;
        this.group = group;
        this.listId = listId;
    }

    public String getScopeName() {
        return scopeName;
    }

    public void setScopeName(String scopeName) {
        this.scopeName = scopeName;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public void setGlobal(boolean global) {
        isGlobal = global;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }

    public long getGroup() {
        return group;
    }

    public void setGroup(long group) {
        this.group = group;
    }

    public int getListId() {
        return listId;
    }

    public void setListId(int listId) {
        this.listId = listId;
    }
}
