package cn.chahuyun.data;

/**
 * ScopeEnum
 *
 * @author Zhangjiaxing
 * @description 控制自定义消息的作用域
 * @date 2022/6/20 8:37
 */
public class ScopeInfo {

    /**
     * 作用域名称
     */
    private String Scope;

    /**
     * true 当前
     * false 全局
     */
    private Boolean type;

    /**
     * 作用域坐标
     */
    private Long scopeCode;

    

    public ScopeInfo(String scope, Boolean type, Long scopeCode) {
        Scope = scope;
        this.type = type;
        this.scopeCode = scopeCode;
    }

    public String getScope() {
        return Scope;
    }

    public void setScope(String scope) {
        Scope = scope;
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
}