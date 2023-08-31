package cn.chahuyun.session.entity;

import cn.chahuyun.session.utils.HibernateUtil;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import static cn.chahuyun.session.HuYanSession.LOGGER;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :作用域实体
 * @Date 2022/7/8 21:24
 */
@Entity
@Table(name = "Scope")
public class Scope implements BaseEntity {
    /**
     * id
     */
    @Id
    private String id;
    /**
     * 所属机器人
     */
    private long bot;
    /**
     * 外键标识
     */
    private String mark;
    /**
     * 作用域名称
     */
    private String scopeName;
    /**
     * 是否全局
     */
    private boolean isGlobal;
    /**
     * 是否群组
     */
    private boolean isGroupInfo;
    /**
     * 群号-`当前`使用
     */
    private long groupNumber;
    /**
     * 群组编号-`群组`使用
     */
    private String listId;

    public Scope() {
    }

    public Scope(long bot, String scopeName, boolean isGlobal, boolean isGroupInfo, long groupNumber, String listId) {
        this.id = bot + "." + isGlobal + "." + isGroupInfo + "." + groupNumber + "." + listId;
        this.bot = bot;
        this.scopeName = scopeName;
        this.isGlobal = isGlobal;
        this.isGroupInfo = isGroupInfo;
        this.groupNumber = groupNumber;
        this.listId = listId;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getBot() {
        return bot;
    }

    public void setBot(long bot) {
        this.bot = bot;
        setId();
    }

    public String getScopeName() {
        return scopeName;
    }

    public void setScopeName(String scopeName) {
        this.scopeName = scopeName;
        setId();
    }

    public boolean getGlobal() {
        return isGlobal;
    }

    public boolean getGroupInfo() {
        return isGroupInfo;
    }

    public long getGroupNumber() {
        return groupNumber;
    }

    public void setGroupNumber(long groupNumber) {
        setId();
        this.groupNumber = groupNumber;
    }

    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
        this.listId = listId;
        setId();
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        setId();
        this.mark = mark;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public void setGlobal(boolean global) {
        isGlobal = global;
        setId();
    }

    public boolean isGroupInfo() {
        return isGroupInfo;
    }

    public void setGroupInfo(boolean isGroupInfo) {
        this.isGroupInfo = isGroupInfo;
        setId();
    }

    @Override
    public String toString() {
        return "Scope{" +
                "id=" + id +
                ", bot=" + bot +
                ", scopeName='" + scopeName + '\'' +
                ", isGlobal=" + isGlobal +
                ", isGroupInfo=" + isGroupInfo +
                ", groupNumber=" + groupNumber +
                ", listId=" + listId +
                '}';
    }

    @Override
    public boolean equals(Object scope) {
        if (scope instanceof Scope) {
            return this.mark.equals(((Scope) scope).getMark());
        }
        return false;
    }

    private void setId() {
        this.id = bot + "." + isGlobal + "." + isGroupInfo + "." + groupNumber + "." + listId;
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
            HibernateUtil.factory.fromTransaction(session -> {
                HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
                JpaCriteriaQuery<Scope> query = builder.createQuery(Scope.class);
                JpaRoot<Scope> from = query.from(Scope.class);
                query.select(from);
                query.where(builder.equal(from.get("bot"), this.bot));
                query.where(builder.equal(from.get("listId"), this.listId));
                Scope singleResult = session.createQuery(query).getSingleResultOrNull();
                if (singleResult != null) {
                    this.setId(singleResult.getId());
                }
                session.merge(this);
                return null;
            });
        } catch (Exception e) {
            LOGGER.error("作用域信息保存失败！", e);
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
            LOGGER.error("作用域信息删除失败！", e);
            return false;
        }
        return true;
    }
}
