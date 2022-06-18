package cn.chahuyun.Session;

import cn.chahuyun.GroupSession;
import cn.chahuyun.Session.Criticaldialog.SpecialDialogue;
import cn.chahuyun.Session.Criticaldialog.SessionDialogue;
import cn.chahuyun.config.PowerConfig;
import cn.chahuyun.config.PowerConfigBase;
import cn.chahuyun.data.SessionDataBase;
import cn.chahuyun.enumerate.MessEnum;
import net.mamoe.mirai.console.permission.AbstractPermitteeId;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.utils.MiraiLogger;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.chahuyun.GroupSession.sessionData;


/**
 * 基本对话事件
 *
 * @author Zhangjiaxing
 * @description 触发基本对话
 * @date 2022/6/8 9:18
 */
public class DialogueBasic {

    private static MiraiLogger l = GroupSession.INSTANCE.getLogger();

    /**
     * 指令正则
     */
    public static String commandPattern = "查询 ?|学习 |删除 |([+-]\\[mirai:at:\\d+\\] [\\w]+)";
    /**
     * 回复消息正则
     */
    public static String dialoguePattern = "噗~|斗地主|帮助";

    /**
     * @description 所有消息的入口
     * @author zhangjiaxing
     * @param event 消息监视器
     * @date 2022/6/16 15:25
     * @return void
     */
    public static void isMessageWhereabouts(MessageEvent event) {
        //判断消息传递类型
        MessEnum messEnum = null;
        //获取到的消息
        String messageToString = event.getMessage().serializeToMiraiCode();

        /*
        判断是否是对话类消息
         */
        //获取对话数据
        ArrayList<SessionDataBase> sessionPattern = new ArrayList<>(sessionData.values()) ;
        //创建触发对话结果
        SessionDataBase sessionDataBase = null;
        //循环判断
        for (SessionDataBase base : sessionPattern) {
            switch (base.getDataEnum().getTypeInt()) {
                //TypeInt = 1 -> 精准匹配
                case 1:
                    if (base.getKey().equals(messageToString)) {
                        l.info("匹配对话成功 " + base.getKey() + " -> " + base.getValue());
                        messEnum = MessEnum.SESSION;
                        sessionDataBase = base;
                    }
                    break;
                //TypeInt = 2 -> 模糊匹配
                case 2:
                    if (messageToString.contains(base.getKey())) {
                        l.info("匹配对话成功 " + base.getKey() + " -> " + base.getValue());
                        messEnum = MessEnum.SESSION;
                        sessionDataBase = base;
                    }
                    break;
                //TypeInt = 3 -> 头部匹配
                case 3:
                    String firstSubstring;
                    if (messageToString.length() >= base.getKey().length()) {
                        firstSubstring = messageToString.substring(0, base.getKey().length());
//                        l.info("进行头部匹配 -> "+firstSubstring);
                    } else {
                        firstSubstring = messageToString;
                    }
                    if (firstSubstring.equals(base.getKey())) {
                        l.info("匹配对话成功 " + base.getKey() + " -> " + base.getValue());
                        messEnum = MessEnum.SESSION;
                        sessionDataBase = base;
                    }
                    break;
                //TypeInt = 4 -> 结尾匹配
                case 4:
                    String endSubstring;
                    if (messageToString.length() >= base.getKey().length()) {
                        endSubstring = messageToString.substring(messageToString.length() - base.getKey().length());
//                        l.info("进行结尾匹配 -> "+endSubstring);
                    } else {
                        endSubstring = messageToString;
                    }
                    if (endSubstring.equals(base.getKey())) {
                        l.info("匹配对话成功 " + base.getKey() + " -> " + base.getValue());
                        messEnum = MessEnum.SESSION;
                        sessionDataBase = base;
                    }
                    break;
                default:
                    break;
            }
        }


        /*
        判断是否是指令消息
         */
        //拼接权限识别字符
        String userPower = "m" + event.getSubject().getId()  + "." + event.getSender().getId();
        //获取配置中权限map
        Map<String, PowerConfigBase> adminList = PowerConfig.INSTANCE.getAdminList();
        //先判断map是否为空，如果为不为空，在判断该用户是否存在,不存在直接不判断能否使用指令
        if (adminList != null && adminList.containsKey(userPower)) {
            Matcher matcher = Pattern.compile(commandPattern).matcher(messageToString);
            if (matcher.find()) {
                messEnum = MessEnum.COMMAND;
            }
        }


        /*
        判断是否是回复消息
         */
        if (Pattern.matches(dialoguePattern,messageToString)) {
            messEnum = MessEnum.REPLY;
        }



        /*
        判断是否触发消息回复
         */
        if (messEnum == null) {
            return;
        } else {
            l.info("识别到"+messEnum.getMessageType());
        }

        //分支
        switch (messEnum.getMessageTypeInt()) {
            //SESSION("会话消息",1)
            case 1:
                SessionDialogue.session(event,sessionDataBase);
                messEnum = null;
                break;
            //COMMAND("指令消息",2)
            case 2:
                l.info(userPower);
                if (adminList.get(userPower).isSessionPower()) {
                    if (messageToString.indexOf("学习") == 0) {
                        l.info("学习指令");
                        SessionManage.studySession(event);
                    } else if (messageToString.indexOf("查询") == 0) {
                        l.info("查询指令");
                        SessionManage.querySession(event);
                    } else if (messageToString.indexOf("删除") == 0) {
                        l.info("删除指令");
                        SessionManage.deleteSession(event);
                    }
                } else if (adminList.get(userPower).isAdminPower()) {
                    if (Pattern.matches("([+-]\\[mirai:at:\\d+\\] [\\w]+)", messageToString)) {
                        l.info("权限指令");
                        messageToPower(event);
                    }
                }
                messEnum = null;
                break;
            //REPLY("回复消息",3)
            case 3:
                switch (messageToString){
                    case "噗~":
                        SpecialDialogue.sessionPu(event);
                        break;
                    case "斗地主":
                        SpecialDialogue.sessionDou(event);
                        break;
                    default:
                        break;
                }
                messEnum = null;
                break;
            default:
                messEnum = null;
                break;
        }
    }

    /**
     * @description 用于处理权限消息的字符串数组
     * @author zhangjiaxing
     * @param event
     * @date 2022/6/19 1:18
     * @return void
     */
    private static void messageToPower(MessageEvent event) {
        //先将发送的消息转换为string
        String string = event.getMessage().contentToString();
        l.info(string);
        //修改类型
        String s = string.substring(0, 1);
        //去掉 修改类型
        string = string.substring(1);
        //通过>分割
        String[] split = string.split(">");
        //权限类参数
        String power = split[1];
        //匹配正则,获取中段消息的所有数字，疑似qq
        Matcher matcher = Pattern.compile("(@\\d+)").matcher(split[0]);
        //这里需要先进行一次匹配，find
        matcher.find();
        //然后才能通关group获取,然后把@给截取掉
        String qq = matcher.group().substring(1);
        //拼接权限类识别用户字符
        String user = "m"+event.getSubject().getId()+"."+qq;
        //进行设置
        MessageChain messages = PowerConfig.INSTANCE.setAdminList(s,user,power);
        //返回消息
        event.getSubject().sendMessage(messages);
    }

}