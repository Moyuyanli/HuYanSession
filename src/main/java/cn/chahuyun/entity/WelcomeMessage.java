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

    private long bot;

    private String mark;

    private String welcomeMessage;

    public WelcomeMessage(int id, long bot,int randomMark, String welcomeMessage) {
        this.id = id;
        this.bot = bot;
        this.mark = bot + "." + randomMark;
        this.welcomeMessage = welcomeMessage;
    }
}
