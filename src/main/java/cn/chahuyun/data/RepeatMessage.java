package cn.chahuyun.data;

import java.util.Date;

/**
 * RepeatMessage
 * 重复消息判断
 *
 * @author Zhangjiaxing
 * @date 2022/8/18 16:03
 */
public class RepeatMessage {


    private Date oldDate;

    private int numberOf;

    public RepeatMessage(Date oldDate, int numberOf) {
        this.oldDate = oldDate;
        this.numberOf = numberOf;
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
}