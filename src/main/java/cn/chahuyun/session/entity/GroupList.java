package cn.chahuyun.session.entity;

import cn.chahuyun.session.utils.HibernateUtil;
import jakarta.persistence.*;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.util.ArrayList;
import java.util.List;

import static cn.chahuyun.session.HuYanSession.LOGGER;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :群组
 * @Date 2022/7/9 20:14
 */
@Entity
@Table(name = "GroupList")
public class GroupList implements BaseEntity{

    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    /**
     * 所属机器人
     */
    private long bot;
    /**
     * 群组编号
     */
    private String listId;
    /**
     * 所有群号
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, targetEntity = GroupInfo.class,mappedBy = "listId")
    private List<GroupInfo> groups = new ArrayList<>();

    public GroupList() {
    }

    public GroupList(long bot, String listId) {
        this.bot = bot;
        this.listId = listId;
    }


    public GroupList(long bot, String listId, List<GroupInfo> groups) {
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

    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
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


    /**
     * 获取对应的groupInfo
     *
     * @param groupId 群id
     * @return cn.chahuyun.session.entity.GroupInfo
     * @author Moyuyanli
     * @date 2023/9/2 21:41
     */
    public GroupInfo getGroupInfo(long groupId) {
        for (GroupInfo group : groups) {
            if (group.getGroupId() == groupId) {
                return group;
            }
        }
        return null;
    }

    /**
     * 修改 this 所保存的数据
     * 用于保存或更新
     *
     * @return boolean t 成功
     * @author Moyuyanli
     * @date 2023/8/4 10:33
     */
    @Override
    public boolean merge() {
        try {
            HibernateUtil.factory.fromTransaction(session ->{
                HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
                JpaCriteriaQuery<GroupList> query = builder.createQuery(GroupList.class);
                JpaRoot<GroupList> from = query.from(GroupList.class);
                query.select(from);
                query.where(builder.equal(from.get("bot"), getBot()));
                query.where(builder.equal(from.get("listId"), getListId()));
                GroupList singleResult = session.createQuery(query).getSingleResultOrNull();
                if (singleResult != null) {
                    this.setId(singleResult.getId());
                }
                GroupList merge = session.merge(this);
                merge.getGroups().forEach(it->{
                    /*
                    不明白为啥这里不需要再进行子表信息保存就可以修改
                    或许是通过对资源的监听来做到同步更新？
                    但不论为啥，这个情况都方便了我。
                     */
                    it.setListId(merge.id);
                });
                return null;
            });
        } catch (Exception e) {
            if (e.getMessage().equals("Converting `org.hibernate.exception.DataException` to JPA `PersistenceException` : could not execute statement")) {
                HibernateUtil.factory.fromTransaction(session ->
                        session.createNativeQuery("alter table GROUPLIST alter column LISTID varchar(12);", GroupList.class)
                                .executeUpdate());
                return this.merge();
            }
            LOGGER.error("群组信息保存失败！");
            return false;
        }
        return true;
    }

    /**
     * 删除
     *
     * @return boolean t 成功
     * @author Moyuyanli
     * @date 2023/8/4 10:34
     */
    @Override
    public boolean remove() {
        try {
            HibernateUtil.factory.fromTransaction(session -> {
                session.remove(this);
                return null;
            });
        } catch (Exception e) {
            LOGGER.error("群组信息删除失败！");
            return false;
        }
        return true;
    }
}
