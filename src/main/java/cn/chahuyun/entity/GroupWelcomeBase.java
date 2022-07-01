package cn.chahuyun.entity;

/**
 * GroupWelcomeBase
 *
 * @author Zhangjiaxing
 * @description 群消息欢迎信息实体类
 * @date 2022/7/1 10:23
 */
public class GroupWelcomeBase {

    private String key;
    private String value;
    private ScopeInfoBase scopeInfo;

    public GroupWelcomeBase(String key, String value, ScopeInfoBase scopeInfo) {
        this.key = key;
        this.value = value;
        this.scopeInfo = scopeInfo;
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

    public ScopeInfoBase getScopeInfo() {
        return scopeInfo;
    }

    public void setScopeInfo(ScopeInfoBase scopeInfo) {
        this.scopeInfo = scopeInfo;
    }

}