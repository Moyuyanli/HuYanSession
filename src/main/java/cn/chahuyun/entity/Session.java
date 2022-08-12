package cn.chahuyun.entity;

import cn.chahuyun.enums.Mate;
import jakarta.persistence.*;
import org.hibernate.annotations.Cascade;

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
     * 触发词
     */
    private String key;
    /**
     * 回复内容
     */
    private String value;
    /**
     * 匹配方式
     */
    private int mateInter;
    @Transient
    private Mate mate;
    /**
     * 作用域
     */
    @Transient
//    @ManyToOne(cascade = CascadeType.ALL,fetch = FetchType.EAGER,targetEntity = Scope.class)
//    @JoinColumn(name = "scope_mark")
    private Scope scopeInfo;

    public Session() {
    }

    public Session(long bot, int type, String key, String value, Mate mate, Scope scopeInfo) {
        this.bot = bot;
        this.type = type;
        this.key = key;
        this.value = value;
        this.mateInter = mate.getMateType();
        this.mate = mate;
        this.scopeInfo = scopeInfo;
    }

    public Session(int id, long bot, int type, String key, String value, Mate mate, Scope scopeInfo) {
        this.id = id;
        this.bot = bot;
        this.type = type;
        this.key = key;
        this.mateInter = mate.getMateType();
        this.value = value;
        this.mate = mate;
        this.scopeInfo = scopeInfo;
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Mate getMate() {
        switch (mateInter) {
            case 1:
                return Mate.ACCURATE;
            case 2:
                return Mate.VAGUE;
            case 3:
                return Mate.START;
            case 4:
                return Mate.END;
            default:
                return Mate.ACCURATE;
        }
    }

    public void setMate(Mate mate) {
        this.mate = mate;
        this.mateInter = mate.getMateType();
    }

    public Scope getScopeInfo() {
        return scopeInfo;
    }

    public void setScopeInfo(Scope scopeInfo) {
        this.scopeInfo = scopeInfo;
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
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", mate=" + mate +
                ", scope=" + scopeInfo +
                '}';
    }
}