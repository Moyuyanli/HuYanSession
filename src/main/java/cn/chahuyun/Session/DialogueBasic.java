package cn.chahuyun.Session;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.Session.Criticaldialog.SessionDialogue;
import cn.chahuyun.Session.Criticaldialog.SpecialDialogue;
import cn.chahuyun.config.ConfigData;
import cn.chahuyun.config.PowerConfigBase;
import cn.chahuyun.data.SessionData;
import cn.chahuyun.data.SessionDataBase;
import cn.chahuyun.enumerate.MessEnum;
import cn.chahuyun.groupManager.GroupManager;
import cn.chahuyun.power.Permissions;
import cn.chahuyun.sessionManager.SessionManage;
import cn.chahuyun.utils.MessageUtil;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.utils.MiraiLogger;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 基本对话事件
 *
 * @author Zhangjiaxing
 * @description 触发基本对话
 * @date 2022/6/8 9:18
 */
public class DialogueBasic {

    public static final DialogueBasic INSTANCE = new DialogueBasic();

    private  MiraiLogger l = HuYanSession.INSTANCE.getLogger();

    private final MessageUtil util = MessageUtil.INSTANCE;
    /**
     * 指令正则
     */
    public String commandPattern = "查询 ?|学习(多词条)?\\s+|删除(多词条)? |([+-]\\[mirai:at:\\d+\\] [\\w]+)|添加多词条 ";
    /**
     * 群管指令正则
     */
    public String groupPattern = "([+-][\\d\\w\\u4e00-\\u9fa5]+[:：][\\d\\w\\S\\u4e00-\\u9fa5]+)|查询[欢迎新]+词|(\\[mirai:at:\\d+\\] \\d+[s|d|h|m])|(踢人\\[mirai:at:\\d+\\])";
    /**
     * 回复消息正则
     */
    public String dialoguePattern = "斗地主|帮助";

    public Long owner = ConfigData.INSTANCE.getOwner();

    /**
     * @description 所有消息的入口
     * @author zhangjiaxing
     * @param event 消息监视器
     * @date 2022/6/16 15:25
     * @return void
     */
    public  void isMessageWhereabouts(MessageEvent event) {
        //判断消息传递类型
        MessEnum messEnum = null;
        //获取到的消息
        String messageToString = event.getMessage().serializeToMiraiCode();

        /*
        判断是否是对话类消息
         */
        //获取对话数据
        ArrayList<SessionDataBase> sessionPattern = new ArrayList<>(SessionData.INSTANCE.getSessionMap().values()) ;
        //创建触发对话结果
        SessionDataBase sessionDataBase = null;
        //循环判断
        for (SessionDataBase base : sessionPattern) {
            //判断是全局还是当前群
            if (base.getScopeInfo().getType()) {
                if (base.getScopeInfo().getScopeCode() != event.getSubject().getId()) {
                    continue;
                }
            }
            //匹配
            switch (base.getDataEnum()) {
                //TypeInt = 1 -> 精准匹配
                case ACCURATE:
                    if (base.getKey().equals(messageToString)) {
                        l.info("匹配对话成功 " + base.getKey() + " -> " + base.getValue());
                        messEnum = MessEnum.SESSION;
                        sessionDataBase = base;
                    }
                    break;
                //TypeInt = 2 -> 模糊匹配
                case VAGUE:
                    if (messageToString.contains(base.getKey())) {
                        l.info("匹配对话成功 " + base.getKey() + " -> " + base.getValue());
                        messEnum = MessEnum.SESSION;
                        sessionDataBase = base;
                    }
                    break;
                //TypeInt = 3 -> 头部匹配
                case START:
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
                case END:
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
        Map<String, PowerConfigBase> adminList = ConfigData.INSTANCE.pickPowerList();
        //优先判断是否为主人
        //先判断map是否为空，如果为不为空，在判断该用户是否存在,不存在直接不判断能否使用指令
        if ( event.getSender().getId() == owner ||(adminList != null && adminList.containsKey(userPower))) {
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

        /**
         * 群管消息判断
         */
        Pattern pattern = Pattern.compile(groupPattern);
        Matcher groupMatcher = pattern.matcher(messageToString);
        if (owner == event.getSender().getId() || (adminList != null && adminList.containsKey(userPower) && adminList.get(userPower).isGroupPower())) {
            if (groupMatcher.find()) {
                messEnum = MessEnum.GROUP;
            }
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
        switch (messEnum) {
            //SESSION("会话消息",1)
            case SESSION:
                SessionDialogue.INSTANCE.session(event,sessionDataBase);
                messEnum = null;
                break;
            //COMMAND("指令消息",2)
            case COMMAND:
                if (owner == event.getSender().getId() || adminList.get(userPower).isSessionPower()) {
                    if (util.isStudyCommand(event)) {
                        l.info("学习指令");
                        SessionManage.INSTANCE.studySession(event);
                    } else if (messageToString.indexOf("查询") == 0) {
                        l.info("查询指令");
                        SessionManage.INSTANCE.querySession(event);
                    } else if (util.isDeleteCommand(event)) {
                        l.info("删除指令");
                        SessionManage.INSTANCE.deleteSession(event);
                    } else if (Pattern.matches("添加多词条 (\\S+)", messageToString)) {
                        l.info("添加多词条指令");
                        util.isRepeatedlyAddMessage(event);
                    }
                }
                if ( owner == event.getSender().getId() || adminList.get(userPower).isAdminPower()) {
                    if (Pattern.matches("([+-]\\[mirai:at:\\d+\\] [\\w]+)", messageToString)) {
                        l.info("权限指令");
                        Permissions.INSTANCE.messageToPower(event);
                    }
                }
                messEnum = null;
                break;
            //REPLY("回复消息",3)
            case REPLY:
                switch (messageToString){
                    case "斗地主":
                        if (ConfigData.INSTANCE.getDouSwitch()) {
                            SpecialDialogue.INSTANCE.sessionDou(event);
                        }
                        break;
                    case "帮助":
                        SpecialDialogue.INSTANCE.sessionHelp(event);
                    default:
                        break;
                }
                messEnum = null;
                break;
            //GROUP("群管消息", 4)
            case GROUP:
                String groupMessage = groupMatcher.group();
                if (groupMessage.equals("查询欢迎词") || groupMessage.equals("查询迎新词") ) {
                    l.info("查询迎新词指令");
                    GroupManager.INSTANCE.checkGroupWelcomeMessage(event);
                }else if (Pattern.matches("([+-][\\d\\w\\u4e00-\\u9fa5]+[:：][\\d\\w\\S\\u4e00-\\u9fa5]+)", messageToString)) {
                    l.info("添加迎新词指令");
                    GroupManager.INSTANCE.setGroupWelcomeMessage(event);
                }else if (Pattern.matches("(\\[mirai:at:\\d+\\] \\d+[s|d|h|m])",messageToString)){
                    l.info("禁言指令");
                    GroupManager.INSTANCE.prohibit(event);
                } else if (Pattern.matches("(踢人\\[mirai:at:\\d+\\])", messageToString)) {
                    l.info("踢人指令");
                    GroupManager.INSTANCE.kick(event);
                }
                break;
            default:
                messEnum = null;
                break;
        }
    }


}