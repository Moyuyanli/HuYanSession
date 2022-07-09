package cn.chahuyun.event;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.utils.ListUtil;
import cn.chahuyun.utils.SessionUtil;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.utils.MiraiLogger;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

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
    }


    @EventHandler
    public void onMessage(@NotNull MessageEvent event) throws Exception { // 可以抛出任何异常, 将在 handleException 处理
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        Bot bot = event.getBot();

        String addListPattern = "\\+?gr\\\\?[:：]\\d+( +\\d+)+|添加群组\\\\?[:：]\\d+( +\\d+)+";
        String queryListPattern = "gr\\\\?[:：](\\d+)?|查询群组\\\\?[:：](\\d+)?";
        String addStudyPattern = "xx +\\S+ +\\S+( +\\S+){0,2}|学习 +\\S+( +\\S+){0,2}";


        if (Pattern.matches(addListPattern, code)) {
            l.info("添加群组指令");
            ListUtil.addList(event);
        } else if (Pattern.matches(queryListPattern, code)) {
            l.info("查询群组指令");
            ListUtil.queryList(event);
        }

        if (Pattern.matches(addStudyPattern, code)) {
            SessionUtil.studySession(event);
        }






    }


}