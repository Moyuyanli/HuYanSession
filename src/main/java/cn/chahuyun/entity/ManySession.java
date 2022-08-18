package cn.chahuyun.entity;

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
     * 多词条消息识别
     */
    private String mark;
    /**
     * 回复消息
     */
    private String reply;

    public ManySession() {
    }

    public ManySession(long bot,String trigger, String reply) {
        this.bot = bot;
        this.mark = bot+"."+trigger+"."+reply;
        this.reply = reply;
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

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }
}
