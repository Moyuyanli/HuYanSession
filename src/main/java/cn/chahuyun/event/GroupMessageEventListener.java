package cn.chahuyun.event;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.data.StaticData;
import cn.chahuyun.dialogue.Dialogue;
import cn.chahuyun.entity.*;
import cn.chahuyun.enums.Mate;
import cn.chahuyun.files.ConfigData;
import cn.chahuyun.manage.GroupManager;
import cn.chahuyun.utils.ListUtil;
import cn.chahuyun.utils.PowerUtil;
import cn.chahuyun.utils.SessionUtil;
import cn.chahuyun.utils.ShareUtils;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.BotIsBeingMutedException;
import net.mamoe.mirai.contact.Contact;
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
 * @Description :群消息检测
 * @Date 2022/7/9 18:11
 */
public class GroupMessageEventListener extends SimpleListenerHost {

    private static final MiraiLogger l = HuYanSession.INSTANCE.getLogger();

    @Override
    public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception){
        if (exception instanceof BotIsBeingMutedException) {
            l.warning("机器人已被禁言");
        }

        // 处理事件处理时抛出的异常
        l.error("插件异常:",exception);
    }


    @EventHandler
    public void onMessage(@NotNull MessageEvent event) throws Exception { // 可以抛出任何异常, 将在 handleException 处理
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        User sender = event.getSender();
        Bot bot = event.getBot();

        //关闭多机器人自触发
        List<Bot> instances = Bot.getInstances();
        for (Bot instance : instances) {
            if (instance.getId() == sender.getId()) {
                return;
            }
        }

        if (ConfigData.INSTANCE.getDebugSwitch()) {
            l.info("MiraiCode-> "+code);
        }

        //主人
        boolean owner = ConfigData.INSTANCE.getOwner() == sender.getId();

        //权限用户识别符
        String powerString = subject.getId() + "." + sender.getId();

        Map<String, Power> powerMap = StaticData.getPowerMap(bot);
        if (ConfigData.INSTANCE.getDebugSwitch()) {
            l.info("owner-"+owner+" power-"+powerMap.containsKey(powerString));
        }
        if (!owner && !powerMap.containsKey(powerString)) {
            isSessionMessage(event);
            return;
        }

        /*
        特殊匹配
         */

        String pauseEventPattern = "^[!！]pause +\\[mirai:at:\\d+]( +\\d+)?";

        //是否忽略下一条消息
        if (ShareUtils.isPause(event)) {
            if (ConfigData.INSTANCE.getDebugSwitch()) {
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
        用户权限
         */
        Power power = powerMap.get(powerString);

        /*
        群组正则
         */
        String addListPattern = "^\\+?gr\\\\?[:：]\\d+( +\\d+)+|^添加群组\\\\?[:：]\\d+( +\\d+)+";
        String queryListPattern = "^gr\\\\?[:：](\\d+)?|^查询群组\\\\?[:：](\\d+)?";
        String deleteListPattern = "^-gr\\\\?[：:]\\d+( +\\d+)?|^删除群组\\\\?[:：]\\d+( +\\d+)?";

        if (owner || power.isGroupList()) {
            if (Pattern.matches(addListPattern, code)) {
                l.info("添加群组指令");
                ListUtil.addGroupListInfo(event);
                return;
            } else if (Pattern.matches(queryListPattern, code)) {
                l.info("查询群组指令");
                ListUtil.queryGroupListInfo(event);
                return;
            } else if (Pattern.matches(deleteListPattern, code)) {
                l.info("删除群组指令");
                ListUtil.deleteGroupListInfo(event);
                return;
            }

        }

        /*
        会话正则
         */
        String addStudyPattern = "^xx +\\S+ +\\S+( +\\S+){0,2}|^学习 +\\S+( +\\S+){0,2}";
        String queryStudyPattern = "^xx\\\\?[:：](\\S+)?|^查询( +\\S+)?";
        String addsStudyPattern = "^%xx|^学习对话";
        String deleteStudyPattern = "^-xx\\\\?[:：](\\S+)|^删除( +\\S+)";

        if (owner || power.isSession() || power.isSessionX()) {
            if (Pattern.matches(addStudyPattern, code)) {
                l.info("学习会话指令");
                SessionUtil.studySession(event);
                return;
            } else if (Pattern.matches(queryStudyPattern, code)) {
                l.info("查询会话指令");
                SessionUtil.querySession(event);
                return;
            } else if (Pattern.matches(addsStudyPattern, code)) {
                l.info("添加会话指令");
                SessionUtil.studyDialogue(event);
                return;
            } else if (Pattern.matches(deleteStudyPattern, code)) {
                l.info("删除会话指令");
                SessionUtil.deleteSession(event);
                return;
            }
        }

        /*
         权限正则
         */
        String addPowerPattern = "^\\+\\[mirai:at:\\d+] +\\S+|^添加\\[mirai:at:\\d+] +\\S+";
        String deletePowerPattern = "^\\-\\[mirai:at:\\d+] +\\S+|^删除\\[mirai:at:\\d+] +\\S+";
        String queryPowerPattern = "^[!！]power( \\S+)?|^权限列表\\\\:( \\S+)?";

        if (owner || power.isAdmin()) {
            if (Pattern.matches(addPowerPattern, code)) {
                l.info("添加权限指令");
                PowerUtil.addOrUpdatePower(event, true);
                return;
            } else if (Pattern.matches(deletePowerPattern, code)) {
                l.info("删除权限指令");
                PowerUtil.addOrUpdatePower(event, false);
                return;
            } else if (Pattern.matches(queryPowerPattern, code)) {
                l.info("查询权限指令");
                PowerUtil.inquirePower(event);
                return;
            }
        }

        /*
        禁言正则
         */
        String groupProhibitPattern = "\\[mirai:at:\\d+\\] \\d+[s|d|h|m]";

        if (owner || power.isGroupManage() || power.isGroupJy() ) {
            if (Pattern.matches(groupProhibitPattern, code)) {
                l.info("禁言指令");
                GroupManager.prohibit(event);
                return;
            }
        }

        /*
        撤回正则
         */

        String groupRecallPattern = "^[!！]recall( +\\d+)?(-\\d+)?|^撤回( +\\d+)?(-\\d+)?";

        if (owner || power.isGroupManage() || power.isGroupCh()) {
            if (Pattern.matches(groupRecallPattern, code)) {
                l.info("撤回消息指令");
                GroupManager.recall(event);
                return;
            }
        }


        isSessionMessage(event);

    }

    /**
     * 匹配会话消息
     * @author Moyuyanli
     * @param event 消息事件
     * @date 2022/7/13 21:30
     */
    private void isSessionMessage(MessageEvent event) {
        String code = event.getMessage().serializeToMiraiCode();
        Bot bot = event.getBot();

        Map<String, Session> sessionMap = StaticData.getSessionMap(bot);
        for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
            if (ConfigData.INSTANCE.getDebugSwitch()) {
                l.info("Session-> "+entry.getKey());
            }
            //先做模糊查询判断存在不存在
            if (code.contains(entry.getKey())) {
                if (ConfigData.INSTANCE.getDebugSwitch()) {
                    l.info("匹配触发内容->存在");
                }
                //存在则尝试匹配作用域
                Session session = entry.getValue();
                if (mateScope(event, session.getScopeInfo())) {
                    if (ConfigData.INSTANCE.getDebugSwitch()) {
                        l.info("匹配作用域->存在");
                    }
                    //尝试匹配匹配方式
                    if (mateMate(code, session.getMate(), session.getTerm())) {
                        if (ConfigData.INSTANCE.getDebugSwitch()) {
                            l.info("匹配匹配方式->成功");
                        }
                        Dialogue.INSTANCE.dialogueSession(event, session);
                    }
                }
            }
        }
    }

    /**
     * 匹配作用域
     * @author Moyuyanli
     * @param event 消息事件
     * @param scope 作用域
     * @date 2022/7/13 21:34
     * @return boolean true 匹配成功! false 匹配失败！
     */
    private boolean mateScope(MessageEvent event, Scope scope) {
        Bot bot = event.getBot();
        long group = event.getSubject().getId();

        Map<Integer, GroupList> groupListMap = StaticData.getGroupListMap(bot);

        if (scope.getGroupInfo()) {
            GroupList groupList = groupListMap.get(scope.getListId());
            List<GroupInfo> groupNumbers = groupList.getGroups();
            for (GroupInfo aLong : groupNumbers) {
                if (group == aLong.getGroupId()) {
                    return true;
                }
            }
        } else if (scope.getGlobal()) {
            return true;
        } else {
            long l = scope.getGroupNumber();
            return l == group;
        }
        return false;
    }

    /**
     * 匹配匹配方式
     * @author Moyuyanli
     * @param code 消息
     * @param mate 匹配方式
     * @param key 匹配内容
     * @date 2022/7/13 21:40
     * @return boolean true 匹配成功! false 匹配失败！
     */
    private boolean mateMate(String code, Mate mate,String key) {
        switch (mate) {
            case ACCURATE:
                if (code.equals(key)) {
                    return true;
                }
                break;
            case VAGUE:
                if (code.contains(key)) {
                    return true;
                }
                break;
            case START:
                if (code.startsWith(key)) {
                    return true;
                }
                break;
            case END:
                if (code.endsWith(key)) {
                    return true;
                }
                break;
            default:break;
        }
        return false;
    }


}
