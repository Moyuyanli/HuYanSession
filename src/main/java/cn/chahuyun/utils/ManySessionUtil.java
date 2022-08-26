package cn.chahuyun.utils;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.config.ConfigData;
import cn.chahuyun.data.StaticData;
import cn.chahuyun.entity.ManySession;
import cn.chahuyun.entity.ManySessionInfo;
import cn.chahuyun.entity.Scope;
import cn.chahuyun.enums.Mate;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.Audio;
import net.mamoe.mirai.message.data.ForwardMessage;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.utils.MiraiLogger;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static cn.chahuyun.utils.ShareUtils.DYNAMIC_MESSAGE_PATTERN;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :多词条消息工具
 * @Date 2022/8/26 21:19
 */
public class ManySessionUtil {

    private final static MiraiLogger l = HuYanSession.INSTANCE.getLogger();

    /**
     * 加载多词条消息到内存
     *
     * @param type true 初次加载 false 更新
     * @author Moyuyanli
     * @date 2022/8/26 21:24
     */
    public static void init(boolean type) {

        List<ManySessionInfo> manySessionInfos = null;
        try {
            manySessionInfos = HibernateUtil.factory.fromTransaction(session -> {
                HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
                JpaCriteriaQuery<ManySessionInfo> query = builder.createQuery(ManySessionInfo.class);
                JpaRoot<ManySessionInfo> from = query.from(ManySessionInfo.class);
                query.select(from);
                return session.createQuery(query).list();
            });
        } catch (Exception e) {
            if (type) {
                l.warning("多词条加载消息出错!", e);
                return;
            }
        }

        Map<Long, Map<String, ManySessionInfo>> map = null;
        for (ManySessionInfo manySessionInfo : manySessionInfos) {
            if (map == null) {
                map = new HashMap<>() {{
                    put(manySessionInfo.getBot(), new HashMap<>() {{
                        put(manySessionInfo.getTrigger(), manySessionInfo);
                    }});
                }};
            }
            if (map.containsKey(manySessionInfo.getBot())) {
                map.get(manySessionInfo.getBot()).put(manySessionInfo.getTrigger(), manySessionInfo);
            }
        }
        StaticData.setManySession(map);

        if (type) {
            l.info("多词条消息初始化成功!");
        }
        if (ConfigData.INSTANCE.getDebugSwitch()) {
            l.info("多词条数据更新成功!");
        }
    }

    /**
     * 添加多词条消息
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/8/26 21:55
     */
    public static void addManySession(MessageEvent event) {
        Bot bot = event.getBot();
        Contact subject = event.getSubject();
        User user = event.getSender();

        subject.sendMessage("请发送多词条的触发词：");
        MessageEvent triggerEvent = ShareUtils.getNextMessageEventFromUser(user);
        if (ShareUtils.isQuit(triggerEvent)) {
            return;
        }
        String code = triggerEvent.getMessage().serializeToMiraiCode();
        subject.sendMessage("请发送参数(中间以空格隔开)");
        MessageEvent paramEvent = ShareUtils.getNextMessageEventFromUser(user);
        if (ShareUtils.isQuit(paramEvent)) {
            return;
        }
        String[] split = paramEvent.getMessage().serializeToMiraiCode().split(" +");

        Scope scope = new Scope(bot.getId(), "当前", false, false, subject.getId(), -1);
        Mate mate = Mate.ACCURATE;
        boolean isRandom = false;


        for (String param : split) {
            switch (param) {
                case "0":
                case "全局":
                    scope = new Scope(bot.getId(), "全局", true, false, subject.getId(), -1);
                    break;
                case "2":
                case "模糊":
                    mate = Mate.VAGUE;
                    break;
                case "3":
                case "头部":
                    mate = Mate.START;
                    break;
                case "4":
                case "结尾":
                    mate = Mate.END;
                    break;
                case "sj":
                case "随机":
                    isRandom = true;
                    break;
                default:
                    String listPattern = "gr\\d+|群组\\d+";
                    if (Pattern.matches(listPattern, param)) {
                        int listId = Integer.parseInt(param.substring(2));
                        if (ListUtil.isContainsList(bot, listId)) {
                            subject.sendMessage("该群组不存在!");
                            return;
                        }
                        scope = new Scope(bot.getId(), "群组" + listId, false, true, subject.getId(), listId);
                    }
                    break;
            }
        }

        ManySessionInfo manySessionInfo = new ManySessionInfo(bot.getId(), isRandom, 0, code, mate.getMateType(), scope);
        List<ManySession> sessionList = manySessionInfo.getManySessions();

        boolean isQuit = false;
        while (!isQuit) {
            subject.sendMessage("请发送多词条回复消息:");
            MessageEvent nextEvent = ShareUtils.getNextMessageEventFromUser(user);
            if (ShareUtils.isQuit(nextEvent)) {
                isQuit = true;
            }
            MessageChain nextEventMessage = nextEvent.getMessage();
            String miraiCode = nextEventMessage.serializeToMiraiCode();
            if (miraiCode.contains("！") || miraiCode.contains("!")) {
                if (sessionList.size() > 2) {
                    sessionList.remove(sessionList.size() - 1);
                    subject.sendMessage("删除上一条回复消息成功！");
                    continue;
                }
            }
            //判断是否存在动态消息
            boolean dynamic = false;
            Pattern compile = Pattern.compile(DYNAMIC_MESSAGE_PATTERN);
            if (compile.matcher(miraiCode).find() || compile.matcher(miraiCode).find()) {
                dynamic = true;
            }

            //判断消息是否是转发消息或音频消息
            boolean other = false;
            if (nextEventMessage.contains(ForwardMessage.Key) || nextEventMessage.contains(Audio.Key)) {
                dynamic = false;
                other = true;
                miraiCode = MessageChain.serializeToJsonString(nextEventMessage);
            }
            ManySession manySession = new ManySession(bot.getId(), code, dynamic, other, miraiCode);
            sessionList.add(manySession);
            subject.sendMessage("添加成功!");
        }

        Scope finalScope = scope;
        try {
            HibernateUtil.factory.fromTransaction(session -> {
                if (!ScopeUtil.isScopeEmpty(finalScope)) {
                    //不存在则先添加作用域
                    session.persist(finalScope);
                }
                session.merge(manySessionInfo);
                return 0;
            });
        } catch (Exception e) {
            subject.sendMessage("多词条保存失败!");
            l.error("出错啦~", e);
            return;
        }
        subject.sendMessage("多词条保存成功!");
    }

    public static void editManySession(MessageEvent event) {

    }

    public static void queryManySession(MessageEvent event) {

    }

    public static void deleteManySession(MessageEvent event) {

    }

}
