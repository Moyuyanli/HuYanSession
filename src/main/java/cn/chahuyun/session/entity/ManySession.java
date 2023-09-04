package cn.chahuyun.session.entity;

import cn.chahuyun.session.utils.HibernateUtil;
import jakarta.persistence.*;

import static cn.chahuyun.session.HuYanSession.LOGGER;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :多词条消息的消息信息
 * @Date 2022/8/17 19:20
 */
@Entity
@Table(name = "ManySession")
public class ManySession implements BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private int manySessionId;

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

    public int getManySessionId() {
        return manySessionId;
    }

    public void setManySessionId(int manySessionId) {
        this.manySessionId = manySessionId;
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
            LOGGER.error("多词条消息保存失败！");
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
            LOGGER.error("多词条消息删除失败！");
            return false;
        }
        return true;
    }
}
