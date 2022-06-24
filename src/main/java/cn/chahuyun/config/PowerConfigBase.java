package cn.chahuyun.config;

/**
 * 说明
 * 用来存储管理员拥有权限的详细信息
 *
 * @author Moyuyanli
 * @Description :权限基本属性
 * @Date 2022/6/18 23:41
 */
public class PowerConfigBase {

    /**
     * 权限拥有着id
     * 利用内部权限的识别方式自己识别
     */
    private final String user;

    /**
     * 管理权限
     * 能否为别人添加权限以及修改权限
     */
    private boolean adminPower = false;

    /**
     * 关键词权限
     * 包括添加以及删除
     */
    private boolean sessionPower = false;

    /**
     * 群管理权限
     * 正常群管理员，只不过以机器人触发
     */
    private boolean groupPower = false;

    /**
     * @description 构造一个权限用户，默认无权限
     * @author zhangjiaxing
     * @param user 用户id
     * @date 2022/6/19 0:01
     */
    public PowerConfigBase(String user) {
        this.user = user;
    }

    public PowerConfigBase(String user, boolean adminPower, boolean sessionPower, boolean groupPower) {
        this.user = user;
        this.adminPower = adminPower;
        this.sessionPower = sessionPower;
        this.groupPower = groupPower;
    }

    public String getUser() {
        return user;
    }

    public boolean isAdminPower() {
        return adminPower;
    }

    public void setAdminPower(boolean adminPower) {
        this.adminPower = adminPower;
    }

    public boolean isSessionPower() {
        return sessionPower;
    }

    public void setSessionPower(boolean sessionPower) {
        this.sessionPower = sessionPower;
    }

    public boolean isGroupPower() {
        return groupPower;
    }

    public void setGroupPower(boolean groupPower) {
        this.groupPower = groupPower;
    }

}
