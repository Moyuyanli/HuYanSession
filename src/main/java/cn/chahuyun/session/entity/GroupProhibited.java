package cn.chahuyun.session.entity;

import cn.chahuyun.session.utils.HibernateUtil;
import cn.chahuyun.session.utils.ScopeUtil;
import jakarta.persistence.*;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import static cn.chahuyun.session.HuYanSession.LOGGER;

/**
 * GroupProhibited
 * 群违禁词
 *
 * @author Moyuyanli
 * @date 2022/8/16 10:33
 */
@Entity
@Table(name = "GroupProhibited")
public class GroupProhibited implements BaseEntity {

    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    /**
     * 所属机器人
     */
    private Long bot;
    /**
     * 匹配方式
     */
    private int mateType;
    /**
     * 触发词
     */
    private String keywords;
    /**
     * 回复消息
     */
    private String reply;
    /**
     * 禁言时间
     */
    private int prohibitTime;
    /**
     * 禁言时间字符
     */
    private String prohibitString;
    /**
     * 是否禁言
     */
    private boolean prohibit;
    /**
     * 是否撤回
     */
    private boolean withdraw;
    /**
     * 是否累加黑名单次数
     */
    private boolean accumulate;
    /**
     * 触发多少次提出
     */
    private int accumulateNumber;
    /**
     * 作用域匹配标识
     */
    private String scopeMark;
    /**
     * 作用域
     */
    @Transient
    private Scope scopeInfo;

    public GroupProhibited() {
    }

    public GroupProhibited(Long bot, String keywords, String reply, int prohibitTime, String prohibitString, boolean prohibit, boolean withdraw, boolean accumulate, int accumulateNumber) {
        this.bot = bot;
        this.keywords = keywords;
        this.mateType = 2;
        this.reply = reply;
        this.prohibitTime = prohibitTime;
        this.prohibitString = prohibitString;
        this.prohibit = prohibit;
        this.withdraw = withdraw;
        this.accumulate = accumulate;
        this.accumulateNumber = accumulateNumber;

    }

    public GroupProhibited(Long bot, String keywords, String reply, int prohibitTime, String prohibitString, boolean prohibit, boolean withdraw, boolean accumulate, int accumulateNumber, Scope scope) {
        this.bot = bot;
        this.mateType = 2;
        this.keywords = keywords;
        this.reply = reply;
        this.prohibitTime = prohibitTime;
        this.prohibitString = prohibitString;
        this.prohibit = prohibit;
        this.withdraw = withdraw;
        this.accumulate = accumulate;
        this.accumulateNumber = accumulateNumber;
        if (scope.isGlobal()) {
            this.scopeMark = bot + ".";
        } else if (scope.isGroupInfo()) {
            this.scopeMark = bot + ".gr" + scope.getListId();
        } else {
            this.scopeMark = bot + "." + scope.getGroupNumber();
        }
        this.scopeInfo = scope;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Long getBot() {
        return bot;
    }

    public void setBot(Long bot) {
        this.bot = bot;
    }

    public int getMateType() {
        return mateType;
    }

    public void setMateType(int mateType) {
        this.mateType = mateType;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String trigger) {
        this.keywords = trigger;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public int getProhibitTime() {
        return prohibitTime;
    }

    public void setProhibitTime(int prohibitTime) {
        this.prohibitTime = prohibitTime;
    }

    public String getProhibitString() {
        return prohibitString;
    }

    public void setProhibitString(String prohibitString) {
        this.prohibitString = prohibitString;
    }

    public boolean isProhibit() {
        return prohibit;
    }

    public void setProhibit(boolean prohibit) {
        this.prohibit = prohibit;
    }

    public boolean isWithdraw() {
        return withdraw;
    }

    public void setWithdraw(boolean withdraw) {
        this.withdraw = withdraw;
    }

    public boolean isAccumulate() {
        return accumulate;
    }

    public void setAccumulate(boolean accumulate) {
        this.accumulate = accumulate;
    }

    public int getAccumulateNumber() {
        return accumulateNumber;
    }

    public void setAccumulateNumber(int accumulateNumber) {
        this.accumulateNumber = accumulateNumber;
    }

    public String getScopeMark() {
        return scopeMark;
    }

    public void setScopeMark(String scopeMark) {
        this.scopeMark = scopeMark;
    }

    public Scope getScopeInfo() {
        return ScopeUtil.getScope(this.scopeMark);
    }

    public void setScopeInfo(Scope scope) {
        this.scopeInfo = scope;
        if (scope.isGlobal()) {
            this.scopeMark = bot + ".";
        } else if (scope.isGroupInfo()) {
            this.scopeMark = bot + ".gr" + scope.getListId();
        } else {
            this.scopeMark = bot + "." + scope.getGroupNumber();
        }
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
                JpaCriteriaQuery<GroupProhibited> query = builder.createQuery(GroupProhibited.class);
                JpaRoot<GroupProhibited> from = query.from(GroupProhibited.class);
                query.select(from);
                query.where(builder.equal(from.get("bot"), this.bot));
                query.where(builder.equal(from.get("keywords"), this.keywords));
                GroupProhibited singleResult = session.createQuery(query).getSingleResultOrNull();
                if (singleResult != null) {
                    this.setId(singleResult.getId());
                }
                session.merge(this);
                return null;
            });
        } catch (Exception e) {
            LOGGER.error("违禁词信息保存失败！");
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
            LOGGER.error("违禁词信息删除失败！");
            return false;
        }
        return true;
    }
}

