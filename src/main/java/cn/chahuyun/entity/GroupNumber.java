package cn.chahuyun.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :群组对应关系
 * @Date 2022/7/30 23:53
 */
@Entity
@Table(name = "GroupNumber")
public class GroupNumber {


    private String id;

    private long bot;

    private int listId;

    private long groupId;

    public GroupNumber() {
    }

    public GroupNumber(long bot, int listId, long groupId) {
        this.id = bot + "." + listId;
        this.bot = bot;
        this.listId = listId;
        this.groupId = groupId;
    }

    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public long getBot() {
        return bot;
    }

    public void setBot(long bot) {
        this.bot = bot;
    }

    public int getListId() {
        return listId;
    }

    public void setListId(int listId) {
        this.listId = listId;
    }

    @Override
    public String toString() {
        return "GroupNumber{" +
                "id='" + id + '\'' +
                ", bot=" + bot +
                ", listId=" + listId +
                ", groupId=" + groupId +
                '}';
    }
}
