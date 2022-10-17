package cn.chahuyun.entity;

import cn.chahuyun.utils.ScopeUtil;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 定时器信息
 *
 * @author Moyuyanli
 * @Date 2022/8/27 18:41
 */
@Entity
@Table(name = "QuartzInfo")
public class QuartzInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    /**
     * 所属bot
     */
    private long bot;
    /**
     * 定时器名称
     */
    private String name;
    /**
     * 定时器cron表达式
     */
    private String cronString;
    /**
     * 是否含有动态消息
     */
    private boolean dynamic;
    /**
     * 是否是转发或其他消息
     */
    private boolean other;
    /**
     * 回复消息
     */
    @Column(length = 10240)
    private String reply;
    /**
     * 定时器状态 true 开启
     */
    private boolean status;
    /**
     * 是否轮询
     */
    private boolean polling;
    /**
     * 是否随机
     */
    private boolean random;
    /**
     * 轮询次数
     */
    private int pollingNumber;
    /**
     * 作用域标识
     */
    private String scopeMark;
    /**
     * 作用域
     */
    @Transient
    private Scope scope;
    /**
     * 多词条消息集合
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, targetEntity = ManySession.class)
    @JoinColumn(name = "QuartzMessage_ID")
    private List<ManySession> manySessions = new ArrayList<>();

    public QuartzInfo() {
    }

    public QuartzInfo(long bot, String name, String cronString, boolean dynamic, boolean other, String reply, boolean polling, boolean random, Scope scope) {
        this.bot = bot;
        this.name = name;
        this.cronString = cronString;
        this.dynamic = dynamic;
        this.other = other;
        this.reply = reply;
        this.polling = polling;
        this.random = random;
        this.pollingNumber = 0;
        this.status = false;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCronString() {
        return cronString;
    }

    public boolean isPolling() {
        return polling;
    }

    public void setPolling(boolean polling) {
        this.polling = polling;
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

    public void setCronString(String cronString) {
        this.cronString = cronString;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    public boolean isOther() {
        return other;
    }

    public void setOther(boolean other) {
        this.other = other;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getScopeMark() {
        return scopeMark;
    }

    public void setScopeMark(String mark) {
        this.scopeMark = mark;
    }

    public Scope getScope() {
        return ScopeUtil.getScope(scopeMark);
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

    public List<ManySession> getManySessions() {
        return manySessions;
    }

    public void setManySessions(List<ManySession> manySessions) {
        this.manySessions = manySessions;
    }
}
