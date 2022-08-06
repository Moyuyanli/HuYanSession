package cn.chahuyun.entity;

import jakarta.persistence.*;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :群组对应关系
 * @Date 2022/7/30 23:53
 */
@Entity
@Table(name = "group")
public class Group {

    @Id
    private int id;

    private long bot;

    private int mark;

    private int listId;

    private long group;

    public Group() {
    }

    public Group(int list_id, long group) {
        this.listId = list_id;
        this.group = group;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getList_id() {
        return listId;
    }

    public void setList_id(int list_id) {
        this.listId = list_id;
    }

    public long getGroup() {
        return group;
    }

    public void setGroup(long group) {
        this.group = group;
    }

    public long getBot() {
        return bot;
    }

    public void setBot(long bot) {
        this.bot = bot;
    }

    public int getMark() {
        return mark;
    }

    public void setMark(int mark) {
        this.mark = mark;
    }

    public int getListId() {
        return listId;
    }

    public void setListId(int listId) {
        this.listId = listId;
    }
}
