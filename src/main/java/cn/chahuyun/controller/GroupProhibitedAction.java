package cn.chahuyun.controller;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.config.ConfigData;
import cn.chahuyun.data.StaticData;
import cn.chahuyun.entity.GroupProhibited;
import cn.chahuyun.entity.Scope;
import cn.chahuyun.utils.HibernateUtil;
import cn.chahuyun.utils.ScopeUtil;
import cn.chahuyun.utils.ShareUtils;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.MessageUtils;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.MiraiLogger;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * GroupProhibitedUtil
 * 违禁词工具
 *
 * @author Moyuyanli
 * @date 2022/8/16 14:19
 */
public class GroupProhibitedAction {

    private final static MiraiLogger l = HuYanSession.INSTANCE.getLogger();

    /**
     * 初始化或加载违禁词
     *
     * @param type true 初始化 false 加载
     * @author Moyuyanli
     * @date 2022/8/16 15:20
     */
    public static void init(boolean type) {
        List<GroupProhibited> groupProhibits = null;
        try {
            groupProhibits = HibernateUtil.factory.fromTransaction(session -> {
                HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
                JpaCriteriaQuery<GroupProhibited> query = builder.createQuery(GroupProhibited.class);
                JpaRoot<GroupProhibited> from = query.from(GroupProhibited.class);
                query.select(from);
                return session.createQuery(query).list();
            });
        } catch (Exception e) {
            l.error("出错啦~", e);
        }

        StaticData.setProhibitedMap(parseList(groupProhibits));

        if (ConfigData.INSTANCE.getDebugSwitch() && type) {
            l.info("数据库违禁词信息初始化成功!");
            return;
        }
        if (ConfigData.INSTANCE.getDebugSwitch()) {
            l.info("违禁词数据更新成功!");
        }

    }

    /**
     * 添加违禁词
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/8/17 19:12
     */
    public static void addProhibited(MessageEvent event) {
        //+wjc:body [3h|gr1|%(重设回复消息)|ch|jy|hmd3|0|全局|1|2|3|4]
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        Bot bot = event.getBot();
        User user = event.getSender();

        Map<Scope, List<GroupProhibited>> prohibitedMap = StaticData.getProhibitedMap(bot);

        //直接把第一个冒号替换为 [ ] 用于分割
        code = code.replaceFirst("[:：]", " ");

        String[] strings = code.split(" +");
        String key = strings[1];

        Scope scope = new Scope(bot.getId(), "当前", false, false, subject.getId(), -1);
        GroupProhibited groupProhibited = new GroupProhibited(bot.getId(), key, ConfigData.INSTANCE.getVariableSymbol() + "at(this)触发天条," + ConfigData.INSTANCE.getVariableSymbol() + "message(prohibitString)", 60, "1m", true, true, false, 0, scope);

        if (strings.length > 2) {
            for (int i = 2; i < strings.length; i++) {
                String string = strings[i];
                switch (string) {
                    case "ch":
                        groupProhibited.setWithdraw(false);
                        break;
                    case "jy":
                        groupProhibited.setProhibit(false);
                        break;
                    case "精准":
                    case "1":
                        groupProhibited.setMateType(1);
                        break;
                    case "头部":
                    case "3":
                        groupProhibited.setMateType(3);
                        break;
                    case "结尾":
                    case "4":
                        groupProhibited.setMateType(4);
                        break;
                    case "0":
                    case "全局":
                        scope.setScopeName("全局");
                        scope.setGlobal(true);
                        groupProhibited.setScopeInfo(scope);
                        break;
                    case "%":
                        //获取下一次消息
                        subject.sendMessage("请输入触发违禁词回复内容:");
                        String reply = ShareUtils.getNextMessageEventFromUser(user).getMessage().serializeToMiraiCode();
                        groupProhibited.setReply(reply);
                        break;
                    default:
                        if (Pattern.matches("\\d+[smhd]", string)) {
                            int timeParam = Integer.parseInt(string.substring(0, string.length() - 1));
                            String type = string.substring(string.length() - 1);
                            int time;
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
                            scope.setScopeName("群组" + string.substring(1));
                            scope.setGroupInfo(true);
                            scope.setListId(Integer.parseInt(string.substring(1)));
                            groupProhibited.setScopeInfo(scope);
                        } else if (Pattern.matches("hmd\\d+", string)) {
                            int number = Integer.parseInt(string.substring(3));
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
                if (prohibited.getTrigger().equals(groupProhibited.getTrigger())) {
                    groupProhibited.setId(prohibited.getId());
                }
            }
        }

        try {
            HibernateUtil.factory.fromTransaction(session -> {
                //判断对应作用域是否存在
                if (ScopeUtil.isScopeEmpty(scope)) {
                    //不存在则先添加作用域
                    session.persist(scope);
                }
                session.merge(groupProhibited);
                return 0;
            });
        } catch (Exception e) {
            subject.sendMessage("违禁词添加失败!");
            l.error("出错啦~", e);
            return;
        }

        subject.sendMessage(MessageUtils.newChain().plus("违禁词 ").plus(MiraiCode.deserializeMiraiCode(key).plus(" 添加成功！")));

        init(false);
    }


    /**
     * 查询违禁词
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/8/17 19:14
     */
    public static void queryGroupProhibited(MessageEvent event) {
        //wjc：
        Contact subject = event.getSubject();
        Bot bot = event.getBot();

        ForwardMessageBuilder builder = new ForwardMessageBuilder(subject);
        builder.add(bot, new PlainText("以下是本群触发的所有违禁词↓"));

        Map<Scope, List<GroupProhibited>> prohibitedMap = StaticData.getProhibitedMap(bot);
        for (Scope scope : prohibitedMap.keySet()) {
            if (ShareUtils.mateScope(event, scope)) {
                List<GroupProhibited> prohibitedList = prohibitedMap.get(scope);
                for (GroupProhibited prohibited : prohibitedList) {
                    builder.add(bot, singleMessages -> {
                        singleMessages.add("违禁词编号:" + prohibited.getId() + "\n" +
                                "违禁词触发词:" + prohibited.getTrigger() + "\n" +
                                "违禁词回复词:" + prohibited.getReply() + "\n" +
                                "是否撤回:" + (prohibited.isWithdraw() ? "是" : "否") + "\n" +
                                "是否禁言:" + (prohibited.isProhibit() ? "是" : "否") + "\n" +
                                "是否累计黑名单次数:" + (prohibited.isAccumulate() ? "是" : "否"));
                        if (prohibited.isAccumulate()) {
                            singleMessages.add("\n次数上限:" + prohibited.getAccumulateNumber());
                        }
                        return null;
                    });
                }
            }
        }

        subject.sendMessage(builder.build());

    }


    /**
     * 删除违禁词
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/8/18 14:20
     */
    public static void deleteProhibited(MessageEvent event) {
        //-wjc:id
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        Bot bot = event.getBot();

        int key = Integer.parseInt(code.split("[:：]")[1]);
        Map<Scope, List<GroupProhibited>> prohibitedMap = StaticData.getProhibitedMap(bot);

        GroupProhibited groupProhibited = null;

        for (Scope scope : prohibitedMap.keySet()) {
            if (ShareUtils.mateScope(event, scope)) {
                List<GroupProhibited> prohibitedList = prohibitedMap.get(scope);
                for (GroupProhibited prohibited : prohibitedList) {
                    if (prohibited.getId() == key) {
                        groupProhibited = prohibited;
                    }
                }
            }
        }

        if (groupProhibited == null) {
            subject.sendMessage("没有找到你要删除的违禁词");
            return;
        }

        try {
            GroupProhibited finalGroupProhibited = groupProhibited;
            HibernateUtil.factory.fromTransaction(session -> {
                session.remove(finalGroupProhibited);
                return 0;
            });
        } catch (Exception e) {
            l.error("出错啦~", e);
            subject.sendMessage("违禁词 " + MiraiCode.deserializeMiraiCode(groupProhibited.getTrigger()) + " 删除失败");
            return;
        }

        subject.sendMessage("违禁词 " + MiraiCode.deserializeMiraiCode(groupProhibited.getTrigger()) + " 删除成功");

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
                listMap.get(bot).put(scope, new ArrayList<>() {{
                    add(entity);
                }});
                continue;
            }
            listMap.get(bot).get(scope).add(entity);
        }
        return listMap;
    }


}