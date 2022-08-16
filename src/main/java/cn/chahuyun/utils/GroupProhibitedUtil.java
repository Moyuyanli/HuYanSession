package cn.chahuyun.utils;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.data.StaticData;
import cn.chahuyun.entity.GroupProhibited;
import cn.chahuyun.entity.Scope;
import cn.chahuyun.files.ConfigData;
import kotlin.coroutines.EmptyCoroutineContext;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.ConcurrencyKind;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.EventPriority;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.utils.MiraiLogger;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

/**
 * GroupProhibitedUtil
 * 违禁词工具
 *
 * @author Zhangjiaxing
 * @date 2022/8/16 14:19
 */
public class GroupProhibitedUtil {

    private final static MiraiLogger l = HuYanSession.INSTANCE.getLogger();

    /**
     * 初始化或加载违禁词
     *
     * @param type true 初始化 false 加载
     * @author Moyuyanli
     * @date 2022/8/16 15:20
     */
    public static void init(boolean type) {
        List<GroupProhibited> groupProhibiteds = null;
        try {
            groupProhibiteds = HibernateUtil.factory.fromTransaction(session -> {
                HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
                JpaCriteriaQuery<GroupProhibited> query = builder.createQuery(GroupProhibited.class);
                JpaRoot<GroupProhibited> from = query.from(GroupProhibited.class);
                query.select(from);
                List<GroupProhibited> list = session.createQuery(query).list();
                for (GroupProhibited groupProhibited : list) {
                    if (groupProhibited.getScopeInfo() == null) {
                        Scope scope = ScopeUtil.getScope(groupProhibited.getScopeMark());
                        groupProhibited.setScopeInfo(scope);
                    }
                }
                return list;
            });
        } catch (Exception e) {
            l.error("出错啦~", e);
        }

        StaticData.setProhibitedMap(parseList(groupProhibiteds));

        if (type) {
            l.info("数据库会话信息初始化成功!");
            return;
        }
        if (ConfigData.INSTANCE.getDebugSwitch()) {
            l.info("会话数据更新成功!");
        }

    }

    public static void addProhibited(MessageEvent event) throws ExecutionException, InterruptedException {
        //+wjc:body [3h|gr1|%(重设回复消息)|ch|jy|hmd3|0|全局]
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        Bot bot = event.getBot();
        User user = event.getSender();

        Map<Scope, List<GroupProhibited>> prohibitedMap = StaticData.getProhibitedMap(bot);

        String[] strings = code.split("[:：]")[1].split(" +");
        String key = strings[0];
        Scope scope = new Scope(bot.getId(), "当前", false, false, subject.getId(), -1);
        GroupProhibited groupProhibited = new GroupProhibited(bot.getId(), key, "$at(this)触发天条,$message(prohibitString)", 60, "1m", true, true, false, 0, scope);

        if (strings.length > 1) {
            for (int i = 1; i < strings.length; i++) {
                String string = strings[i];
                switch (string) {
                    case "ch":
                        groupProhibited.setWithdraw(false);
                        break;
                    case "jy":
                        groupProhibited.setProhibit(false);
                        break;
                    case "0":
                    case "全局":
                        scope.setGlobal(true);
                        groupProhibited.setScopeInfo(scope);
                        break;
                    case "%":
                        //获取下一次消息
                        subject.sendMessage("请输入触发违禁词回复内容:");
                        String reply = getNextMessageEventFromUser(user).getMessage().serializeToMiraiCode();
                        groupProhibited.setReply(reply);
                        break;
                    default:
                        if (Pattern.matches("\\d+[smhd]", string)) {
                            int timeParam = Integer.parseInt(string.substring(0, string.length() - 1));
                            String type = string.substring(string.length() - 1);
                            int time = 0;
                            String messages = "";
                            switch (type) {
                                case "s":
                                    time = timeParam;
                                    messages += "禁言:" + timeParam + "秒";
                                    break;
                                case "m":
                                    time = timeParam * 60;
                                    messages += "禁言:" + timeParam + "分钟";
                                    break;
                                case "h":
                                    time = timeParam * 60 * 60;
                                    messages += "禁言:" + timeParam + "小时";
                                    break;
                                case "d":
                                    time = timeParam * 60 * 60 * 24;
                                    messages += "禁言:" + timeParam + "天";
                                    break;
                                default:
                                    subject.sendMessage("禁言时间格式错误!");
                                    return;
                            }
                            groupProhibited.setProhibitTime(time);
                            groupProhibited.setProhibitString(messages);
                        } else if (Pattern.matches("gr\\d+", string)) {
                            scope.setGroupInfo(true);
                            scope.setListId(Integer.parseInt(string.substring(1)));
                            groupProhibited.setScopeInfo(scope);
                        } else if (Pattern.matches("hmd\\d+", string)) {
                            int number = Integer.parseInt(string.substring(2));
                            groupProhibited.setAccumulate(true);
                            groupProhibited.setAccumulateNumber(number);
                        }
                        break;
                }
            }
        }

        //寻找是否存在这个触发词的违禁词，如果有，将原id付给新的违禁词进行修改
        if (prohibitedMap.containsKey(scope)) {
            List<GroupProhibited> prohibitedList = prohibitedMap.get(scope);
            for (GroupProhibited prohibited : prohibitedList) {
                if (prohibited.getTrigger().equals(prohibited.getTrigger())) {
                    groupProhibited.setId(prohibited.getId());
                }
            }
        }

        try {
            HibernateUtil.factory.fromTransaction(session -> {
                session.merge(groupProhibited);
                return 0;
            });
        } catch (Exception e) {
            subject.sendMessage("违禁词添加失败!");
            l.error("出错啦~", e);
            return;
        }

        init(false);
    }


    //==========================================================================================


    /**
     * 解析违禁词数组
     *
     * @param prohibitedList 违禁词list
     * @return java.util.Map<java.lang.Long, java.util.Map < cn.chahuyun.entity.Scope, java.util.List < cn.chahuyun.entity.GroupProhibited>>>
     * @author Moyuyanli
     * @date 2022/8/16 15:19
     */
    private static Map<Long, Map<Scope, List<GroupProhibited>>> parseList(List<GroupProhibited> prohibitedList) {
        if (prohibitedList == null || prohibitedList.isEmpty()) {
            return null;
        }
        Map<Long, Map<Scope, List<GroupProhibited>>> listMap = new HashMap<>();

        for (GroupProhibited entity : prohibitedList) {
            long bot = entity.getBot();
            Scope scope = entity.getScopeInfo();

            if (!listMap.containsKey(bot)) {
                listMap.put(bot, new HashMap<>() {{
                    put(scope, new ArrayList<>() {{
                        add(entity);
                    }});
                }});
                continue;
            }
            if (!listMap.get(bot).containsKey(scope)) {
                listMap.get(bot).get(scope).add(entity);
            }
        }
        return listMap;
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
        MessageEvent event = future.get();
        event.intercept();
        return event;
    }


}