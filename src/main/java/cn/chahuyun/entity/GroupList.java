package cn.chahuyun.entity;

import java.util.List;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :群组
 * @Date 2022/7/9 20:14
 */
public class GroupList extends Base {

    private int listId;

    private List<Long> groups;


    public GroupList(int listId, List<Long> groups) {
        this.listId = listId;
        this.groups = groups;
    }

    public int getListId() {
        return listId;
    }

    public void setListId(int listId) {
        this.listId = listId;
    }

    public List<Long> getGroups() {
        return groups;
    }

    public void setGroups(List<Long> groups) {
        this.groups = groups;
    }
}
