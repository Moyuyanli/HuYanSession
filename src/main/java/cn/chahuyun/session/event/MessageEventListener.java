package cn.chahuyun.session.event;

import cn.chahuyun.config.SessionConfig;
import cn.chahuyun.session.HuYanSession;
import cn.chahuyun.session.controller.*;
import cn.chahuyun.session.data.StaticData;
import cn.chahuyun.session.dialogue.DialogueImpl;
import cn.chahuyun.session.dialogue.DialogueProcessing;
import cn.chahuyun.session.entity.ManySessionInfo;
import cn.chahuyun.session.entity.Power;
import cn.chahuyun.session.entity.Session;
import cn.chahuyun.session.manage.DataManager;
import cn.chahuyun.session.manage.GroupManager;
import cn.chahuyun.session.utils.ShareUtils;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.utils.MiraiLogger;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :消息检测
 * @Date 2022/7/9 18:11
 */
public class MessageEventListener extends SimpleListenerHost {

    private static final MiraiLogger l = HuYanSession.INSTANCE.getLogger();

    @EventHandler()
    public void onMessage(@NotNull MessageEvent event) { // 可以抛出任何异常, 将在 handleException 处理
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        User sender = event.getSender();
        Bot bot = event.getBot();

        if (SessionConfig.INSTANCE.getBotsSwitch()) {
            //关闭多机器人自触发
            List<Bot> instances = Bot.getInstances();
            for (Bot instance : instances) {
                if (instance.getId() == sender.getId()) {
                    return;
                }
            }
        }

        if (SessionConfig.INSTANCE.getDebugSwitch()) {
            l.info("MiraiCode-> " + code);
        }


        //主人
        boolean owner = SessionConfig.INSTANCE.getOwner() == sender.getId();

        //权限用户识别符
        String powerString = subject.getId() + "." + sender.getId();

        Map<String, Power> powerMap = StaticData.getPowerMap(bot);
        if (SessionConfig.INSTANCE.getDebugSwitch()) {
            l.info("owner-" + owner + " power-" + powerMap.containsKey(powerString));
        }
        //是否是有权限的用户 true 是有权限的用户
        boolean powerUser = powerMap.containsKey(powerString);

        BlackListAction blackListAction = new BlackListAction();

        if (!owner) {
            blackListAction.isBlackUser(event);
            if (powerUser) {
                if (!powerMap.get(powerString).isGroupManage() && !powerMap.get(powerString).isGroupWjc()) {
                    if (GroupManager.INSTANCE.isProhibited(event)) {
                        return;
                    }
                }
            } else {
                if (GroupManager.INSTANCE.isProhibited(event)) {
                    return;
                }
            }
        }

        //机器人在群里的权限
        boolean isGroupAdmin = false;
        boolean isGroupOwner = false;
        if (subject instanceof Group) {
            if (RepeatMessageAction.isScreen(event)) {
                return;
            }
            Group group = (Group) subject;
            MemberPermission botPermission = group.getBotPermission();
            if (botPermission == MemberPermission.ADMINISTRATOR) {
                isGroupAdmin = true;
            } else if (botPermission == MemberPermission.OWNER) {
                isGroupOwner = true;
            }
        }

        /*
        不是主人
        没有指令权限
        没有触发违禁词
        → 直接跳过指令判断
         */
        if (!owner && !powerUser) {
            isSessionMessage(event);
            return;
        }

        /*
        用户权限
         */
        Power power = powerMap.get(powerString);
        if (power == null) {
            power = new Power();
        }
        boolean admin = power.isAdmin();
        /*
        特殊匹配
         */
        String pauseEventPattern = "^[!！]pause +\\[mirai:at:\\d+]( +\\d+)?";

        //是否忽略下一条消息
        if (ShareUtils.isPause(event)) {
            if (SessionConfig.INSTANCE.getDebugSwitch()) {
                l.info("本" + bot.getNick() + "(Bot)还不是很想理你");
            }
            return;
        }

        //添加忽略消息
        if (Pattern.matches(pauseEventPattern, code)) {
            l.info("添加忽略消息指令");
            ShareUtils.spotPause(event);
            return;
        }


        /*
        群组正则
         */
        String addListPattern = "^\\+?gr\\\\?[:：]\\d+( +\\d+)+|^添加群组\\\\?[:：]\\d+( +\\d+)+";
        String queryListPattern = "^gr\\\\?[:：](\\d+)?|^查询群组\\\\?[:：](\\d+)?";
        String deleteListPattern = "^-gr\\\\?[：:]\\d+( +\\d+)?|^删除群组\\\\?[:：]\\d+( +\\d+)?";

        if (owner || admin || power.isGroupList()) {
            ListAction listAction = new ListAction();
            if (Pattern.matches(addListPattern, code)) {
                l.info("添加群组指令");
                listAction.addGroupListInfo(event);
                return;
            } else if (Pattern.matches(queryListPattern, code)) {
                l.info("查询群组指令");
                listAction.queryGroupListInfo(event);
                return;
            } else if (Pattern.matches(deleteListPattern, code)) {
                l.info("删除群组指令");
                listAction.deleteGroupListInfo(event);
                return;
            }
        }

        /*
        会话正则
         */
        String addStudyPattern = "^xx( +\\S+){2,4}|^学习( +\\S+){2,4}";
        String queryStudyPattern = "^xx\\\\?[:：](\\S+)?|^查询( +\\S+)?";
        String addsStudyPattern = "^%xx|^学习对话";
        String deleteStudyPattern = "^-xx\\\\?[:：](\\S+)|^删除( +\\S+)";
        String deleteDialogueStudyPattern = "^-%xx|^删除对话";

        if (owner || admin || power.isSession() || power.isSessionX()) {
            SessionAction sessionAction = new SessionAction();
            if (Pattern.matches(addStudyPattern, code)) {
                l.info("学习会话指令");
                sessionAction.studySession(event);
                return;
            } else if (Pattern.matches(queryStudyPattern, code)) {
                l.info("查询会话指令");
                sessionAction.querySession(event);
                return;
            } else if (Pattern.matches(addsStudyPattern, code)) {
                l.info("添加会话指令");
                sessionAction.studyDialogue(event);
                return;
            } else if (Pattern.matches(deleteStudyPattern, code)) {
                l.info("删除会话指令");
                sessionAction.deleteSession(event);
                return;
            } else if (Pattern.matches(deleteDialogueStudyPattern, code)) {
                l.info("删除会话指令");
                sessionAction.deleteInformationSession(event);
                return;
            }
        }

        /*
         权限正则
         */
        String addPowerPattern = "^\\+\\[mirai:at:\\d+] +\\S+|^添加\\[mirai:at:\\d+] +\\S+";
        String deletePowerPattern = "^-\\[mirai:at:\\d+] +\\S+|^删除\\[mirai:at:\\d+] +\\S+";
        String queryPowerPattern = "^[!！]power( \\S+)?|^权限列表\\\\:( \\S+)?";
//        String queryPowerPattern = "^[!！]power(( \\S+)?|( \\[mirai:at:\\d+] )?)|^权限列表\\\\:(( \\S+)?|( \\[mirai:at:\\d+] )?)";

        if (owner || admin) {
            PowerAction powerAction = new PowerAction();
            if (Pattern.matches(addPowerPattern, code)) {
                l.info("添加权限指令");
                powerAction.addOrUpdatePower(event, true);
                return;
            } else if (Pattern.matches(deletePowerPattern, code)) {
                l.info("删除权限指令");
                powerAction.addOrUpdatePower(event, false);
                return;
            } else if (Pattern.matches(queryPowerPattern, code)) {
                l.info("查询权限指令");
                powerAction.queryPower(event);
                return;
            }
        }

        /*
        禁言正则
         */
        String groupProhibitPattern = "^\\[mirai:at:\\d+] \\d+[sdhm]";

        if ((owner || admin || power.isGroupManage() || power.isGroupJy()) && (isGroupAdmin || isGroupOwner)) {
            if (Pattern.matches(groupProhibitPattern, code)) {
                l.info("禁言指令");
                GroupManager.INSTANCE.prohibit(event);
                return;
            }
        }

        /*
        撤回正则
         */

        String groupRecallPattern = "^[!！]recall( +\\d+)?([-~]\\d+)?|^撤回( +\\d+)?(-\\d+)?";

        if ((owner || admin || power.isGroupManage() || power.isGroupCh()) && (isGroupAdmin || isGroupOwner)) {
            if (Pattern.matches(groupRecallPattern, code)) {
                l.info("撤回消息指令");
                GroupManager.INSTANCE.recall(event);
                return;
            }
        }

        /*
         * 踢人正则
         */
        String kickPattern = "^tr?\\[mirai:at:\\d+] ?(hmd)?|^踢人\\[mirai:at:\\d+] ?(hmd)?";
        if ((owner || admin || power.isGroupManage() || power.isGroupTr()) && (isGroupAdmin || isGroupOwner)) {
            if (Pattern.matches(kickPattern, code)) {
                l.info("踢人指令");
                GroupManager.kick(event);
                return;
            }
        }

        /*
         * 特殊头衔正则
         */
        String editGroupUserTitlePattern = "^%\\[mirai:at:\\d+] \\S+|^设置头衔 \\S+";
        if (owner || admin || power.isGroupManage()) {
            if (Pattern.matches(editGroupUserTitlePattern, code)) {
                l.info("设置头衔指令");
                GroupManager.INSTANCE.editUserTitle(event);
                return;
            }
        }


        /*
        违禁词正则
         */

        //+wjc:body [3h|gr1|%(重设回复消息)|ch|jy|hmd3|0|全局]
        String addProhibitedPattern = "^\\+wjc\\\\?[:：]\\S+( +\\S+)*?|^添加违禁词\\\\?[:：]\\S+( +\\S+)*?";
        String deleteProhibitedPattern = "^-wjc\\\\?[:：]\\d+|^删除违禁词\\\\?[:：]\\d+";
        String queryProhibitedPattern = "^\\wjc\\\\?[:：]|^查询违禁词";

        if (owner || admin || power.isGroupManage() || power.isGroupWjc()) {
            GroupProhibitedAction groupProhibitedAction = new GroupProhibitedAction();
            if (Pattern.matches(addProhibitedPattern, code)) {
                l.info("添加违禁词指令");
                groupProhibitedAction.addProhibited(event);
                return;
            } else if (Pattern.matches(deleteProhibitedPattern, code)) {
                l.info("删除违禁词指令");
                groupProhibitedAction.deleteProhibited(event);
                return;
            } else if (Pattern.matches(queryProhibitedPattern, code)) {
                l.info("查询违禁词指令");
                groupProhibitedAction.queryGroupProhibited(event);
                return;
            }
        }

        /*
          欢迎词正则
         */
        String addGroupWelcomeMessagePattern = "^%hyc|^添加欢迎词";
        String queryGroupWelcomeMessagePattern = "^hyc\\\\?[：:]|^查询欢迎词";
        String deleteGroupWelcomeMessagePattern = "^-hyc[:：]\\d+( +\\d+)?|^删除欢迎词\\\\?[：:]\\d+( +\\d+)?";

        if (owner || admin || power.isGroupManage() || power.isGroupHyc()) {
            GroupWelcomeInfoAction groupWelcomeInfoAction = new GroupWelcomeInfoAction();
            if (Pattern.matches(addGroupWelcomeMessagePattern, code)) {
                l.info("添加欢迎词指令");
                groupWelcomeInfoAction.addGroupWelcomeInfo(event);
                return;
            } else if (Pattern.matches(queryGroupWelcomeMessagePattern, code)) {
                l.info("查询欢迎词指令");
                groupWelcomeInfoAction.queryGroupWelcomeInfo(event);
                return;
            } else if (Pattern.matches(deleteGroupWelcomeMessagePattern, code)) {
                l.info("删除欢迎词指令");
                groupWelcomeInfoAction.deleteGroupWelcomeInfo(event);
                return;
            }
        }

        /*
        黑名单正则
         */

        String addBlackListPattern = "^\\+hmd\\\\?[:：]\\[mirai:at:\\d+]( \\S+)*?|^添加黑名单\\\\?[:：]\\[mirai:at:\\d+]( \\S+)*?";
        String queryBlackListPattern = "^hmd\\\\?[:：]|^查询黑名单";
        String deleteBlackListPattern = "^-hmd\\\\?[:：]\\d+|^删除黑名单\\\\?[:：]\\d+";

        if (owner || admin || power.isGroupManage() || power.isGroupHmd()) {
            if (Pattern.matches(addBlackListPattern, code)) {
                l.info("添加黑名单指令");
                blackListAction.addBlackList(event);
                return;
            } else if (Pattern.matches(queryBlackListPattern, code)) {
                l.info("查询黑名单指令");
                blackListAction.queryBlackList(event);
                return;
            } else if (Pattern.matches(deleteBlackListPattern, code)) {
                l.info("删除黑名单指令");
                blackListAction.deleteBlackList(event);
                return;
            }
        }

        /*
         * 多词条正则
         */
        String addManySessionPattern = "^%dct|^添加多词条";
        String queryManySessionPattern = "^dct\\\\?[:：]|^查询多词条";
        String deleteManySessionPattern = "^-dct\\\\?[:：]\\d+( +\\d+)*?|^删除多词条\\\\?[:：]\\d+( +\\d+)*?";

        if (owner || admin || power.isSession() || power.isSessionDct()) {
            ManySessionAction manySessionAction = new ManySessionAction();
            if (Pattern.matches(addManySessionPattern, code)) {
                l.info("添加多词条指令");
                manySessionAction.addManySession(event);
                return;
            } else if (Pattern.matches(queryManySessionPattern, code)) {
                l.info("查询多词条指令");
                manySessionAction.queryManySession(event);
                return;
            } else if (Pattern.matches(deleteManySessionPattern, code)) {
                l.info("删除多词条指令");
                manySessionAction.deleteManySession(event);
                return;
            }
        }

        /*
        定时器正则
         */
        String addQuartzPattern = "^%ds|^添加定时任务|^添加定时器";
        String queryQuartzPattern = "^ds\\\\?[:：]|^查询定时任务|^查询定时器";
        String deleteQuartzPattern = "^-ds\\\\?[:：]\\d+|^删除定时任务\\\\?[:：]\\d+|^删除定时器\\\\?[:：]\\d+";
        String switchQuartzPattern = "^%ds\\\\?[:：]\\S+|^切换定时任务\\\\?[:：]\\S+|^切换定时器\\\\?[:：]\\S+";

        QuartzAction quartzAction = new QuartzAction();

        if (owner || admin || power.isDs() || power.isDscz()) {
            if (Pattern.matches(switchQuartzPattern, code)) {
                l.info("切换定时器指令");
                quartzAction.switchQuartz(event);
                return;
            } else if (Pattern.matches(queryQuartzPattern, code)) {
                l.info("查询定时器指令");
                quartzAction.queryQuartz(event);
                return;
            }
        }
        if (owner || admin || power.isDs()) {
            if (Pattern.matches(addQuartzPattern, code)) {
                l.info("添加定时器指令");
                quartzAction.addQuartz(event);
                return;
            } else if (Pattern.matches(deleteQuartzPattern, code)) {
                l.info("删除定时器指令");
                quartzAction.deleteQuartz(event);
                return;
            }
        }

        /*
         数据操作正则
         */
        String outputDataPattern = "[!！]out( \\S+)?|[!！]导出数据( \\S+)?";
        String inputDataPattern = "[!！]in( \\S+)?|[!！]导入数据( \\S+)?";


        if (owner) {
            if (Pattern.matches(outputDataPattern, code)) {
                l.info("导出数据指令");
                DataManager.outputData(event);
                return;
            } else if (Pattern.matches(inputDataPattern, code)) {
                l.info("导入数据指令");
                DataManager.inputData(event);
                return;
            }
        }


        isSessionMessage(event);

    }

    /**
     * 匹配会话消息
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/7/13 21:30
     */
    private void isSessionMessage(@NotNull MessageEvent event) {
        String code = event.getMessage().serializeToMiraiCode();
        String content = event.getMessage().contentToString();
        Bot bot = event.getBot();

        DialogueProcessing instance = DialogueProcessing.getInstance();

        Map<String, Session> sessionMap = StaticData.getSessionMap(bot);
        for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
            //存在则尝试匹配作用域
            Session sessionInfo = entry.getValue();
            if (ShareUtils.mateScope(event, sessionInfo.getScope())) {
                if (SessionConfig.INSTANCE.getDebugSwitch()) {
                    l.info("匹配作用域->存在");
                }
                //尝试匹配匹配方式
                if (ShareUtils.mateMate(code, sessionInfo.getMate(), sessionInfo.getTerm(), content)) {
                    if (SessionConfig.INSTANCE.getDebugSwitch()) {
                        l.info("匹配匹配方式->成功");
                    }
                    instance.dialogue(event,sessionInfo);
//                    DialogueImpl.INSTANCE.dialogueSession(event, sessionInfo);
                    return;
                }
            }
        }

        Map<String, ManySessionInfo> manySession = StaticData.getManySession(bot);
        for (Map.Entry<String, ManySessionInfo> entry : manySession.entrySet()) {
            //先做模糊查询判断存在不存在
            if (code.contains(entry.getKey())) {
                if (SessionConfig.INSTANCE.getDebugSwitch()) {
                    l.info("匹配触发内容->存在");
                }
                //存在则尝试匹配作用域
                ManySessionInfo manySessionInfo = entry.getValue();
                if (ShareUtils.mateScope(event, manySessionInfo.getScope())) {
                    if (SessionConfig.INSTANCE.getDebugSwitch()) {
                        l.info("匹配作用域->存在");
                    }
                    //尝试匹配匹配方式
                    if (ShareUtils.mateMate(code, manySessionInfo.getMate(), manySessionInfo.getKeywords(), content)) {
                        if (SessionConfig.INSTANCE.getDebugSwitch()) {
                            l.info("匹配匹配方式->成功");
                        }
//                        todo
//                        instance.dialogue(event,manySessionInfo);
                        DialogueImpl.INSTANCE.dialogueSession(event, manySessionInfo);
                        return;
                    }
                }
            }
        }

    }


}
