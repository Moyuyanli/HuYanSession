package cn.chahuyun.entity;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :实体基类
 * @Date 2022/7/8 21:21
 */
public class Base {

    /**
     * id
     */
    private int id;

    /**
     * 所属机器人
     */
    private long bot;

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
}
