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
@Table(name = "group_number")
public class GroupNumber {


    private int id;

    private long bot;

    private String mark;

    private int listId;

    private long groupNum;

    public GroupNumber() {
    }

    public GroupNumber(int list_id, long groupNum) {
        this.listId = list_id;
        this.groupNum = groupNum;
        this.mark = groupNum + "." + list_id;
    }
    @Id
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

    public long getGroupNum() {
        return groupNum;
    }

    public void setGroupNum(long groupNum) {
        this.groupNum = groupNum;
    }

    public long getBot() {
        return bot;
    }

    public void setBot(long bot) {
        this.bot = bot;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public int getListId() {
        return listId;
    }

    public void setListId(int listId) {
        this.listId = listId;
    }
}
