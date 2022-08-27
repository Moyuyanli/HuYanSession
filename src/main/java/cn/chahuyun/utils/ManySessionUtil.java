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
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.MiraiLogger;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        assert manySessionInfos != null;
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

        boolean editOrAdd = StaticData.getManySession(bot).containsKey(code);
        ManySessionInfo manySessionInfo = null;
        Scope scope;
        Mate mate;
        boolean isRandom;
        if (editOrAdd) {
            manySessionInfo = StaticData.getManySession(bot).get(code);
            scope = manySessionInfo.getScope();
            mate = manySessionInfo.getMate();
            isRandom = manySessionInfo.isRandom();
        } else {
            scope = new Scope(bot.getId(), "当前", false, false, subject.getId(), -1);
            mate = Mate.ACCURATE;
            isRandom = false;
        }
        subject.sendMessage("请发送参数(中间以空格隔开)");
        MessageEvent paramEvent = ShareUtils.getNextMessageEventFromUser(user);
        if (ShareUtils.isQuit(paramEvent)) {
            return;
        }
        String[] split = paramEvent.getMessage().serializeToMiraiCode().split(" +");

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
            if (editOrAdd) {
                manySessionInfo.setScope(scope);
                manySessionInfo.setMate(mate);
            } else {
                manySessionInfo = new ManySessionInfo(bot.getId(), isRandom, 0, code, mate.getMateType(), scope);
            }
        }
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
            if (miraiCode.equals("！") || miraiCode.equals("!")) {
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
        if (editOrAdd) {
            scope = manySessionInfo.getScope();
        }
        try {
            Scope finalScope = scope;
            ManySessionInfo finalManySessionInfo = manySessionInfo;
            HibernateUtil.factory.fromTransaction(session -> {
                if (!ScopeUtil.isScopeEmpty(finalScope)) {
                    //不存在则先添加作用域
                    session.persist(finalScope);
                }
                session.merge(finalManySessionInfo);
                return 0;
            });
        } catch (Exception e) {
            subject.sendMessage("多词条保存失败!");
            l.error("出错啦~", e);
            return;
        }
        subject.sendMessage("多词条保存成功!");
        init(false);
    }


    /**
     * 查询多词条
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/8/26 18:33
     */
    public static void queryManySession(MessageEvent event) {
        MessageChain message = event.getMessage();
        Contact subject = event.getSubject();
        Bot bot = event.getBot();

        init(false);

        Map<String, ManySessionInfo> manySession = StaticData.getManySession(bot);
        if (manySession == null || manySession.isEmpty()) {
            subject.sendMessage("没有多词条消息!");
            return;
        }

        ForwardMessageBuilder builder = new ForwardMessageBuilder(subject);
        builder.add(bot, new PlainText("以下是所有多词条消息↓"));

        for (ManySessionInfo value : manySession.values()) {
            List<ManySession> manySessions = value.getManySessions();
            PlainText plainText = new PlainText(String.format("多词条编号:%d%n触发方式:%s%n触发内容:%s%n", value.getId(), value.getMate().getMateName(), value.getTrigger()));
            plainText.plus(String.format("作用域:%s%n", value.getScope().getScopeName()));
            plainText.plus(String.format("当前群是否触发:%b", ShareUtils.mateScope(event, value.getScope()) ? "是" : "否"));
            builder.add(bot, plainText);
            ForwardMessageBuilder messageBuilder = new ForwardMessageBuilder(subject);
            for (ManySession session : manySessions) {
                if (session.isOther()) {
                    messageBuilder.add(bot, new PlainText(String.format("编号:%s", session.getId())));
                    MessageChain singleMessages = MessageChain.deserializeFromJsonString(session.getReply());
                    messageBuilder.add(bot, singleMessages);
                    continue;
                }
                MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
                messageChainBuilder.add(String.format("编号:%s%n", session.getId()));
                messageChainBuilder.add(MiraiCode.deserializeMiraiCode(session.getReply()));
                messageBuilder.add(bot, messageChainBuilder.build());
            }
        }
        subject.sendMessage(builder.build());
    }

    /**
     * 删除多词条，支持一次性删除多条
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/8/27 1:11
     */
    public static void deleteManySession(MessageEvent event) {
        //-hyc:id id?
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        Bot bot = event.getBot();

        init(false);

        String[] split = code.split("[：:]")[1].split(" +");
        int keyId = Integer.parseInt(split[0]);

        Map<String, ManySessionInfo> manySession = StaticData.getManySession(bot);
        ManySessionInfo manySessionInfo = null;
        for (ManySessionInfo value : manySession.values()) {
            if (value.getId() == keyId) {
                manySessionInfo = value;
            }
        }
        if (manySessionInfo == null) {
            subject.sendMessage("沒有找到对应的多词条!");
            return;
        }
        boolean deleteType = split.length > 1;
        List<ManySession> manySessions = manySessionInfo.getManySessions();
        List<ManySession> manySessionList = manySessionInfo.getManySessions();

        if (split.length > 1) {
            for (String value : split) {
                int s = 0;
                try {
                    s = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    l.warning("删除多词条-id中含有不是数值的字符!");
                }
                int finalS = s;
                manySessionList = manySessionList.stream().filter(it -> it.getId() != finalS).collect(Collectors.toList());
            }
        }

        boolean deleteMessageType = manySessions.size() > manySessionList.size();

        ManySessionInfo finalManySessionInfo = manySessionInfo;
        List<ManySession> finalManySessionList = manySessionList;
        HibernateUtil.factory.fromTransaction(session -> {
            if (deleteType) {
                session.remove(finalManySessionInfo);
            } else if (deleteMessageType) {
                finalManySessionInfo.setManySessions(finalManySessionList);
                session.merge(finalManySessionInfo);
            }
            return 0;
        });

        if (deleteType) {
            subject.sendMessage(String.format("删除多词条 %s 成功！", manySessionInfo.getTrigger()));
        } else if (deleteMessageType) {
            subject.sendMessage(String.format("删除多词条 %s 下的回复成功!", manySessionInfo.getTrigger()));
        } else {
            subject.sendMessage("没有找到要删除的多词条回复！");
        }
        init(false);
    }

    /**
     * 轮询次数递增
     *
     * @param sessionInfo 多词条消息
     * @author Moyuyanli
     * @date 2022/8/22 16:32
     */
    public static void increase(ManySessionInfo sessionInfo) {
        sessionInfo.setPollingNumber(sessionInfo.getPollingNumber() + 1);
        HibernateUtil.factory.fromTransaction(session -> session.merge(sessionInfo));
    }

}
