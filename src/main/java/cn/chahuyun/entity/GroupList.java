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
@Table(name = "GroupList")
public class GroupList {

    /**
     * id
     */
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
     * 对应标识
     */
    private String mark;
    /**
     * 所有群号
     */

    private List<GroupNumber> groups;

    public GroupList() {
    }

    public GroupList(long bot, int listId) {
        this.bot = bot;
        this.listId = listId;
        this.mark = bot + "." + listId;
    }

    public GroupList(int id, long bot, int listId, String mark) {
        this.id = id;
        this.bot = bot;
        this.listId = listId;
        this.mark = mark;
    }

    public GroupList(long bot, int listId, List<GroupNumber> groups) {
        this.bot = bot;
        this.listId = listId;
        this.mark = bot + "." + listId;
        this.groups = groups;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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

    @Transient
    public List<GroupNumber> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupNumber> groups) {
        this.groups = groups;
    }

    @Override
    public String toString() {
        return "GroupList{" +
                "id=" + id +
                ", bot=" + bot +
                ", listId=" + listId +
                ", mark='" + mark + '\'' +
                ", groups=" + groups +
                '}';
    }
}
