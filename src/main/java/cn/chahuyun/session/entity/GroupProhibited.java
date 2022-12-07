package cn.chahuyun.session.entity;

import cn.chahuyun.session.utils.ScopeUtil;
import jakarta.persistence.*;

/**
 * GroupProhibited
 * 群违禁词
 *
 * @author Moyuyanli
 * @date 2022/8/16 10:33
 */
@Entity
@Table
public class GroupProhibited {

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
    private String trigger;
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

    public GroupProhibited(Long bot, String trigger, String reply, int prohibitTime, String prohibitString, boolean prohibit, boolean withdraw, boolean accumulate, int accumulateNumber) {
        this.bot = bot;
        this.trigger = trigger;
        this.mateType = 2;
        this.reply = reply;
        this.prohibitTime = prohibitTime;
        this.prohibitString = prohibitString;
        this.prohibit = prohibit;
        this.withdraw = withdraw;
        this.accumulate = accumulate;
        this.accumulateNumber = accumulateNumber;

    }

    public GroupProhibited(Long bot, String trigger, String reply, int prohibitTime, String prohibitString, boolean prohibit, boolean withdraw, boolean accumulate, int accumulateNumber, Scope scope) {
        this.bot = bot;
        this.mateType = 2;
        this.trigger = trigger;
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

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
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
}

