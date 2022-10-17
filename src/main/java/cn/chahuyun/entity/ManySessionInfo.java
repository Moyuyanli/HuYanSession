package cn.chahuyun.entity;

import cn.chahuyun.enums.Mate;
import cn.chahuyun.utils.MateUtil;
import cn.chahuyun.utils.ScopeUtil;
import cn.chahuyun.utils.ShareUtils;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :多词条消息的综合信息
 * @Date 2022/8/17 19:20
 */
@Entity
@Table(name = "ManySessionInfo")
public class ManySessionInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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
    private String trigger;
    /**
     * 匹配类型 int
     */
    private int mateType;
    /**
     * 多词条消息集合
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, targetEntity = ManySession.class)
    @JoinColumn(name = "ManySession_ID")
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

    public ManySessionInfo(long bot, boolean random, int pollingNumber, String trigger, int mateType, Scope scope) {
        this.bot = bot;
        this.random = random;
        this.pollingNumber = pollingNumber;
        this.trigger = trigger;
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

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
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
}
