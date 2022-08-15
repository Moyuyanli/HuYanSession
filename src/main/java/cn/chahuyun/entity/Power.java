package cn.chahuyun.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :权限识别
 * @Date 2022/7/29 23:51
 */
@Entity
@Table(name = "power")
public class Power {

    /**
     * 唯一识别符
     */
    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;
    /**
     * 所属bot
     */
    private long bot;
    /**
     * 所属群
     */
    private long groupId;
    /**
     * 所属qq
     */
    private long qq;
    /**
     * 管理权限
     */
    private boolean admin;
    /**
     * 群组权限
     * 直接使用群组全部指令
     */
    private boolean groupList;
    /**
     * 学习权限
     * 学习的全部功能
     */
    private boolean session;
    /**
     * 学习单一回复的权限
     */
    private boolean sessionX;
    /**
     * 学习多词条的权限
     */
    private boolean sessionDct;
    /**
     * 定时器的权限
     */
    private boolean ds;
    /**
     * 定时器的开启关闭查询权限
     */
    private boolean dscz;
    /**
     * 群管理操作权限
     */
    private boolean groupManage;
    /**
     * 群欢迎词的权限
     */
    private boolean groupHyc;
    /**
     * 群禁言的权限
     */
    private boolean groupJy;
    /**
     * 群黑名单的权限
     */
    private boolean groupHmd;
    /**
     * 群消息撤回的权限
     */
    private boolean groupCh;
    /**
     * 群踢人权限
     */
    private boolean groupTr;

    public Power() {
    }

    public Power(long bot, long groupId, long qq) {
        this.id = bot + "." + groupId + "." + qq;
        this.bot = bot;
        this.groupId = groupId;
        this.qq = qq;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getBot() {
        return bot;
    }

    public void setBot(long bot) {
        this.bot = bot;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public long getQq() {
        return qq;
    }

    public void setQq(long qq) {
        this.qq = qq;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isGroupList() {
        return groupList;
    }

    public void setGroupList(boolean groupList) {
        this.groupList = groupList;
    }

    public boolean isSession() {
        return session;
    }

    public void setSession(boolean session) {
        this.session = session;
    }

    public boolean isSessionX() {
        return sessionX;
    }

    public void setSessionX(boolean sessionX) {
        this.sessionX = sessionX;
    }

    public boolean isSessionDct() {
        return sessionDct;
    }

    public void setSessionDct(boolean sessionDct) {
        this.sessionDct = sessionDct;
    }

    public boolean isDs() {
        return ds;
    }

    public void setDs(boolean ds) {
        this.ds = ds;
    }

    public boolean isDscz() {
        return dscz;
    }

    public void setDscz(boolean dscz) {
        this.dscz = dscz;
    }

    public boolean isGroupManage() {
        return groupManage;
    }

    public void setGroupManage(boolean groupManage) {
        this.groupManage = groupManage;
    }

    public boolean isGroupHyc() {
        return groupHyc;
    }

    public void setGroupHyc(boolean groupHyc) {
        this.groupHyc = groupHyc;
    }

    public boolean isGroupJy() {
        return groupJy;
    }

    public void setGroupJy(boolean groupJy) {
        this.groupJy = groupJy;
    }

    public boolean isGroupHmd() {
        return groupHmd;
    }

    public void setGroupHmd(boolean groupHmd) {
        this.groupHmd = groupHmd;
    }

    public boolean isGroupCh() {
        return groupCh;
    }

    public void setGroupCh(boolean groupCh) {
        this.groupCh = groupCh;
    }

    public boolean isGroupTr() {
        return groupTr;
    }

    public void setGroupTr(boolean groupTr) {
        this.groupTr = groupTr;
    }

    @Override
    public String toString() {
        return "权限列表:" +
                "\nadmin         -管理员:" + (admin ? "是" : "否") +
                "\nlist        \t-群组管理权限:" + (groupList ? "是" : "否") +
                "\nsession       -会话管理权限:" + (session ? "是" : "否") +
                "\nsessionx      -会话管理权限(单一):" + (sessionX ? "是" : "否") +
                "\nsessiondct   -会话管理权限(多词条):" + (sessionDct ? "是" : "否") +
                "\nds         \t-定时任务管理权限:" + (ds ? "是" : "否") +
                "\ndscz      \t-定时任务控制权限:" + (dscz ? "是" : "否") +
                "\ngroup          -群操作管理权限:" + (groupManage ? "是" : "否") +
                "\ngroupHyc     -群操作欢迎词权限:" + (groupHyc ? "是" : "否") +
                "\ngroupJy       -群操作禁言权限:" + (groupJy ? "是" : "否") +
                "\ngroupHmd   -群操作黑名单权限:" + (groupHmd ? "是" : "否") +
                "\ngroupCh      -群操作消息撤回权限:" + (groupCh ? "是" : "否") +
                "\ngroupTr       -群操作踢人权限:" + (groupTr ? "是" : "否");
    }

    /**
     * 设置全部权限为 true
     *
     * @author Moyuyanli
     * @date 2022/8/14 18:07
     */
    public void setAll() {
        this.admin = true;
        this.groupList = true;
        this.session = true;
        this.sessionX = true;
        this.sessionDct = true;
        this.ds = true;
        this.dscz = true;
        this.groupManage = true;
        this.groupHyc = true;
        this.groupJy = true;
        this.groupHmd = true;
        this.groupCh = true;
        this.groupTr = true;
    }
}
