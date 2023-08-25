package cn.chahuyun.session.entity;

import cn.chahuyun.session.utils.HibernateUtil;
import jakarta.persistence.*;

import static cn.chahuyun.session.HuYanSession.LOGGER;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :群欢迎消息
 * @Date 2022/8/17 19:52
 */
@Entity
@Table(name = "WelcomeMessage")
public class WelcomeMessage implements BaseEntity {

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
    private Integer mark;
    /**
     * 欢迎消息
     */
    private String welcomeMessage;
    /**
     * 匹配主表id
     */
    private int groupWelcomeInfoId;

    public WelcomeMessage() {
    }

    public WelcomeMessage(long bot, int type, String welcomeMessage) {
        this.bot = bot;
        this.type = type;
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

    public Integer getMark() {
        return mark;
    }

    public void setMark(Integer mark) {
        this.mark = mark;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }

    public int getGroupWelcomeInfoId() {
        return groupWelcomeInfoId;
    }

    public void setGroupWelcomeInfoId(int groupWelcomeInfoId) {
        this.groupWelcomeInfoId = groupWelcomeInfoId;
    }

    @Override
    public boolean equals(Object obj) {
        return this.welcomeMessage.equals(((WelcomeMessage) obj).getWelcomeMessage());
    }

    /**
     * 修改 this 所保存的数据
     * 用于保存或更新
     *
     * @return boolean t 成功
     * @author Moyuyanli
     * @date 2023/8/4 10:33
     */
    @Override
    public boolean merge() {
        try {
            HibernateUtil.factory.fromTransaction(session -> session.merge(this));
        } catch (Exception e) {
            LOGGER.error("欢迎词消息保存失败！",e);
            return false;
        }
        return true;
    }

    /**
     * 删除
     *
     * @return boolean t 成功
     * @author Moyuyanli
     * @date 2023/8/4 10:34
     */
    @Override
    public boolean remove() {
        try {
            HibernateUtil.factory.fromTransaction(session -> {
                session.remove(this);
                return null;
            });
        } catch (Exception e) {
            LOGGER.error("欢迎词消息删除失败！",e);
            return false;
        }
        return true;
    }
}
