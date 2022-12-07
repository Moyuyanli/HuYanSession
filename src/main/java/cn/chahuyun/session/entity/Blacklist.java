package cn.chahuyun.session.entity;

import cn.chahuyun.session.utils.ScopeUtil;
import jakarta.persistence.*;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :黑名单实体
 * @Date 2022/8/17 19:42
 */
@Entity
@Table(name = "Blacklist")
public class Blacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    /**
     * 所属机器人
     */
    private long bot;
    /**
     * 黑名单qq
     */
    private long blackQQ;
    /**
     * 封禁理由
     */
    private String reason;
    /**
     * 是否踢出
     */
    private boolean kick;
    /**
     * 是否禁言
     */
    private boolean prohibit;
    /**
     * 是否撤回消息
     */
    private boolean withdraw;
    /**
     * 作用域标识
     */
    private String scopeMark;

    @Transient
    private Scope scope;

    public Blacklist() {
    }

    public Blacklist(long bot, long blackQQ, String reason, Scope scopeInfo) {
        this.bot = bot;
        this.blackQQ = blackQQ;
        this.reason = reason;
        if (scope.isGlobal()) {
            this.scopeMark = bot + ".";
        } else if (scope.isGroupInfo()) {
            this.scopeMark = bot + ".gr" + scope.getListId();
        } else {
            this.scopeMark = bot + "." + scope.getGroupNumber();
        }
        this.scope = scopeInfo;
    }

    public Blacklist(long bot, long blackQQ, String reason, boolean kick, boolean prohibit, boolean withdraw, Scope scope) {
        this.bot = bot;
        this.blackQQ = blackQQ;
        this.reason = reason;
        this.kick = kick;
        this.prohibit = prohibit;
        this.withdraw = withdraw;
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

    public long getBlackQQ() {
        return blackQQ;
    }

    public void setBlackQQ(long blackQQ) {
        this.blackQQ = blackQQ;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getScopeMark() {
        return scopeMark;
    }

    public void setScopeMark(String scopeMark) {
        this.scopeMark = scopeMark;
    }

    public boolean isKick() {
        return kick;
    }

    public void setKick(boolean kick) {
        this.kick = kick;
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

    public Scope getScope() {
        return ScopeUtil.getScope(this.scopeMark);
    }

    public void setScope(Scope scopeInfo) {
        if (scope.isGlobal()) {
            this.scopeMark = bot + ".";
        } else if (scope.isGroupInfo()) {
            this.scopeMark = bot + ".gr" + scope.getListId();
        } else {
            this.scopeMark = bot + "." + scope.getGroupNumber();
        }
        this.scope = scopeInfo;
    }
}
