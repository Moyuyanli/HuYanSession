package cn.chahuyun.data;

import java.util.Date;

/**
 * RepeatMessage
 * 重复消息判断
 *
 * @author Moyuyanli
 * @date 2022/8/18 16:03
 */
public class RepeatMessage {


    private Date oldDate;

    private int numberOf;

    private boolean replyTo;

    public RepeatMessage(Date oldDate, int numberOf) {
        this.oldDate = oldDate;
        this.numberOf = numberOf;
        this.replyTo = false;
    }

    public Date getOldDate() {
        return oldDate;
    }

    public void setOldDate(Date oldDate) {
        this.oldDate = oldDate;
    }

    public int getNumberOf() {
        return numberOf;
    }

    public void setNumberOf(int numberOf) {
        this.numberOf = numberOf;
    }

    public boolean isReplyTo() {
        return replyTo;
    }

    public void setReplyTo(boolean replyTo) {
        this.replyTo = replyTo;
    }
}