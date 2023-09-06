package cn.chahuyun.session.entity;

import cn.chahuyun.session.enums.Mate;
import cn.chahuyun.session.utils.HibernateUtil;
import cn.chahuyun.session.utils.MateUtil;
import cn.chahuyun.session.utils.ScopeUtil;
import cn.chahuyun.session.utils.ShareUtils;
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
 * @Description :多词条消息的综合信息
 * @Date 2022/8/17 19:20
 */
@Entity
@Table(name = "ManySessionInfo")
public class ManySessionInfo extends BaseMessage implements BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    /**
     * 所属机器人
     */
    private long bot;
    /**
     * 是否随机
     */
    private boolean random;
    /**
     * 轮询次数
     */
    private int pollingNumber;
    /**
     * 触发消息
     */
    private String keywords;
    /**
     * 匹配类型 int
     */
    private int mateType;
    /**
     * 多词条消息集合
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER,mappedBy = "manySessionId",targetEntity = ManySession.class)
    private List<ManySession> manySessions = new ArrayList<>();
    /**
     * 作用域标识
     */
    private String scopeMark;
    /**
     * 匹配类型
     */
    @Transient
    private Mate mate;
    /**
     * 作用域
     */
    @Transient
    private Scope scope;

    public ManySessionInfo() {
    }

    public ManySessionInfo(long bot, boolean random, int pollingNumber, String keywords, int mateType, Scope scope) {
        this.bot = bot;
        this.random = random;
        this.pollingNumber = pollingNumber;
        this.keywords = keywords;
        this.mateType = mateType;
        this.mate = MateUtil.getMate(mateType);
        if (scope.isGlobal()) {
            this.scopeMark = bot + ".";
        } else if (scope.isGroupInfo()) {
            this.scopeMark = bot + ".gr" + scope.getListId();
        } else {
            this.scopeMark = bot + "." + scope.getGroupNumber();
        }
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

    public boolean isRandom() {
        return random;
    }

    public void setRandom(boolean random) {
        this.random = random;
    }

    public int getPollingNumber() {
        return pollingNumber;
    }

    public void setPollingNumber(int pollingNumber) {
        this.pollingNumber = pollingNumber;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String trigger) {
        this.keywords = trigger;
    }

    public int getMateType() {
        return mateType;
    }

    public void setMateType(int mateType) {
        this.mateType = mateType;
    }

    public List<ManySession> getManySessions() {
        return manySessions;
    }

    public void setManySessions(List<ManySession> manySessions) {
        this.manySessions = manySessions;
    }

    public String getScopeMark() {
        return scopeMark;
    }

    public void setScopeMark(String scopeMark) {
        this.scopeMark = scopeMark;
    }

    public Mate getMate() {
        return ShareUtils.getMate(mateType);
    }

    public void setMate(Mate mate) {
        this.mate = mate;
        this.mateType = mate.getMateType();
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
                JpaCriteriaQuery<ManySessionInfo> query = builder.createQuery(ManySessionInfo.class);
                JpaRoot<ManySessionInfo> from = query.from(ManySessionInfo.class);
                query.select(from);
                query.where(builder.equal(from.get("bot"), this.bot));
                query.where(builder.equal(from.get("keywords"), this.keywords));
                ManySessionInfo singleResult = session.createQuery(query).getSingleResultOrNull();
                if (singleResult != null) {
                    this.setId(singleResult.getId());
                }
                ManySessionInfo merge = session.merge(this);
                merge.getManySessions().forEach(it->{
                    it.setManySessionId(merge.getId());
//                    it.merge();
                });
                return null;
            });
        } catch (Exception e) {
            LOGGER.error("多词条信息保存失败！");
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
            LOGGER.error("多词条信息删除失败！");
            return false;
        }
        return true;
    }
}
