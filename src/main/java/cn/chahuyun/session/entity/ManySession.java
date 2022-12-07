package cn.chahuyun.session.entity;

import jakarta.persistence.*;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :多词条消息的消息信息
 * @Date 2022/8/17 19:20
 */
@Entity
@Table(name = "ManySession")
public class ManySession {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    /**
     * 所属机器人
     */
    private long bot;
    /**
     * 是否包含动态消息参数
     */
    private boolean dynamic;
    /**
     * 是否是转发或语音消息
     */
    private boolean other;
    /**
     * 匹配主表消息id
     */
    private Integer ManySession_ID;

    /**
     * 匹配定时任务的多消息
     */
    private Integer QuartzMessage_ID;

    /**
     * 回复消息
     */
    @Column(length = 10240)
    private String reply;

    public ManySession() {
    }


    public ManySession(long bot, boolean dynamic, boolean other, String reply) {
        this.bot = bot;
        this.dynamic = dynamic;
        this.other = other;
        this.reply = reply;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
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

    public Integer getManySession_ID() {
        return ManySession_ID;
    }

    public void setManySession_ID(Integer manySession_ID) {
        ManySession_ID = manySession_ID;
    }

    public Integer getQuartzMessage_ID() {
        return QuartzMessage_ID;
    }

    public void setQuartzMessage_ID(Integer quartzMessage_ID) {
        QuartzMessage_ID = quartzMessage_ID;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public boolean isOther() {
        return other;
    }

    public void setOther(boolean other) {
        this.other = other;
    }
}
