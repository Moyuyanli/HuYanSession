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
    @GeneratedValue(strategy = GenerationType.AUTO)
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
                JpaCriteriaQuery<GroupInfo> query = builder.createQuery(GroupInfo.class);
                JpaRoot<GroupInfo> from = query.from(GroupInfo.class);
                query.select(from);
                query.where(builder.equal(from.get("bot"), this.bot));
                query.where(builder.equal(from.get("listId"), this.listId));
                GroupInfo singleResult = session.createQuery(query).getSingleResultOrNull();
                if (singleResult != null) {
                    this.setId(singleResult.getId());
                }
                GroupList merge = session.merge(this);
                merge.getGroups().forEach(it->{
                    it.setListId(merge.id);
                    it.merge();
                });
                return null;
            });
        } catch (Exception e) {
            LOGGER.error("群组信息保存失败！",e);
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
                this.getGroups().forEach(GroupInfo::remove);
                session.remove(this);
                return null;
            });
        } catch (Exception e) {
            LOGGER.error("群组信息删除失败！",e);
            return false;
        }
        return true;
    }
}
