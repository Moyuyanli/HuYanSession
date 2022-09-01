package cn.chahuyun.entity;

import cn.chahuyun.utils.ScopeUtil;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :群欢迎词
 * @Date 2022/8/17 19:48
 */
@Entity
@Table(name = "GroupWelcomeInfo")
public class GroupWelcomeInfo {

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
     * 群欢迎消息随机标识 - 用于匹配欢迎消息
     */
    private int randomMark;
    /**
     * 作用域标识
     */
    private String scopeMark;
    /**
     * 欢迎消息集合
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, targetEntity = WelcomeMessage.class)
    @JoinColumn(name = "WelcomeMessage_id")
    private List<WelcomeMessage> welcomeMessages = new ArrayList<>();
    /**
     * 作用域
     */
    @Transient
    private Scope scope;

    public GroupWelcomeInfo() {
    }

    public GroupWelcomeInfo(long bot, boolean random, int pollingNumber, int randomMark, Scope scopeInfo) {
        this.bot = bot;
        this.random = random;
        this.pollingNumber = pollingNumber;
        this.randomMark = randomMark;
        this.scopeMark = bot + "." + scopeInfo.isGlobal() + "." + scopeInfo.isGroupInfo() + "." + scopeInfo.getGroupNumber() + "." + scopeInfo.getListId();
        this.scope = scopeInfo;
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

    public String getScopeMark() {
        return scopeMark;
    }

    public void setScopeMark(String scopeMark) {
        this.scopeMark = scopeMark;
    }

    public List<WelcomeMessage> getWelcomeMessages() {
        return welcomeMessages;
    }

    public void setWelcomeMessages(List<WelcomeMessage> welcomeMessages) {
        this.welcomeMessages = welcomeMessages;
    }

    public int getRandomMark() {
        return randomMark;
    }

    public void setRandomMark(int randomMark) {
        this.randomMark = randomMark;
    }

    public Scope getScope() {
        return ScopeUtil.getScope(this.scopeMark);
    }

    public void setScope(Scope scopeInfo) {
        this.scopeMark = bot + "." + scopeInfo.isGlobal() + "." + scopeInfo.isGroupInfo() + "." + scopeInfo.getGroupNumber() + "." + scopeInfo.getListId();
        this.scope = scopeInfo;
    }
}
