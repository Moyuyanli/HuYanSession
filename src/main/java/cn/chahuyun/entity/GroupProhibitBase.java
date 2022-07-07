package cn.chahuyun.entity;

/**
 * GroupProhibitBase
 *
 * @author Zhangjiaxing
 * @description 群消息禁言词
 * @date 2022/7/6 16:38
 */
public class GroupProhibitBase {

    /**
     * 标记
     */
    private String key;

    /**
     * 触发内容
     */
    private String value;

    /**
     * 禁言回复内容
     */
    private String reply;

    /**
     * 禁言时间
     */
    private String prohibit;

    /**
     * 禁言时间
     */
    private int prohibitNum;

    /**
     * 作用域
     */
    private ScopeInfoBase scope;


    public GroupProhibitBase(String key, String value, String reply, String prohibit, int prohibitNum, ScopeInfoBase scope) {
        this.key = key;
        this.value = value;
        this.reply = reply;
        this.prohibit = prohibit;
        this.prohibitNum = prohibitNum;
        this.scope = scope;
    }

    public GroupProhibitBase() {

    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getProhibit() {
        return prohibit;
    }

    public void setProhibit(String prohibit) {
        this.prohibit = prohibit;
    }

    public ScopeInfoBase getScope() {
        return scope;
    }

    public void setScope(ScopeInfoBase scope) {
        this.scope = scope;
    }

    public int getProhibitNum() {
        return prohibitNum;
    }

    public void setProhibitNum(int prohibitNum) {
        this.prohibitNum = prohibitNum;
    }
}