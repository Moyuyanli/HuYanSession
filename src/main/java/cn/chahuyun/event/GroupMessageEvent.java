package cn.chahuyun.event;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.data.StaticData;
import cn.chahuyun.dialogue.Dialogue;
import cn.chahuyun.entity.Group;
import cn.chahuyun.entity.GroupList;
import cn.chahuyun.entity.Scope;
import cn.chahuyun.entity.Session;
import cn.chahuyun.enums.Mate;
import cn.chahuyun.files.ConfigData;
import cn.chahuyun.utils.ListUtil;
import cn.chahuyun.utils.SessionUtil;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.utils.MiraiLogger;
import org.jetbrains.annotations.NotNull;

import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static cn.chahuyun.enums.Mate.*;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :群消息检测
 * @Date 2022/7/9 18:11
 */
public class GroupMessageEvent extends SimpleListenerHost {

    private static final MiraiLogger l = HuYanSession.INSTANCE.getLogger();

    @Override
    public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception){
        // 处理事件处理时抛出的异常
        l.error("插件异常:",exception);
    }


    @EventHandler
    public void onMessage(@NotNull MessageEvent event) throws Exception { // 可以抛出任何异常, 将在 handleException 处理
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        Bot bot = event.getBot();


        if (ConfigData.INSTANCE.getDebugSwitch()) {
            l.info("MiraiCode-> "+code);
//            l.info("MiraiJson-> "+ MessageChain.serializeToJsonString(event.getMessage()));
        }

        /*
        群组正则
         */
        String addListPattern = "^\\+?gr\\\\?[:：]\\d+( +\\d+)+|^添加群组\\\\?[:：]\\d+( +\\d+)+";
        String queryListPattern = "^gr\\\\?[:：](\\d+)?|^查询群组\\\\?[:：](\\d+)?";
        String deleteListPattern = "^-gr\\\\?[：:]\\d+( +\\d+)?|^删除群组\\\\?[:：]\\d+( +\\d+)?";

        /*
        会话正则
         */
        String addStudyPattern = "^xx +\\S+ +\\S+( +\\S+){0,2}|^学习 +\\S+( +\\S+){0,2}";
        String queryStudyPattern = "^xx\\\\?[:：](\\S+)?|^查询( +\\S+)?";
        String addsStudyPattern = "^%xx|^学习对话";
        String deleteStudyPattern = "^-xx\\\\?[:：](\\S+)|^删除( +\\S+)";


        if (Pattern.matches(addListPattern, code)) {
            l.info("添加群组指令");
            ListUtil.addList(event);
        } else if (Pattern.matches(queryListPattern, code)) {
            l.info("查询群组指令");
            ListUtil.queryList(event);
        } else if (Pattern.matches(deleteListPattern, code)) {
            l.info("删除群组指令");
            ListUtil.deleteList(event);
        }

        if (Pattern.matches(addStudyPattern, code)) {
            l.info("学习会话指令");
            SessionUtil.studySession(event);
        } else if (Pattern.matches(queryStudyPattern, code)) {
            l.info("查询会话指令");
            SessionUtil.querySession(event);
        } else if (Pattern.matches(addsStudyPattern, code)) {
            l.info("添加会话指令");
            SessionUtil.studyDialogue(event);
        } else if (Pattern.matches(deleteStudyPattern, code)) {
            l.info("删除会话指令");
            SessionUtil.deleteSession(event);
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
        long bot = event.getBot().getId();

        Map<String, Session> sessionMap = StaticData.getSessionMap(bot);
        for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
            if (ConfigData.INSTANCE.getDebugSwitch()) {
                l.info("Session-> "+entry.getKey());
            }
            //先做模糊查询判断存在不存在
            if (code.contains(entry.getKey())) {
                if (ConfigData.INSTANCE.getDebugSwitch()) {
                    l.info("匹配->存在");
                }
                //存在则尝试匹配作用域
                Session session = entry.getValue();
                if (mateScope(event, session.getScope())) {
                    if (ConfigData.INSTANCE.getDebugSwitch()) {
                        l.info("匹配作用域->存在");
                    }
                    //尝试匹配匹配方式
                    if (mateMate(code, session.getMate(), session.getKey())) {
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
        long bot = event.getBot().getId();
        long group = event.getSubject().getId();

        Map<Integer, GroupList> groupListMap = StaticData.getGroupListMap(bot);

        if (scope.isGroup()) {
            GroupList groupList = groupListMap.get(scope.getListId());
            List<Group> groups = groupList.getGroups();
            for (Group aLong : groups) {
                if (group == aLong.getGroup()) {
                    return true;
                }
            }
        } else if (scope.isGlobal()) {
            return true;
        } else {
            long l = scope.getGroup();
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
