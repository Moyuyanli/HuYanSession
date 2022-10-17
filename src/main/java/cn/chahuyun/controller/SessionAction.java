package cn.chahuyun.controller;

import cn.chahuyun.config.ConfigData;
import cn.chahuyun.data.StaticData;
import cn.chahuyun.entity.Scope;
import cn.chahuyun.entity.Session;
import cn.chahuyun.enums.Mate;
import cn.chahuyun.utils.HibernateUtil;
import cn.chahuyun.utils.ListUtil;
import cn.chahuyun.utils.ShareUtils;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.*;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static cn.chahuyun.HuYanSession.log;
import static cn.chahuyun.utils.ShareUtils.DYNAMIC_MESSAGE_PATTERN;

/**
 * 对话消息工具类
 *
 * @author Moyuyanli
 * @Description :对话消息工具类
 * @Date 2022/7/9 17:14
 */
public class SessionAction {


    /**
     * 刷新静态内存数据
     *
     * @param type true 初始化加载 false 刷新
     * @author Moyuyanli
     * @date 2022/7/29 22:25
     */
    public static void init(boolean type) {
        List<Session> sessionInfos;
        //获取sessionList
        try {
            sessionInfos = HibernateUtil.factory.fromTransaction(session -> {
                HibernateCriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
                JpaCriteriaQuery<Session> query = criteriaBuilder.createQuery(Session.class);
                JpaRoot<Session> from = query.from(Session.class);
                query.select(from);
                return session.createQuery(query).list();
            });
        } catch (Exception e) {
            log.error("会话数据加载失败:", e);
            return;
        }

        Map<Long, Map<String, Session>> sessionAll = new HashMap<>();
        //解析成sessionMap
        if (sessionInfos != null && !sessionInfos.isEmpty()) {
            for (Session entity : sessionInfos) {
                if (!sessionAll.containsKey(entity.getBot())) {
                    sessionAll.put(entity.getBot(), new HashMap<>() {{
                        put(entity.getTerm(), entity);
                    }});
                    continue;
                }
                Map<String, Session> sessionMap = sessionAll.get(entity.getBot());
                if (!sessionMap.containsKey(entity.getTerm())) {
                    sessionAll.get(entity.getBot()).put(entity.getTerm(), entity);
                }
            }
            StaticData.setSessionMap(sessionAll);
        } else {
            StaticData.setSessionMap(sessionAll);
        }
        if (ConfigData.INSTANCE.getDebugSwitch() && type) {
            log.info("数据库会话信息初始化成功!");
            return;
        }
        if (ConfigData.INSTANCE.getDebugSwitch()) {
            log.info("会话数据更新成功!");
        }

    }


    /**
     * 学习会话
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/7/29 22:27
     */
    public void studySession(MessageEvent event) {
        //xx a b [p1] [p2]
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        Bot bot = event.getBot();

        //解析消息，获取参数
        String[] split = code.split("\\s+");
        String key = split[1];
        String value = split[2];
        //验证是否存在
        if (StaticData.isSessionKey(bot, key)) {
            subject.sendMessage("我已经学废了" + key + "!不能再学了!");
            return;
        }
        //判断消息是否含有图片
        int type = 0;
        String typePattern = "\\[mirai:image\\S+]";
        if (Pattern.matches(typePattern, key) || Pattern.matches(typePattern, value)) {
            type = 1;
        }
        //判断是否存在动态消息
        boolean dynamic = false;
        Pattern compile = Pattern.compile(DYNAMIC_MESSAGE_PATTERN);
        if (compile.matcher(key).find() || compile.matcher(value).find()) {
            dynamic = true;
        }

        Mate mate = Mate.ACCURATE;
        Scope scope = new Scope(bot.getId(), "当前", false, false, subject.getId(), -1);

        //解析参数
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
                    case "5":
                    case "正则":
                        subject.sendMessage("xx 学习不支持正则");
                        return;
                    case "0":
                    case "全局":
                        scope = new Scope(bot.getId(), "全局", true, false, subject.getId(), -1);
                        break;
                    default:
                        String listPattern = "gr\\d+|群组\\d+";
                        if (Pattern.matches(listPattern, s)) {
                            int listId = Integer.parseInt(s.substring(2));

                            if (ListUtil.isContainsList(bot, listId)) {
                                subject.sendMessage("该群组不存在!");
                                return;
                            }
                            scope = new Scope(bot.getId(), "群组" + listId, false, true, subject.getId(), listId);
                        }
                        break;
                }
            }
        }
        //如果是私聊，需要发送作用域
        if (subject instanceof User && !scope.getGlobal() && scope.getGroupInfo()) {
            subject.sendMessage("私发学习请输入作用域！");
            return;
        }
        //保存
        saveSession(subject, bot, key, value, mate, scope, type, dynamic);
    }

    /**
     * 查询所有会话消息
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/7/13 21:21
     */
    public void querySession(MessageEvent event) {
        //xx:key?
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        User user = event.getSender();
        Bot bot = event.getBot();
        init(false);
        //匹配是否查询单条
        String querySessionPattern = "xx\\\\?[:：](\\S+)|查询 +\\S+";
        boolean type = Pattern.matches(querySessionPattern, code);

        String key;
        if (type) {
            String[] split = code.split("[:：]| +");
            key = split[1];
            Map<String, Session> sessionMap;
            try {
                sessionMap = StaticData.getSessionMap(bot);
            } catch (Exception e) {
                subject.sendMessage("查询会话消息为空!");
                return;
            }
            if (sessionMap == null ) {
                subject.sendMessage("我不太会讲发~!");
                return;
            }
            if (sessionMap.containsKey(key)) {
                Session sessionInfo = sessionMap.get(key);
                //判断触发类别

                String trigger = judgeScope(event, subject, sessionInfo);
                subject.sendMessage("查询到对应会话:\n" +
                        sessionInfo.getTerm() + "==>" + sessionInfo.getReply() + "\n" +
                        "匹配方式:" + sessionInfo.getMate().getMateName() + "\n" +
                        "触发范围:" + trigger);
                return;
            }
            subject.sendMessage("我不是很会讲发~");
            return;
        }
        int startIndex = -1;
        while (true) {
            Map<String, Object> stringObjectMap = parseMessage(event, startIndex);
            if (stringObjectMap == null) {
                return;
            }
            ForwardMessage build = (ForwardMessage) stringObjectMap.get("build");
            if (build != null) {
                subject.sendMessage(build);
            }
            int start = (int) stringObjectMap.get("start");
            int end = (int) stringObjectMap.get("end");
            boolean up = (boolean) stringObjectMap.get("up");
            boolean down = (boolean) stringObjectMap.get("down");
            MessageEvent nextMessageEventFromUser = ShareUtils.getNextMessageEventFromUser(user);
            if (ShareUtils.isQuit(nextMessageEventFromUser)) {
                return;
            }
            String string = nextMessageEventFromUser.getMessage().contentToString();
            if (up && string.equals("上一页")) {
                startIndex = start - 1;
            } else if (down && string.equals("下一页")) {
                startIndex = end;
            } else {
                return;
            }
        }

    }

    /**
     * 通过发送消息的方式进行添加会话
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/7/29 15:05
     */
    public void studyDialogue(MessageEvent event) {
        //%xx|学习对话
        Contact subject = event.getSubject();
        User user = event.getSender();
        Bot bot = event.getBot();

        subject.sendMessage("开始添加对话，请输入触发内容:");
        event.intercept();
        MessageEvent nextMessageEventFromUser = ShareUtils.getNextMessageEventFromUser(user);
        if (ShareUtils.isQuit(nextMessageEventFromUser)) {
            return;
        }
        String key = nextMessageEventFromUser.getMessage().serializeToMiraiCode();
        String patternString = nextMessageEventFromUser.getMessage().contentToString();


        subject.sendMessage("请发送回复消息:");
        nextMessageEventFromUser = ShareUtils.getNextMessageEventFromUser(user);
        if (ShareUtils.isQuit(nextMessageEventFromUser)) {
            return;
        }
        MessageChain valueChain = nextMessageEventFromUser.getMessage();


        subject.sendMessage("请发送参数(一次发送，多参数中间隔开):");
        nextMessageEventFromUser = ShareUtils.getNextMessageEventFromUser(user);
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
                case "5":
                case "正则":
                    mate = Mate.PATTERN;
                    key = patternString;
                    break;
                case "0":
                case "全局":
                    scope = new Scope(bot.getId(), "全局", true, false, subject.getId(), -1);
                    break;
                default:
                    String listPattern = "gr\\d+|群组\\d+";
                    if (Pattern.matches(listPattern, s)) {
                        int listId = Integer.parseInt(s.substring(2));
                        if (ListUtil.isContainsList(bot, listId)) {
                            subject.sendMessage("该群组不存在!");
                            return;
                        }
                        scope = new Scope(bot.getId(), "群组" + listId, false, true, subject.getId(), listId);
                    }
                    break;
            }
        }

        if (subject instanceof User && !scope.getGlobal() && scope.getGroupInfo()) {
            subject.sendMessage("私发学习请输入作用域！");
            return;
        }

        String value = valueChain.serializeToMiraiCode();
        //判断消息是否含有图片
        int type = 0;
        String typePattern = "\\[mirai:image\\S+]";
        if (Pattern.matches(typePattern, key) || Pattern.matches(typePattern, value)) {
            type = 1;
        }
        //判断是否存在动态消息
        boolean dynamic = false;
        Pattern compile = Pattern.compile(DYNAMIC_MESSAGE_PATTERN);
        if (compile.matcher(key).find() || compile.matcher(value).find()) {
            dynamic = true;
        }
        //判断消息是否是转发消息或音频消息
        if (valueChain.contains(ForwardMessage.Key) || valueChain.contains(Audio.Key)) {
            type = 5;
            dynamic = false;
            value = MessageChain.serializeToJsonString(valueChain);
        }
        saveSession(subject, bot, key, value, mate, scope, type, dynamic);

    }


    /**
     * 删除会话数据
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/7/29 15:08
     */
    public void deleteSession(MessageEvent event) {
        //^-xx\\?[:：](\S+)|^删除( +\S+)
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        Bot bot = event.getBot();

        String[] split = code.split("[:：]");
        if (split.length == 1) {
            split = code.split(" +");
        }
        String key = split[1];

        deleteMessage(subject, bot, key);
    }

    /**
     * 会话形式删除消息
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/8/20 12:31
     */
    public void deleteInformationSession(MessageEvent event) {
        Contact subject = event.getSubject();
        Bot bot = event.getBot();
        User user = event.getSender();

        subject.sendMessage("请发送需要删除的消息");
        MessageEvent eventFromUser = ShareUtils.getNextMessageEventFromUser(user);

        String key = eventFromUser.getMessage().serializeToMiraiCode();
        String patternString = eventFromUser.getMessage().contentToString();

        Map<String, Session> sessionMap = StaticData.getSessionMap(bot);
        if (sessionMap.containsKey(patternString)) {
            deleteMessage(subject, bot, patternString);
        } else {
            deleteMessage(subject, bot, key);
        }
    }

    //================================================================================


    /**
     * 删除消息
     *
     * @param subject 消息发送着
     * @param bot     所属机器人
     * @param key     键
     * @author Moyuyanli
     * @date 2022/8/20 12:31
     */
    private void deleteMessage(Contact subject, Bot bot, String key) {
        Map<String, Session> sessionMap = StaticData.getSessionMap(bot);
        Session sessionInfo;
        if (sessionMap.containsKey(key)) {
            sessionInfo = sessionMap.get(key);
        } else {
            subject.sendMessage("没有找到忘掉的东西...");
            return;
        }

        try {
            HibernateUtil.factory.fromTransaction(session -> {
                session.remove(sessionInfo);
                return 0;
            });
        } catch (Exception e) {
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
     * @param subject 消息发送者
     * @param bot     所属机器人
     * @param key     触发词
     * @param value   回复词
     * @param mate    匹配方式
     * @param scope   作用域
     * @param type    类型
     * @author Moyuyanli
     * @date 2022/7/29 15:03
     */
    private void saveSession(Contact subject, Bot bot, String key, String value, Mate mate, Scope scope, int type, boolean dynamic) {
        try {
            HibernateUtil.factory.fromTransaction(session -> {
                Session sessionInfoEntity = new Session(bot.getId(), type, key, value, mate, scope, dynamic);
                //判断对应作用域是否存在
                session.persist(sessionInfoEntity);
                return 0;
            });
        } catch (Exception e) {
            log.error("添加对话失败:" + e.getMessage());
            subject.sendMessage("学不废!");
            e.printStackTrace();
            return;
        }
        subject.sendMessage("学废了!");
        init(false);
    }


    /**
     * 判断作用域
     *
     * @param event       消息事件
     * @param subject     发送者
     * @param sessionInfo 消息
     * @return java.lang.String
     * @author Moyuyanli
     * @date 2022/7/13 12:25
     */
    private String judgeScope(MessageEvent event, Contact subject, Session sessionInfo) {
        String trigger = "其他群触发";
        long groupId = event.getSubject().getId();
        Scope scopeInfo = sessionInfo.getScope();
        if (scopeInfo.getGlobal()) {
            trigger = "全局触发";
        } else if (scopeInfo.getGroupInfo()) {
            trigger = "群组:" + scopeInfo.getListId() + "触发";
        } else if (scopeInfo.getGroupNumber() == groupId) {
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
    private Map<String, Object> parseMessage(MessageEvent event, int startIndex) {
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
        if (sessionMap == null || sessionMap.isEmpty()) {
            group.sendMessage("会话消息为空!");
            return null;
        }


        MessageChainBuilder accurate = new MessageChainBuilder();
        MessageChainBuilder vague = new MessageChainBuilder();
        MessageChainBuilder start = new MessageChainBuilder();
        MessageChainBuilder end = new MessageChainBuilder();
        MessageChainBuilder pattern = new MessageChainBuilder();
        MessageChainBuilder other = new MessageChainBuilder();
        MessageChainBuilder special = new MessageChainBuilder();
        nodes.add(bot, new PlainText("以下为所有查询到的触发关键词结果↓"));

        accurate.append("所有的精准匹配触发消息:\n");
        vague.append("所有的模糊匹配触发消息:\n");
        start.append("所有的头部匹配触发消息:\n");
        end.append("所有的结尾匹配触发消息:\n");
        pattern.append("所有的正则匹配触发消息:\n");
        other.append("所有的其他匹配触发消息:\n");
        special.append("所有的特殊匹配触发消息:\n");

        int nodesLength = 929;

        //获取全部消息
        ArrayList<Session> values = new ArrayList<>(sessionMap.values());
        int size = values.size();
        int endIndex = 0;
        for (int i = startIndex += 1; i < size; i++) {
            Session base = values.get(i);
            //判断触发类别
            String trigger = judgeScope(event, group, base);
            //判断消息类别
            switch (base.getType()) {
                case 0:
                    //判断匹配机制
                    switch (base.getMate()) {
                        case ACCURATE:
                            accurate.append(base.getTerm()).append(" ==> ").append(base.getReply()).append(" -> ").append(trigger).append("\n");
                            int accurateLength = accurate.build().toString().length();
                            nodesLength += accurateLength;
                            log.debug("accurate.toString.length->" + accurateLength);
                            break;
                        case VAGUE:
                            vague.append(base.getTerm()).append(" ==> ").append(base.getReply()).append(" -> ").append(trigger).append("\n");
                            int vagueLength = vague.build().toString().length();
                            log.debug("vague.toString.length->" + vagueLength);
                            break;
                        case START:
                            start.append(base.getTerm()).append(" ==> ").append(base.getReply()).append(" -> ").append(trigger).append("\n");
                            int startLength = start.build().toString().length();
                            log.debug("start.toString.length->" + startLength);
                            break;
                        case END:
                            end.append(base.getTerm()).append(" ==> ").append(base.getReply()).append(" -> ").append(trigger).append("\n");
                            int endLength = end.build().toString().length();
                            log.debug("end.toString.length->" + endLength);
                            break;
                        case PATTERN:
                            pattern.append(base.getTerm()).append(" ==> ").append(base.getReply()).append(" -> ").append(trigger).append("\n");
                            int patternLength = end.build().toString().length();
                            log.debug("pattern.toString.length->" + patternLength);
                            break;
                        default:
                            break;
                    }
                    break;
                case 1:
                    other.append(MiraiCode.deserializeMiraiCode(base.getTerm()))
                            .append(" ==> ")
                            .append(MiraiCode.deserializeMiraiCode(base.getReply()))
                            .append(" -> ")
                            .append(trigger)
                            .append(":")
                            .append(base.getMate().getMateName())
                            .append("\n");
                    int OtherLength = other.build().toString().length();
                    log.debug("other.toString.length->" + OtherLength);

                    break;
                case 5:
                    special.append(MiraiCode.deserializeMiraiCode(base.getTerm()))
                            .append(" ==> ")
                            .append(MessageChain.deserializeFromJsonString(base.getReply()).contentToString())
                            .append(" -> ")
                            .append(trigger)
                            .append(":")
                            .append(base.getMate().getMateName())
                            .append("\n");
                    int specialLength = special.build().toString().length();
                    log.debug("other.toString.length->" + specialLength);
                default:
                    break;
            }
            endIndex = i;
            if (nodesLength > 4000) {
                log.debug("endIndex->" + endIndex);
                break;
            }
        }
        nodes.add(bot, accurate.build());
        nodes.add(bot, vague.build());
        nodes.add(bot, start.build());
        nodes.add(bot, end.build());
        nodes.add(bot, pattern.build());
        nodes.add(bot, other.build());
        nodes.add(bot, special.build());

        nodes.add(bot, new PlainText(String.format("----- 当前条数: %d ~ %d /总条数: %d  -----", startIndex + 1, endIndex + 1, size)));
        Map<String, Object> map = new HashMap<>();
        map.put("start", startIndex);
        map.put("end", endIndex);
        map.put("size", endIndex);
        //是否能上一页
        map.put("up", startIndex > -1);
        //能否下一页
        map.put("down", endIndex < size);

        ForwardMessage build = nodes.build();
        log.debug("session.toString.length->" + build.toString().length());
        map.put("build", build);
        return map;
    }

}
