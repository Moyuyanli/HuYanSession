package cn.chahuyun.session.entity;

import jakarta.persistence.*;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :群组对应关系
 * @Date 2022/7/30 23:53
 */
@Entity
@Table(name = "GroupInfo")
public class GroupInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private long bot;

    private int listId;

    private long groupId;


    public GroupInfo() {
    }

    public GroupInfo(long bot, int listId, long groupId) {
        this.bot = bot;
        this.listId = listId;
        this.groupId = groupId;
    }

    public GroupInfo(long bot, long groupId) {
        this.bot = bot;
        this.groupId = groupId;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
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
                "id=" + id +
                ", bot=" + bot +
                ", listId=" + listId +
                ", groupId=" + groupId +
                '}';
    }
}
