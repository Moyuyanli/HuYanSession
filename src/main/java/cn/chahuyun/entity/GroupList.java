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

    private String mark;

    /**
     * 所有群号
     */
    @OneToMany(fetch = FetchType.EAGER ,targetEntity = GroupNumber.class,mappedBy = "mark")
    private List<GroupNumber> groups;

    public GroupList() {

    }

    public GroupList(long bot, int listId) {
        this.bot = bot;
        this.listId = listId;
    }

    public GroupList(long bot, int listId, List<GroupNumber> groupNumbers) {
        this.bot = bot;
        this.listId = listId;
        this.groups = groupNumbers;
    }

    public long getBot() {
        return bot;
    }

    public void setBot(long bot) {
        this.bot = bot;
    }

    public void setGroups(List<GroupNumber> groupNumbers) {
        this.groups = groupNumbers;
    }

    public int getListId() {
        return listId;
    }

    public void setListId(int listId) {
        this.listId = listId;
    }

    public List<GroupNumber> getGroups() {
        return groups;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }
}
