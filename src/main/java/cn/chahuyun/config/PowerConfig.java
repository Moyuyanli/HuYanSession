package cn.chahuyun.config;

import cn.chahuyun.HuYanSession;
import com.alibaba.fastjson.JSONArray;
import jdk.internal.net.http.common.Log;
import net.mamoe.mirai.console.data.Value;
import net.mamoe.mirai.console.data.java.JavaAutoSavePluginConfig;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 说明
 * 用于管理添加指令管理员
 * 用于判断是否拥有改权限
 *
 * @author Moyuyanli
 * @Description :各类指令权限
 * @Date 2022/6/18 23:33
 */
public class PowerConfig extends JavaAutoSavePluginConfig {

    /**
     * 唯一构造
     */
    public static final PowerConfig INSTANCE = new PowerConfig();

    /**
     * 文件名
     */
    public PowerConfig() {
        super("config");
    }

    /**
     * 主人识别
     */
    private final Value<Long> owner = typedValue("owner", createKType(Long.class));
    /**
     * 机器人识别
     */
    private final Value<Long> bot = typedValue("bot", createKType(Long.class));


    private final Value<Boolean> douSwitch = typedValue("斗地主帮助显示", createKType(Boolean.class),false);
    private final Value<Boolean> linkSwitch = typedValue("详细帮助链接显示", createKType(Boolean.class),false);

    /**
     * 群号
     */
    private final Value<List<Long>> groupList = typedValue("groupList", createKType(List.class, createKType(Long.class)));


    /**
     * 权限存储识别法
     */
    private final Value<Map<String, String>> powerList = typedValue("powerList",
            createKType(Map.class,
                    createKType(String.class),
                    createKType(String.class)
            ));

    /**
     * @return java.util.Map<java.lang.String, cn.chahuyun.config.PowerConfigBase>
     * @description 获取权限map
     * @author zhangjiaxing
     * @date 2022/6/19 0:51
     */
    public Map<String, PowerConfigBase> getPowerList() {
        HashMap<String, PowerConfigBase> stringPowerConfigBaseHashMap = new HashMap<String, PowerConfigBase>();
        Map<String, String> stringStringMap = this.powerList.get();
        for (String key : stringStringMap.keySet()) {
            stringPowerConfigBaseHashMap.put(key, JSONArray.parseObject(stringStringMap.get(key), PowerConfigBase.class));
        }
        return stringPowerConfigBaseHashMap;
    }

    /**
     * @param s     修改类型
     * @param user  用户匹配
     * @param power 权限
     * @return net.mamoe.mirai.message.data.MessageChain
     * @description 根据传递消息进行权限的修改
     * @author zhangjiaxing
     * @date 2022/6/19 2:43
     */
    public MessageChain setAdminList(String s, String user, String power) {
        //创建返回消息构造器
        MessageChainBuilder messages = new MessageChainBuilder();
        //获取3类数据 添加or删除  用户识别符  权限
        //判断
        if (s.equals("+")) {
            //添加权限，直接新建然后覆盖，就可以不用从本地重新获取
            PowerConfigBase base = new PowerConfigBase(user);
            switch (power) {
                case "admin":
                    base.setAdminPower(true);
                    messages.append("权限管理员权限添加成功！");
                    break;
                case "session":
                    base.setSessionPower(true);
                    messages.append("会话管理员权限添加成功！");
                    break;
                case "group":
                    base.setGroupPower(true);
                    messages.append("群管理员权限添加成功！");
                    break;
                case "all":
                    base.setAdminPower(true);
                    base.setSessionPower(true);
                    base.setGroupPower(true);
                    messages.append("管理员添加成功！");
                    break;
                default:
                    messages.append("添加失败，未识别权限！");
                    break;
            }
            String jsonString = JSONArray.toJSONString(base);
            //添加or覆盖
            this.powerList.get().put(user, jsonString);
            HuYanSession.INSTANCE.getLogger().info("添加权限: " + user + " " + power);
            return messages.build();
        } else {
            //先从本地获取数据
            Map<String, String> baseMap = this.powerList.get();
            //创建一个空权限base
            PowerConfigBase base = null;
            //查找本地有没有该用户的权限base
            for (String k : baseMap.keySet()) {
                if (k.equals(user)) {
                    base = JSONArray.parseObject(baseMap.get(k), PowerConfigBase.class);
                }
            }
            //如果没有，直接返回失败
            if (base == null) {
                messages.append("删除权限失败，没有找到该用户！");
                return messages.build();
            }
            switch (power) {
                case "admin":
                    base.setAdminPower(false);
                    messages.append("权限管理员权限删除成功！");
                    break;
                case "session":
                    base.setSessionPower(false);
                    messages.append("会话管理员权限删除成功！");
                    break;
                case "group":
                    base.setGroupPower(false);
                    messages.append("群管理员权限删除成功！");
                    break;
                case "all":
                    //当删除全部权限的时候，直接删除该用户的权限base
                    this.powerList.get().remove(user);
                    messages.append("管理员删除成功！");
                default:
                    messages.append("删除失败，未识别权限！");
                    break;
            }
            String jsonString = JSONArray.toJSONString(base);
            //覆盖
            this.powerList.get().put(user, jsonString);
            HuYanSession.INSTANCE.getLogger().info("删除权限: " + user + " " + power);
            return messages.build();
        }
    }


    public Long getOwner() {
        return owner.get();
    }

    public Long getBot() {
        return bot.get();
    }

    public Value<Boolean> getDouSwitch() {
        return douSwitch;
    }

    public Value<Boolean> getLinkSwitch() {
        return linkSwitch;
    }

    public ArrayList<Long> getGroupList() {
        return new  ArrayList<Long>(groupList.get());
    }

    public MessageChain setGroupList(Boolean operate,Long group) {
        if (operate) {
            this.groupList.get().add(group);
            return new MessageChainBuilder().append("群" + group + "检测添加成功!").build();
        } else {
            try {
                this.groupList.get().remove(group);
                return new MessageChainBuilder().append("群" + group + "检测删除成功!").build();
            } catch (Exception e) {
                return new MessageChainBuilder().append("没有该群!").build();
            }
        }
    }


}
