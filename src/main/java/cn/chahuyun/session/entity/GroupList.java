package cn.chahuyun.session.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
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
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, targetEntity = GroupInfo.class)
    @JoinColumn(name = "GROUPLIST_ID")
    private List<GroupInfo> groups = new ArrayList<>();

    public GroupList() {
    }

    public GroupList(long bot, int listId) {
        this.bot = bot;
        this.listId = listId;
    }


    public GroupList(long bot, int listId, List<GroupInfo> groups) {
        this.bot = bot;
        this.listId = listId;
        this.groups = groups;
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

    public int getListId() {
        return listId;
    }

    public void setListId(int listId) {
        this.listId = listId;
    }

    public List<GroupInfo> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupInfo> groups) {
        this.groups = groups;
    }

    @Override
    public String toString() {
        return "GroupList{" +
                "id=" + id +
                ", bot=" + bot +
                ", listId=" + listId +
                ", groups=" + groups +
                '}';
    }

    /**
     * 判断这个群在群组信息中是否存在
     *
     * @param groupId 群号
     * @return boolean  true 存在
     * @author Moyuyanli
     * @date 2022/8/11 15:35
     */
    public boolean containsGroupId(long groupId) {
        for (GroupInfo group : groups) {
            if (group.getGroupId() == groupId) {
                return true;
            }
        }
        return false;
    }

}
