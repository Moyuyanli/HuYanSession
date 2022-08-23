package cn.chahuyun.entity;

import jakarta.persistence.*;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :群欢迎消息
 * @Date 2022/8/17 19:52
 */
@Entity
@Table(name = "WelcomeMessage")
public class WelcomeMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    /**
     * 所属机器人
     */
    private long bot;
    /**
     * 0-普通消息
     * 1-带动态消息的消息
     * 2-语音和转发
     */
    private int type;
    /**
     * 标识
     */
    private String mark;
    /**
     * 欢迎消息
     */
    private String welcomeMessage;

    public WelcomeMessage() {
    }

    public WelcomeMessage(long bot, int type, int randomMark, String welcomeMessage) {
        this.bot = bot;
        this.type = type;
        this.mark = bot + "." + randomMark;
        this.welcomeMessage = welcomeMessage;
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

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }

    @Override
    public boolean equals(Object obj) {
        return this.welcomeMessage.equals(((WelcomeMessage) obj).getWelcomeMessage());
    }
}
