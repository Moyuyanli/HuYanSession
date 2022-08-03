package cn.chahuyun.entity;

import jakarta.persistence.*;

import java.util.List;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :群组
 * @Date 2022/7/9 20:14
 */
@Entity
@Table(name = "group_list")
public class GroupList {

    /**
     * id
     */
    @Id
    private int id;

    /**
     * 所属机器人
     */
    private long bot;
    /**
     * 群组编号
     */
    private int listId;

    /**
     * 所有群号
     */
    @OneToMany(fetch = FetchType.EAGER )
    @JoinColumn
    private List<Group> groups;

    public GroupList() {

    }

    public GroupList(long bot, int listId) {
        this.bot = bot;
        this.listId = listId;
    }

    public GroupList(long bot, int listId, List<Group> groups) {
        this.bot = bot;
        this.listId = listId;
        this.groups = groups;
    }

    public long getBot() {
        return bot;
    }

    public void setBot(long bot) {
        this.bot = bot;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public int getListId() {
        return listId;
    }

    public void setListId(int listId) {
        this.listId = listId;
    }

    public List<Group> getGroups() {
        return groups;
    }
}
