package cn.chahuyun.entity;

import cn.chahuyun.enums.Mate;
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
    private Mate mate;
    /**
     * 作用域
     */

    private Scope scope;

    public Session() {
    }

    public Session(long bot, int type, String key, String value, Mate mate, Scope scope) {
        this.bot = bot;
        this.type = type;
        this.key = key;
        this.value = value;
        this.mate = mate;
        this.scope = scope;
    }

    public Session(int id, long bot, int type, String key, String value, Mate mate, Scope scope) {
        this.id = id;
        this.bot = bot;
        this.type = type;
        this.key = key;
        this.value = value;
        this.mate = mate;
        this.scope = scope;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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

    @Transient
    public Mate getMate() {
        return mate;
    }

    public void setMate(Mate mate) {
        this.mate = mate;
    }

    @Transient
    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
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
                ", scope=" + scope +
                '}';
    }
}