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
     * false 全局
     */
    private Boolean type;

    /**
     * 作用域识别符
     */
    private String scopeCode;

    private int scopeNum;


    public ScopeInfoBase(String scope, Boolean type, Long scopeCode, int scopeNum) {
        this.scope = scope;
        this.type = type;
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

    public String getScopeCode() {
        return scopeCode;
    }

    public void setScopeCode(String scopeCode) {
        this.scopeCode = scopeCode;
    }
}