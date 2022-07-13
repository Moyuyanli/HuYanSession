package cn.chahuyun.utils;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.data.StaticData;
import cn.chahuyun.entity.Scope;
import cn.chahuyun.entity.Session;
import cn.chahuyun.enums.Mate;
import cn.chahuyun.files.ConfigData;
import cn.hutool.db.Entity;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.ForwardMessage;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.utils.MiraiLogger;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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


    public static void init(boolean type) {

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
        List<Entity> entities = null;
        try {
            entities = HuToolUtil.db.query(querySessionSql);
        } catch (SQLException e) {
            l.error("会话数据加载失败:"+e.getMessage());
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
                Session session = null;
                Scope scope = null;
                try {
                    scope = BeanUtil.parseEntity(entity, Scope.class);
                    session = BeanUtil.parseEntity(entity, Session.class);
                } catch (Exception e) {
                    l.error("会话数据初始化失败:"+e.getMessage());
                    e.printStackTrace();
                    return;
                }
                session.setMate(mate);
                session.setScope(scope);
                if (!sessionAll.containsKey(session.getBot())) {
                    Session finalSession = session;
                    sessionAll.put(session.getBot(), new HashMap<String, Session>() {{
                        put(finalSession.getKey(), finalSession);
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


    public static void studySession(MessageEvent event) {
        //xx a b [p1] [p2]
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        Bot bot = event.getBot();

        String[] split = code.split("\\s+");
        int type = 0;
        String key = split[1];
        String value = split[2];

        if (StaticData.isSessionKey(bot.getId(), key)) {
            subject.sendMessage("我已经学废了"+key+"!不能再学了!");
            return;
        }

        String typePattern = "\\[mirai:image\\S+]";
        if (Pattern.matches(typePattern, key) || Pattern.matches(typePattern, value)) {
            type = 1;
        }

        Mate mate = Mate.ACCURATE;
        Scope scope = new Scope(bot.getId(),"当前", false, false, subject.getId(), -1);

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
                        scope = new Scope(bot.getId(),"全局", true, false, subject.getId(), -1);
                        break;
                    default:
                        String listPattern = "gr\\d+|群组\\d+";
                        if (Pattern.matches(listPattern, s)) {
                            int listId = Integer.parseInt(s.substring(2));
                            if (!ListUtil.isContainsList(bot.getId(), listId)) {
                                subject.sendMessage("该群组不存在!");
                                return;
                            }
                            scope = new Scope(bot.getId(),"群组", false, true, subject.getId(), listId);
                        }
                        break;
                }
            }
        }
        if (subject instanceof User && !scope.isGlobal() && scope.isGroup() ) {
            subject.sendMessage("私发学习请输入作用域！");
            return;
        }

        int scope_id = ScopeUtil.getScopeId(bot, scope);
        if (scope_id == -1) {
            subject.sendMessage("学不废!");
            l.warning("学习添加失败,无作用域!");
            return;
        }

        String insertSessionSql =
        "INSERT INTO session(bot,type,key,value,mate_id,scope_id)"+
        "VALUES( ?, ?, ? ,? ,?, ?) ;";

        int i = 0;
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


    public static void querySession(MessageEvent event) {
        //xx:key?
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        Bot bot = event.getBot();

        String querySessionPattern = "xx\\\\?[:：](\\S+)|查询 +\\S+";

        boolean type = Pattern.matches(querySessionPattern, code);
        l.info("type-" + type);
        String key = null;
        if (type) {
            String[] split = code.split("[:：]| +");
            key = split[1];
            Map<String, Session> sessionMap;
            try {
                l.info("1");
                sessionMap = StaticData.getSessionMap(bot.getId());
            } catch (Exception e) {
                subject.sendMessage("查询会话消息为空!");
                e.printStackTrace();
                return;
            }
            if (sessionMap == null) {
                l.info("3");
                subject.sendMessage("我不太会讲发~!");
                return;
            }
            l.info(sessionMap.containsKey(key)+"");
            if (sessionMap.containsKey(key)) {
                l.info("4");
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
     * 判断作用域
     * @author Moyuyanli
     * @param event 消息事件
     * @param subject 发送者
     * @param session 消息
     * @date 2022/7/13 12:25
     * @return java.lang.String
     */
    private static String judgeScope(MessageEvent event, Contact subject, Session session) {
        String trigger = "其他群触发";
        long groupId = event.getSubject().getId();
        if (session.getScope().isGlobal()) {
            trigger = "全局触发";
        } else if (session.getScope().isGroup()) {
            trigger = "群组:" + session.getScope().getListId() + "触发";
        } else if (groupId == subject.getId()) {
            trigger = "当前群触发";
        }
        return trigger;
    }

    /**
     * 会话消息分页构造
     * @author Moyuyanli
     * @param event 消息事件
     * @date 2022/7/13 12:16
     * @return net.mamoe.mirai.message.data.ForwardMessage
     */
    private static ForwardMessage parseMessage(MessageEvent event) {
        Contact group = event.getSubject();
        Bot bot = event.getBot();
        ForwardMessageBuilder nodes = new ForwardMessageBuilder(group);
        Map<String, Session> sessionMap = null;
        try {
            sessionMap = StaticData.getSessionMap(bot.getId());
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
        table.append("以下为所有查询到的触发关键词结果↓");
        nodes.add(bot, table.build());

        accurate.append("所有的精准匹配触发消息:\n");
        vague.append("所有的模糊匹配触发消息:\n");
        start.append("所有的头部匹配触发消息:\n");
        end.append("所有的结尾匹配触发消息:\n");
        other.append("所有的其他匹配触发消息:\n");
        //获取全部消息
        ArrayList<Session> values = new ArrayList<Session>(sessionMap.values());
        for (Session base : values) {
            //判断触发类别
            String trigger = judgeScope(event, group, base);
            //判断消息类别
            switch (base.getType()) {
                case 0:
                    //判断匹配机制
                    switch (base.getMate()) {
                        case ACCURATE:
                            accurate.append(base.getKey() + " ==> " + base.getValue() + " -> " + trigger + "\n");
                            break;
                        case VAGUE:
                            vague.append(base.getKey() + " ==> " + base.getValue() + " -> " + trigger + "\n");
                            break;
                        case START:
                            start.append(base.getKey() + " ==> " + base.getValue() + " -> " + trigger + "\n");
                            break;
                        case END:
                            end.append(base.getKey() + " ==> " + base.getValue() + " -> " + trigger + "\n");
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
                default:
                    break;
            }
        }
        nodes.add(bot, accurate.build());
        nodes.add(bot, vague.build());
        nodes.add(bot, start.build());
        nodes.add(bot, end.build());
        nodes.add(bot, other.build());

        return nodes.build();
    }

}
