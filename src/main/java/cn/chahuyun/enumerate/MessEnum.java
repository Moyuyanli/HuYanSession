package cn.chahuyun.enumerate;

/**
 * MessEnum
 *
 * @author Zhangjiaxing
 * @description 消息枚举
 * @date 2022/6/16 11:19
 */
public enum MessEnum {

    //会话消息
    SESSION("会话消息",1),
    //指令消息
    COMMAND("指令消息",2),
    //回复消息
    REPLY("回复消息",3);

    private String messageType;
    private int messageTypeInt;

    MessEnum(String messageType, int messageTypeInt) {
        this.messageType = messageType;
        this.messageTypeInt = messageTypeInt;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public int getMessageTypeInt() {
        return messageTypeInt;
    }

    public void setMessageTypeInt(int messageTypeInt) {
        this.messageTypeInt = messageTypeInt;
    }
}