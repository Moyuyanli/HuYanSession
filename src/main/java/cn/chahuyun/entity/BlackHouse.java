package cn.chahuyun.entity;

import jakarta.persistence.*;

/**
 * BlackHouse
 * 小黑屋
 *
 * @author Moyuyanli
 * @date 2022/8/19 10:43
 */
@Entity
@Table(name = "BlackHouse")
public class BlackHouse {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    /**
     * 所属机器人
     */
    private long bot;
    /**
     * 触发qq
     */
    private long qq;
    /**
     * 触发违禁词id
     */
    private int prohibitedId;
    /**
     * 触发次数
     */
    private int number;

    public BlackHouse() {
    }

    public BlackHouse(long bot, long qq, int prohibitedId, int number) {
        this.bot = bot;
        this.qq = qq;
        this.prohibitedId = prohibitedId;
        this.number = number;
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

    public long getQq() {
        return qq;
    }

    public void setQq(long qq) {
        this.qq = qq;
    }

    public int getProhibitedId() {
        return prohibitedId;
    }

    public void setProhibitedId(int prohibitedId) {
        this.prohibitedId = prohibitedId;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}