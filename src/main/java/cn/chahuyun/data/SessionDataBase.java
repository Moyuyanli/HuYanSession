package cn.chahuyun.data;

import cn.chahuyun.enumerate.DataEnum;

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
     */
    private int type = 0;
    /**
     * 触发结果-字符
     */
    private String value;
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
}