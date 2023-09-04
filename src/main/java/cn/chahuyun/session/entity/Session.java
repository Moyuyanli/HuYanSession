package cn.chahuyun.session.entity;

import cn.chahuyun.session.enums.Mate;
import cn.chahuyun.session.utils.HibernateUtil;
import cn.chahuyun.session.utils.ScopeUtil;
import cn.chahuyun.session.utils.ShareUtils;
import jakarta.persistence.*;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import static cn.chahuyun.session.HuYanSession.LOGGER;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :会话消息实体类
 * @Date 2022/7/8 21:20
 */
@Entity
@Table(name = "Session")
public class Session extends BaseMessage implements BaseEntity {

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
     * 内容类型
     */
    private int type;
    /**
     * 是否包含动态消息参数
     */
    private boolean dynamic;
    /**
     * 触发词
     */
    private String term;
    /**
     * 回复内容
     */
    @Column(length = 10240)
    private String reply;
    /**
     * 匹配方式
     */
    private int mateInter;
    /**
     * 作用域匹配id
     */
    private String scopeMark;
    @Transient
    private Mate mate;
    @Transient
    private Scope scope;

    public Session() {
    }

    public Session(long bot, int type, String term, String reply, Mate mate, Scope scope, boolean dynamic) {
        this.bot = bot;
        this.type = type;
        this.term = term;
        this.reply = reply;
        this.mateInter = mate.getMateType();
        this.mate = mate;
        this.scope = scope;
        this.dynamic = dynamic;
        if (scope.isGlobal()) {
            this.scopeMark = bot + ".";
        } else if (scope.isGroupInfo()) {
            this.scopeMark = bot + ".gr" + scope.getListId();
        } else {
            this.scopeMark = bot + "." + scope.getGroupNumber();
        }
    }

    public Session(int id, long bot, int type, String term, String reply, Mate mate, Scope scope) {
        this.id = id;
        this.bot = bot;
        this.type = type;
        this.term = term;
        this.mateInter = mate.getMateType();
        this.reply = reply;
        this.mate = mate;
        this.scope = scope;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getScopeMark() {
        return scopeMark;
    }

    public void setScopeMark(String scopeMark) {
        this.scopeMark = scopeMark;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    public Mate getMate() {
        return ShareUtils.getMate(mateInter);
    }

    public void setMate(Mate mate) {
        this.mate = mate;
        this.mateInter = mate.getMateType();
    }

    public Scope getScope() {
        return ScopeUtil.getScope(this.scopeMark);
    }

    public void setScope(Scope scope) {
        if (scope.isGlobal()) {
            this.scopeMark = bot + ".";
        } else if (scope.isGroupInfo()) {
            this.scopeMark = bot + ".gr" + scope.getListId();
        } else {
            this.scopeMark = bot + "." + scope.getGroupNumber();
        }
        this.scope = scope;
    }

    public int getMateInter() {
        return mateInter;
    }

    public void setMateInter(int mateInter) {
        this.mateInter = mateInter;
    }

    @Override
    public String toString() {
        return "Session{" +
                "id=" + id +
                ", bot=" + bot +
                ", type=" + type +
                ", key='" + term + '\'' +
                ", reply='" + reply + '\'' +
                ", mate=" + mate +
                ", scope=" + scope +
                '}';
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
            //todo 标记 用于展示Hibernate查询
            HibernateUtil.factory.fromTransaction(session -> {
                HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
                JpaCriteriaQuery<Session> query = builder.createQuery(Session.class);
                JpaRoot<Session> from = query.from(Session.class);
                query.select(from);
                query.where(builder.equal(from.get("term"), this.term));
                Session singleResult = session.createQuery(query).getSingleResultOrNull();
                if (singleResult != null) {
                    setId(singleResult.getId());
                }
                session.merge(this);
                return null;
            });
        } catch (Exception e) {
            LOGGER.error("会话信息保存失败！");
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
            LOGGER.error("会话信息删除失败！");
            return false;
        }
        return true;
    }
}