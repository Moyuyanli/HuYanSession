package cn.chahuyun.entity;

import jakarta.persistence.*;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :作用域实体
 * @Date 2022/7/8 21:24
 */
@Entity
@Table(name = "Scope")
public class Scope {
    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    /**
     * 所属机器人
     */
    private long bot;
    /**
     * 外键标识
     */
    private String mark;
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
    private boolean isGroupInfo;
    /**
     * 群号-`当前`使用
     */
    private long groupNumber;
    /**
     * 群组编号-`群组`使用
     */
    private int listId;

    public Scope() {
    }

    public Scope(long bot, String scopeName, boolean isGlobal, boolean isGroupInfo, long groupNumber, int listId) {
        this.bot = bot;
        this.scopeName = scopeName;
        this.isGlobal = isGlobal;
        this.isGroupInfo = isGroupInfo;
        this.groupNumber = groupNumber;
        this.listId = listId;
        this.mark = bot + "." + isGlobal + "." + isGroupInfo + "." + groupNumber + "." + listId;
    }

    public Scope(int id, long bot, String scopeName, boolean isGlobal, boolean isGroupInfo, long groupNumber, int listId) {
        this.id = id;
        this.bot = bot;
        this.scopeName = scopeName;
        this.isGlobal = isGlobal;
        this.isGroupInfo = isGroupInfo;
        this.groupNumber = groupNumber;
        this.listId = listId;
    }

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

    public boolean getGlobal() {
        return isGlobal;
    }

    public void setGlobal(boolean global) {
        isGlobal = global;
    }

    public boolean getGroupInfo() {
        return isGroupInfo;
    }

    public void setGroupInfo(boolean isGroupInfo) {
        this.isGroupInfo = isGroupInfo;
    }

    public long getGroupNumber() {
        return groupNumber;
    }

    public void setGroupNumber(long groupNumber) {
        this.groupNumber = groupNumber;
    }

    public int getListId() {
        return listId;
    }

    public void setListId(int listId) {
        this.listId = listId;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public boolean isGroupInfo() {
        return isGroupInfo;
    }

    @Override
    public String toString() {
        return "Scope{" +
                "id=" + id +
                ", bot=" + bot +
                ", scopeName='" + scopeName + '\'' +
                ", isGlobal=" + isGlobal +
                ", isGroupInfo=" + isGroupInfo +
                ", groupNumber=" + groupNumber +
                ", listId=" + listId +
                '}';
    }



}
