package cn.chahuyun.data;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.enumerate.DataEnum;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.util.ArrayList;

/**
 * SessionData
 *
 * @author Zhangjiaxing
 * @description 会话数据
 * @date 2022/6/16 10:30
 */
public class SessionDataBase{
    /**
     * 触发关键词
     */
    private String key;
    /**
     * 触发结果类型
     * 默认为字符 = 0 string类回复
     * 默认为字符 = 1 图片类回复
     * 默认为字符 = 2 多词条轮询类回复
     * 默认为字符 = 3 多词条随机类回复
     */
    private int type = 0;
    /**
     * 触发结果-字符
     */
    private String value;
    /**
     * 多词条回复集合
     */
    private final ArrayList<String> values = new ArrayList<String>();
    /**
     * 轮询用验证次数
     */
    private int poll;
    /**
     * 触发条件
     */
    private DataEnum dataEnum;
    /**
     * 触发范围
     * 默认本群，以枚举保存
     */
    private ScopeInfo scopeInfo;
    /**
     * @description 构建
     * @author zhangjiaxing
     * @param key 触发词
     * @param type 消息类型
     * @param value 返回字符消息
     * @param dataEnum 触发条件
     * @param scopeInfo 触发作用域
     * @date 2022/6/16 14:30
     */
    public SessionDataBase(String key, int type, String value, DataEnum dataEnum, ScopeInfo scopeInfo) {
        this.key = key;
        this.type = type;
        this.value = value;
        this.dataEnum = dataEnum;
        this.scopeInfo = scopeInfo;
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

    public int getPoll() {
        return poll;
    }

    public void setPoll(int poll) {
        this.poll = poll;
    }

    /**
     * @description 返回+1poll
     * @author zhangjiaxing
     * @date 2022/6/24 10:40
     * @return int
     */
    public int getPollAdd() {
        return this.poll++;
    }

    public DataEnum getDataEnum() {
        return dataEnum;
    }

    public void setDataEnum(DataEnum dataEnum) {
        this.dataEnum = dataEnum;
    }

    public ScopeInfo getScopeInfo() {
        return scopeInfo;
    }

    public void setScopeInfo(ScopeInfo scopeInfo) {
        this.scopeInfo = scopeInfo;
    }

    public ArrayList<String> getValues() {
        return values;
    }

    /**
     * @description 添加或删除多词条单独信息
     * @author zhangjiaxing
     * @param b t 添加 f 删除
     * @param value 添加或删除的内容
     * @date 2022/6/23 16:47
     * @return net.mamoe.mirai.message.data.MessageChain
     */
    public MessageChain setValues(boolean b,String value) {
        if (b) {
            this.values.add(value);
            return new MessageChainBuilder().append("多词条回复添加成功！").build();
        } else {
            HuYanSession.INSTANCE.getLogger().info("c-"+this.values.contains(value));
            HuYanSession.INSTANCE.getLogger().info("c-"+value);
            if (this.values.contains(value)) {
                this.values.remove(value);
                return new MessageChainBuilder().append("多词条回复删除成功！").build();
            } else {
                return new MessageChainBuilder().append("多词条回复删除失败！").build();
            }

        }
    }

}