package cn.chahuyun.data;

/**
 * RepeatMessage
 * 重复消息判断
 *
 * @author Zhangjiaxing
 * @date 2022/8/18 16:03
 */
public class RepeatMessage {

    private long groupId;

    private String key;

    private int numberOf;

    public RepeatMessage(long groupId, String key, int numberOf) {
        this.groupId = groupId;
        this.key = key;
        this.numberOf = numberOf;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getNumberOf() {
        return numberOf;
    }

    public void setNumberOf(int numberOf) {
        this.numberOf = numberOf;
    }
}