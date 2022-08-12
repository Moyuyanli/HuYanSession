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
@Table(name = "GroupInfo")
public class GroupInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String mark;

    private long bot;

    private int listId;

    private long groupId;


//    @ManyToOne(fetch = FetchType.LAZY,targetEntity = GroupList.class)
//    @JoinColumn(name = "groupList_mark")
    @Transient
    private GroupList groupList;

    public GroupInfo() {
    }

    public GroupInfo(long bot, int listId, long groupId) {
        this.mark = bot + "." + listId;
        this.bot = bot;
        this.listId = listId;
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

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    @Override
    public String toString() {
        return "GroupNumber{" +
                "id=" + id +
//                ", mark='" + mark + '\'' +
                ", bot=" + bot +
                ", listId=" + listId +
                ", groupId=" + groupId +
                '}';
    }
}
