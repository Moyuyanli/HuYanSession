package cn.chahuyun.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :作用域实体
 * @Date 2022/7/8 21:24
 */
@Entity
@Table(name = "scope")
public class Scope {
    /**
     * id
     */
    private int id;
    /**
     * 所属机器人
     */
    private long bot;
    /**
     * 作用域名称
     */
    private String scopeName;
    /**
     * 是否全局
     */
    private int isGlobal;
    /**
     * 是否群组
     */
    private int isGroup;
    /**
     * 群号-`当前`使用
     */
    private long group;
    /**
     * 群组编号-`群组`使用
     */
    private int listId;
    public Scope() {
    }

    public Scope(long bot, String scopeName, boolean isGlobal, boolean isGroup, long group, int listId) {
        this.bot = bot;
        this.scopeName = scopeName;
        this.isGlobal = isGlobal?1:0;
        this.isGroup = isGroup?1:0;
        this.group = group;
        this.listId = listId;
    }

    public Scope(int id, long bot, String scopeName, boolean isGlobal, boolean isGroup, long group, int listId) {
        this.id = id;
        this.bot = bot;
        this.scopeName = scopeName;
        this.isGlobal = isGlobal?1:0;
        this.isGroup = isGroup?1:0;
        this.group = group;
        this.listId = listId;
    }
    @Id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getBot() {
        return bot;
    }

    public void setBot(long bot) {
        this.bot = bot;
    }

    public String getScopeName() {
        return scopeName;
    }

    public void setScopeName(String scopeName) {
        this.scopeName = scopeName;
    }

    public boolean isGlobal() {
        return isGlobal == 1;
    }

    public void setGlobal(boolean global) {
        isGlobal = global?1:0;
    }

    public boolean isGroup() {
        return isGroup==1;
    }

    public void setGroup(boolean group) {
        isGroup = group?1:0;
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
