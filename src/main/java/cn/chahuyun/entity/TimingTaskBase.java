package cn.chahuyun.entity;

import java.util.List;

/**
 * TimingTaskBase
 *
 * @author Zhangjiaxing
 * @description 定时器任务基础实体类
 * @date 2022/6/28 11:12
 */
public class TimingTaskBase {
    /**
     * 定时器id
     */
    private int id;
    /**
     * 定时器任务名称
     */
    private String name;
    /**
     * 定时器时间解析
     */
    private String timeResolve;
    /**
     * 定时器cron
     */
    private String cronString;
    /**
     * 定时器回复内容类型
     */
    private int type;
    /**
     * 定时器回复内容
     */
    private String value;
    /**
     * 定时器多词条回复内容
     */
    private List<String> values;
    /**
     * 轮询次数
     */
    private int poll;
    /**
     * 定时器作用群组
     */
    private ScopeInfoBase scope;
    /**
     * 定时器当前状态
     * false 关闭 true 开启
     */
    private Boolean state;

    public TimingTaskBase(int id, String name, String timeResolve, String cronString, int type, String value, List<String> values, int poll, ScopeInfoBase scope) {
        this.id = id;
        this.name = name;
        this.timeResolve = timeResolve;
        this.cronString = cronString;
        this.type = type;
        this.value = value;
        this.values = values;
        this.poll = poll;
        this.scope = scope;
        this.state = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimeResolve() {
        return timeResolve;
    }

    public void setTimeResolve(String timeResolve) {
        this.timeResolve = timeResolve;
    }

    public String getCronString() {
        return cronString;
    }

    public void setCronString(String cronString) {
        this.cronString = cronString;
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

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public int getPoll() {
        return poll;
    }

    public void setPoll(int poll) {
        this.poll = poll;
    }

    public ScopeInfoBase getScope() {
        return scope;
    }

    public void setScope(ScopeInfoBase scope) {
        scope = scope;
    }

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }
}