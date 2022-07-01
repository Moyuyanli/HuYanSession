package cn.chahuyun.entity;

/**
 * ScopeEnum
 *
 * @author Zhangjiaxing
 * @description 控制自定义消息的作用域
 * @date 2022/6/20 8:37
 */
public class ScopeInfoBase {

    /**
     * 作用域名称
     */
    private String scope;

    /**
     * true 当前
     * false 群组或全局
     */
    private Boolean type;

    /**
     * true 群组
     * false 全局
     */
    private Boolean groupType;

    /**
     * 作用域识别符
     */
    private Long scopeCode;

    /**
     * 群组编号
     */
    private int scopeNum;


    public ScopeInfoBase(String scope, Boolean type, Boolean groupType, Long scopeCode, int scopeNum) {
        this.scope = scope;
        this.type = type;
        this.groupType = groupType;
        this.scopeCode = scopeCode;
        this.scopeNum = scopeNum;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        scope = scope;
    }

    public Boolean getType() {
        return type;
    }

    public void setType(Boolean type) {
        this.type = type;
    }

    public Long getScopeCode() {
        return scopeCode;
    }

    public void setScopeCode(Long scopeCode) {
        this.scopeCode = scopeCode;
    }

    public int getScopeNum() {
        return scopeNum;
    }

    public void setScopeNum(int scopeNum) {
        this.scopeNum = scopeNum;
    }

    public Boolean getGroupType() {
        return groupType;
    }

    public void setGroupType(Boolean groupType) {
        this.groupType = groupType;
    }
}