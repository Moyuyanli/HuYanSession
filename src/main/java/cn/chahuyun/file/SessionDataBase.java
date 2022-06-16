package cn.chahuyun.file;

import cn.chahuyun.enumerate.DataEnum;
import net.mamoe.mirai.message.data.MessageChain;

/**
 * SessionData
 *
 * @author Zhangjiaxing
 * @description 会话数据
 * @date 2022/6/16 10:30
 */
public class SessionDataBase{
    //触发关键词
    private String key;
    //触发结果类型
    private int type = 0;
    //触发结果-字符
    private String value;
    //触发结果(所有)
    private MessageChain messageChain;
    //触发条件
    private DataEnum dataEnum;
    /**
     * @description 构建
     * @author zhangjiaxing
     * @param key 触发词
     * @param type 返回消息类型
     * @param value 返回字符消息
     * @param messageChain 返回消息全部类型
     * @param dataEnum 触发条件
     * @date 2022/6/16 14:30
     * @return viod
     */
    public SessionDataBase(String key, int type, String value, MessageChain messageChain, DataEnum dataEnum) {
        this.key = key;
        this.type = type;
        this.value = value;
        this.messageChain = messageChain;
        this.dataEnum = dataEnum;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public MessageChain getMessageChain() {
        return messageChain;
    }

    public void setMessageChain(MessageChain messageChain) {
        this.messageChain = messageChain;
    }

    public DataEnum getDataEnum() {
        return dataEnum;
    }

    public void setDataEnum(DataEnum dataEnum) {
        this.dataEnum = dataEnum;
    }
}