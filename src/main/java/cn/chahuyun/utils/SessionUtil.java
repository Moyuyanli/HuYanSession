package cn.chahuyun.utils;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.data.StaticData;
import cn.chahuyun.entity.Scope;
import cn.chahuyun.entity.Session;
import cn.chahuyun.enums.Mate;
import cn.chahuyun.files.ConfigData;
import cn.hutool.db.Entity;
import kotlin.coroutines.EmptyCoroutineContext;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.ConcurrencyKind;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.EventPriority;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.ForwardMessage;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.utils.MiraiLogger;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :对话消息工具类
 * @Date 2022/7/9 17:14
 */
public class SessionUtil {

    private final static MiraiLogger l = HuYanSession.INSTANCE.getLogger();
    private static MessageEvent event;

    /**
     * 刷新静态内存数据
     *
     * @param type true 初始化加载 false 刷新
     * @author Moyuyanli
     * @date 2022/7/29 22:25
     */
    public static void initHuTool(boolean type) {

        String querySessionSql =
                "SELECT " +
                        "s.bot," +
                        "s.type," +
                        "s.key," +
                        "s.value," +
                        "s.mate_id 'mateId'," +
                        "se.scope_name 'scopeName'," +
                        "se.is_group 'isGroup'," +
                        "se.is_global 'isGlobal'," +
                        "se.`group` 'group'," +
                        "se.list_id 'listId'" +
                        "FROM " +
                        "session 's'" +
                        "LEFT JOIN " +
                        "scope se ON s.scope_id = se.id " +
                        "WHERE " +
                        "s.bot = se.bot;";
        List<Entity> entities;
        try {
            entities = HuToolUtil.db.query(querySessionSql);
        } catch (SQLException e) {
            l.error("会话数据加载失败:" + e.getMessage());
            e.printStackTrace();
            return;
        }

        Map<Long, Map<String, Session>> sessionAll = new HashMap<>();

        if (entities != null && entities.size() != 0) {
            for (Entity entity : entities) {
                Integer mateId = entity.getInt("mateId");
                Mate mate = Mate.ACCURATE;
                switch (mateId) {
                    case 2:
                        mate = Mate.VAGUE;
                        break;
                    case 3:
                        mate = Mate.START;
                        break;
                    case 4:
                        mate = Mate.END;
                        break;
                    default:
                        break;
                }
                Session session;
                Scope scope;
                try {
                    scope = BeanUtil.parseEntity(entity, Scope.class);
                    session = BeanUtil.parseEntity(entity, Session.class);
                } catch (Exception e) {
                    l.error("会话数据初始化失败:" + e.getMessage());
                    e.printStackTrace();
                    return;
                }
                session.setMate(mate);
                session.setScopeInfo(scope);
                if (!sessionAll.containsKey(session.getBot())) {
                    sessionAll.put(session.getBot(), new HashMap<String, Session>() {{
                        put(session.getKey(), session);
                    }});
                    continue;
                }
                Map<String, Session> sessionMap = sessionAll.get(session.getBot());
                if (!sessionMap.containsKey(session.getKey())) {
                    sessionAll.get(session.getBot()).put(session.getKey(), session);
                }
            }
            StaticData.setSessionMap(sessionAll);
        }
        if (type) {
            l.info("数据库会话数据初始化成功!");
            return;
        }
        if (ConfigData.INSTANCE.getDebugSwitch()) {
            l.info("会话数据更新成功!");
        }

    }

    public static void init(boolean type) {
        List<Session> sessions;
        try {
            sessions = HibernateUtil.factory.fromTransaction(session -> {
                HibernateCriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
                JpaCriteriaQuery<Session> query = criteriaBuilder.createQuery(Session.class);
                JpaRoot<Session> from = query.from(Session.class);
                query.select(from);
                return session.createQuery(query).list();
            });
        } catch (Exception e) {
            l.error("会话数据加载失败:" + e.getMessage());
            e.printStackTrace();
            return;
        }

        Map<Long, Map<String, Session>> sessionAll = new HashMap<>();

        if (sessions != null && !sessions.isEmpty()) {
            for (Session entity : sessions) {
                if (!sessionAll.containsKey(entity.getBot())) {
                    sessionAll.put(entity.getBot(), new HashMap<String, Session>() {{
                        put(entity.getKey(), entity);
                    }});
                    continue;
                }
                Map<String, Session> sessionMap = sessionAll.get(entity.getBot());
                if (!sessionMap.containsKey(entity.getKey())) {
                    sessionAll.get(entity.getBot()).put(entity.getKey(), entity);
                }
            }
            StaticData.setSessionMap(sessionAll);
        }
        if (type) {
            l.info("数据库会话数据初始化成功!");
            return;
        }
        if (ConfigData.INSTANCE.getDebugSwitch()) {
            l.info("会话数据更新成功!");
        }

    }
    

    /**
     * 学习会话
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/7/29 22:27
     */
    public static void studySession(MessageEvent event) {
        //xx a b [p1] [p2]
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        Bot bot = event.getBot();

        String[] split = code.split("\\s+");
        int type = 0;
        String key = split[1];
        String value = split[2];

        if (StaticData.isSessionKey(bot, key)) {
            subject.sendMessage("我已经学废了" + key + "!不能再学了!");
            return;
        }

        String typePattern = "\\[mirai:image\\S+]";
        if (Pattern.matches(typePattern, key) || Pattern.matches(typePattern, value)) {
            type = 1;
        }

        Mate mate = Mate.ACCURATE;
        Scope scope = new Scope(bot.getId(), "当前", false, false, subject.getId(), -1);

        //最小分割大小
        int minIndex = 3;
        //大于这个大小就进行参数判断
        if (split.length > minIndex) {
            for (int i = minIndex; i < split.length; i++) {
                String s = split[i];
                switch (s) {
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
                    case "0":
                    case "全局":
                        scope = new Scope(bot.getId(), "全局", true, false, subject.getId(), -1);
                        break;
                    default:
                        String listPattern = "gr\\d+|群组\\d+";
                        if (Pattern.matches(listPattern, s)) {
                            int listId = Integer.parseInt(s.substring(2));
                            if (!ListUtil.isContainsList(bot, listId)) {
                                subject.sendMessage("该群组不存在!");
                                return;
                            }
                            scope = new Scope(bot.getId(), "群组", false, true, subject.getId(), listId);
                        }
                        break;
                }
            }
        }
        if (subject instanceof User && !scope.getGlobal() && scope.getGroupInfo()) {
            subject.sendMessage("私发学习请输入作用域！");
            return;
        }

        int scope_id = ScopeUtil.getScopeId(bot, scope);
        if (scope_id == -1) {
            subject.sendMessage("学不废!");
            l.warning("学习添加失败,无作用域!");
            return;
        }

        saveSession(subject, bot, key, value, mate, scope_id, type);
    }

    /**
     * 查询所有会话消息
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/7/13 21:21
     */
    public static void querySession(MessageEvent event) {
        //xx:key?
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        Bot bot = event.getBot();

        String querySessionPattern = "xx\\\\?[:：](\\S+)|查询 +\\S+";

        boolean type = Pattern.matches(querySessionPattern, code);
        String key;
        if (type) {
            String[] split = code.split("[:：]| +");
            key = split[1];
            Map<String, Session> sessionMap;
            try {
                init(false);
                sessionMap = StaticData.getSessionMap(bot);
            } catch (Exception e) {
                subject.sendMessage("查询会话消息为空!");
                return;
            }
            if (sessionMap == null) {
                subject.sendMessage("我不太会讲发~!");
                return;
            }
            if (sessionMap.containsKey(key)) {
                Session session = sessionMap.get(key);
                //判断触发类别

                String trigger = judgeScope(event, subject, session);
                subject.sendMessage("查询到对应会话:\n" +
                        session.getKey() + "==>" + session.getValue() + "\n" +
                        "匹配方式:" + session.getMate().getMateName() + "\n" +
                        "触发范围:" + trigger);
                return;
            }
            subject.sendMessage("我不是很会讲发~");
            return;
        }

        ForwardMessage forwardMessage = parseMessage(event);
        if (forwardMessage != null) {
            subject.sendMessage(forwardMessage);
        }

    }

    /**
     * 通过发送消息的方式进行添加会话
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/7/29 15:05
     */
    public static void studyDialogue(MessageEvent event) throws ExecutionException, InterruptedException {
        //%xx|学习对话
        Contact subject = event.getSubject();
        User user = event.getSender();
        Bot bot = event.getBot();

        subject.sendMessage("开始添加对话，请输入触发类容:");
        event.intercept();
        MessageEvent nextMessageEventFromUser = getNextMessageEventFromUser(user);
        if (ShareUtils.isQuit(nextMessageEventFromUser)) {
            return;
        }
        String key = nextMessageEventFromUser.getMessage().serializeToMiraiCode();
        nextMessageEventFromUser.intercept();
        subject.sendMessage("请发送回复消息:");
        nextMessageEventFromUser = getNextMessageEventFromUser(user);
        if (ShareUtils.isQuit(nextMessageEventFromUser)) {
            return;
        }
        String value = MessageChain.serializeToJsonString(nextMessageEventFromUser.getMessage());
        nextMessageEventFromUser.intercept();
        subject.sendMessage("请发送参数(一次发送，多参数中间隔开):");
        nextMessageEventFromUser = getNextMessageEventFromUser(user);
        if (ShareUtils.isQuit(nextMessageEventFromUser)) {
            return;
        }
        String param = nextMessageEventFromUser.getMessage().serializeToMiraiCode();

        Mate mate = Mate.ACCURATE;
        Scope scope = new Scope(bot.getId(), "当前", false, false, subject.getId(), 0);

        String[] split = param.split(" +");
        for (String s : split) {
            switch (s) {
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
                case "0":
                case "全局":
                    scope = new Scope(bot.getId(), "全局", true, false, subject.getId(), -1);
                    break;
                default:
                    String listPattern = "gr\\d+|群组\\d+";
                    if (Pattern.matches(listPattern, s)) {
                        int listId = Integer.parseInt(s.substring(2));
                        if (!ListUtil.isContainsList(bot, listId)) {
                            subject.sendMessage("该群组不存在!");
                            return;
                        }
                        scope = new Scope(bot.getId(), "群组", false, true, subject.getId(), listId);
                    }
                    break;
            }
        }

        if (subject instanceof User && !scope.getGlobal() && scope.getGroupInfo()) {
            subject.sendMessage("私发学习请输入作用域！");
            return;
        }

        int scope_id = ScopeUtil.getScopeId(bot, scope);
        if (scope_id == -1) {
            subject.sendMessage("学不废!");
            l.warning("学习添加失败,无作用域!");
            return;
        }
        int type = 5;
        saveSession(subject, bot, key, value, mate, scope_id, type);

    }


    //================================================================================

    /**
     * 删除会话数据
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/7/29 15:08
     */
    public static void deleteSession(MessageEvent event) {
        //^-xx\\?[:：](\S+)|^删除( +\S+)
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        Bot bot = event.getBot();

        String[] split = code.split("[:：]");
        if (split.length == 1) {
            split = code.split(" +");
        }
        String key = split[1];

        String deleteSessionSql = "DELETE FROM session WHERE key = ?";

        try {
            HuToolUtil.db.del("session", "key", key);
        } catch (SQLException e) {
            subject.sendMessage("出错啦~~");
            e.printStackTrace();
            return;
        }
        subject.sendMessage("我好像忘记了点啥?");

        init(false);
    }

    /**
     * 保存会话
     *
     * @param subject  消息发送者
     * @param bot      所属机器人
     * @param key      触发词
     * @param value    回复词
     * @param mate     匹配方式
     * @param scope_id 作用域
     * @param type     类型
     * @author Moyuyanli
     * @date 2022/7/29 15:03
     */
    private static void saveSession(Contact subject, Bot bot, String key, String value, Mate mate, int scope_id, int type) {
        String insertSessionSql =
                "INSERT INTO session(bot,type,key,value,mate_id,scope_id)" +
                        "VALUES( ?, ?, ? ,? ,?, ?) ;";

        int i;
        try {
            i = HuToolUtil.db.execute(insertSessionSql, bot.getId(), type, key, value, mate.getMateType(), scope_id);
        } catch (SQLException e) {
            l.error("添加对话失败:" + e.getMessage());
            subject.sendMessage("学不废!");
            e.printStackTrace();
            return;
        }
        if (i == 0) {
            subject.sendMessage("学不废!");
            return;
        }
        subject.sendMessage("学废了!");
        init(false);
    }

    /**
     * 获取该用户的下一次消息事件
     *
     * @param user 用户
     * @return net.mamoe.mirai.event.events.MessageEvent
     * @author Moyuyanli
     * @date 2022/7/29 12:36
     */
    private static MessageEvent getNextMessageEventFromUser(User user) throws ExecutionException, InterruptedException {

        EventChannel<MessageEvent> channel = GlobalEventChannel.INSTANCE.parentScope(HuYanSession.INSTANCE)
                .filterIsInstance(MessageEvent.class)
                .filter(event -> event.getSender().getId() == user.getId());

        CompletableFuture<MessageEvent> future = new CompletableFuture<>();

        channel.subscribeOnce(MessageEvent.class, EmptyCoroutineContext.INSTANCE,
                ConcurrencyKind.LOCKED, EventPriority.HIGH, future::complete);
        return future.get();
    }


    /**
     * 判断作用域
     *
     * @param event   消息事件
     * @param subject 发送者
     * @param session 消息
     * @return java.lang.String
     * @author Moyuyanli
     * @date 2022/7/13 12:25
     */
    private static String judgeScope(MessageEvent event, Contact subject, Session session) {
        String trigger = "其他群触发";
        long groupId = event.getSubject().getId();
        Scope scopeInfo = session.getScopeInfo();
        if (scopeInfo.getGlobal()) {
            trigger = "全局触发";
        } else if (scopeInfo.getGroupInfo()) {
            trigger = "群组:" + scopeInfo.getListId() + "触发";
        } else if (groupId == subject.getId()) {
            trigger = "当前群触发";
        }
        return trigger;
    }

    /**
     * 会话消息分页构造
     *
     * @param event 消息事件
     * @return net.mamoe.mirai.message.data.ForwardMessage
     * @author Moyuyanli
     * @date 2022/7/13 12:16
     */
    private static ForwardMessage parseMessage(MessageEvent event) {
        Contact group = event.getSubject();
        Bot bot = event.getBot();
        ForwardMessageBuilder nodes = new ForwardMessageBuilder(group);
        Map<String, Session> sessionMap;
        try {
            sessionMap = StaticData.getSessionMap(bot);
        } catch (Exception e) {
            group.sendMessage("查询会话消息为空!");
            e.printStackTrace();
            return null;
        }
        if (sessionMap == null) {
            group.sendMessage("会话消息为空!");
            return null;
        }

        MessageChainBuilder table = new MessageChainBuilder();
        MessageChainBuilder accurate = new MessageChainBuilder();
        MessageChainBuilder vague = new MessageChainBuilder();
        MessageChainBuilder start = new MessageChainBuilder();
        MessageChainBuilder end = new MessageChainBuilder();
        MessageChainBuilder other = new MessageChainBuilder();
        MessageChainBuilder special = new MessageChainBuilder();
        table.append("以下为所有查询到的触发关键词结果↓");
        nodes.add(bot, table.build());

        accurate.append("所有的精准匹配触发消息:\n");
        vague.append("所有的模糊匹配触发消息:\n");
        start.append("所有的头部匹配触发消息:\n");
        end.append("所有的结尾匹配触发消息:\n");
        other.append("所有的其他匹配触发消息:\n");
        special.append("所有的特殊匹配触发消息:\n");
        //获取全部消息
        ArrayList<Session> values = new ArrayList<>(sessionMap.values());
        for (Session base : values) {
            //判断触发类别
            String trigger = judgeScope(event, group, base);
            //判断消息类别
            switch (base.getType()) {
                case 0:
                    //判断匹配机制
                    switch (base.getMate()) {
                        case ACCURATE:
                            accurate.append(base.getKey()).append(" ==> ").append(base.getValue()).append(" -> ").append(trigger).append("\n");
                            break;
                        case VAGUE:
                            vague.append(base.getKey()).append(" ==> ").append(base.getValue()).append(" -> ").append(trigger).append("\n");
                            break;
                        case START:
                            start.append(base.getKey()).append(" ==> ").append(base.getValue()).append(" -> ").append(trigger).append("\n");
                            break;
                        case END:
                            end.append(base.getKey()).append(" ==> ").append(base.getValue()).append(" -> ").append(trigger).append("\n");
                            break;
                        default:
                            break;
                    }
                    break;
                case 1:
                    other.append(MiraiCode.deserializeMiraiCode(base.getKey()))
                            .append(" ==> ")
                            .append(MiraiCode.deserializeMiraiCode(base.getValue()))
                            .append(" -> ")
                            .append(trigger)
                            .append(":")
                            .append(base.getMate().getMateName())
                            .append("\n");
                    break;
                case 5:
                    special.append(MiraiCode.deserializeMiraiCode(base.getKey()))
                            .append(" ==> ")
                            .append(MessageChain.deserializeFromJsonString(base.getValue()).contentToString())
                            .append(" -> ")
                            .append(trigger)
                            .append(":")
                            .append(base.getMate().getMateName())
                            .append("\n");
                default:
                    break;
            }
        }
        nodes.add(bot, accurate.build());
        nodes.add(bot, vague.build());
        nodes.add(bot, start.build());
        nodes.add(bot, end.build());
        nodes.add(bot, other.build());
        nodes.add(bot, special.build());

        return nodes.build();
    }

}
