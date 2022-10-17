package cn.chahuyun.entity;

import cn.chahuyun.enums.Mate;
import cn.chahuyun.utils.ScopeUtil;
import cn.chahuyun.utils.ShareUtils;
import jakarta.persistence.*;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :会话消息实体类
 * @Date 2022/7/8 21:20
 */
@Entity
@Table(name = "Session")
public class Session {

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
}