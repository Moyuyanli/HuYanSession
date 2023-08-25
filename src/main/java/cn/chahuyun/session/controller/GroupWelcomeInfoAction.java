package cn.chahuyun.session.controller;

import cn.chahuyun.session.entity.GroupWelcomeInfo;
import cn.chahuyun.session.entity.Scope;
import cn.chahuyun.session.entity.WelcomeMessage;
import cn.chahuyun.session.utils.HibernateUtil;
import cn.chahuyun.session.utils.ListUtil;
import cn.chahuyun.session.utils.ScopeUtil;
import cn.chahuyun.session.utils.ShareUtils;
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import static cn.chahuyun.session.HuYanSession.LOGGER;
import static cn.chahuyun.session.utils.ShareUtils.DYNAMIC_MESSAGE_PATTERN;

/**
 * GroupWelcomeInfoUtil
 * 群欢迎消息工具
 *
 * @author Moyuyanli
 * @date 2022/8/22 9:27
 */
public class GroupWelcomeInfoAction {


    /**
     * 轮询次数递增
     *
     * @param welcomeInfo 欢迎消息
     * @author Moyuyanli
     * @date 2022/8/22 16:32
     */
    public static void increase(GroupWelcomeInfo welcomeInfo) {
        welcomeInfo.setPollingNumber(welcomeInfo.getPollingNumber() + 1);
        HibernateUtil.factory.fromTransaction(session -> session.merge(welcomeInfo));
    }

    public void addGroupWelcomeInfo(MessageEvent event) {
        Contact subject = event.getSubject();
        User user = event.getSender();
        Bot bot = event.getBot();

        List<WelcomeMessage> messageList = new ArrayList<>();

        while (true) {
            subject.sendMessage("请输入欢迎消息:");

            MessageEvent nextMessageEventFromUser = ShareUtils.getNextMessageEventFromUser(user);
            if (ShareUtils.isQuit(nextMessageEventFromUser)) {
                return;
            }

            MessageChain message = nextMessageEventFromUser.getMessage();
            String value = message.serializeToMiraiCode();

            if ("!!".equals(value) || "！！".equals(value)) {
                break;
            }

            int type = 0;

            //判断是否存在动态消息
            Pattern compile = Pattern.compile(DYNAMIC_MESSAGE_PATTERN);
            if (compile.matcher(value).find()) {
                type = 1;
            }
            //判断消息是否是转发消息或音频消息
            if (message.contains(ForwardMessage.Key) || message.contains(Audio.Key)) {
                type = 2;
                value = MessageChain.serializeToJsonString(message);
            }
            WelcomeMessage welcomeMessage = new WelcomeMessage(bot.getId(), type, value);
            messageList.add(welcomeMessage);
        }
        //随机标识
        int randomMark = (int) (Math.random() * 100);
        //是否随机发送
        boolean random = false;

        subject.sendMessage("请发送参数(一次发送，多参数中间隔开):");
        MessageEvent nextParamMessageEventFromUser = ShareUtils.getNextMessageEventFromUser(user);
        if (ShareUtils.isQuit(nextParamMessageEventFromUser)) {
            return;
        }
        String param = nextParamMessageEventFromUser.getMessage().serializeToMiraiCode();

        Scope scope = new Scope(bot.getId(), "当前", false, false, subject.getId(), 0);

        //解析参数
        String[] split = param.split(" +");
        for (String s : split) {
            switch (s) {
                case "sj":
                case "随机":
                    random = true;
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

        GroupWelcomeInfo groupWelcomeInfo = new GroupWelcomeInfo(bot.getId(), random, randomMark, scope);
        //保存或更新
        groupWelcomeInfo.setWelcomeMessages(messageList);
        if (groupWelcomeInfo.merge()) {
            subject.sendMessage("欢迎词保存成功!");
        } else {
            subject.sendMessage("欢迎词保存失败!");
        }
    }

    /**
     * 查询群欢迎词
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/8/23 9:00
     */
    public void queryGroupWelcomeInfo(MessageEvent event) {
        Contact subject = event.getSubject();
        Bot bot = event.getBot();

        List<GroupWelcomeInfo> welcomeInfoList = null;
        try {
            welcomeInfoList = HibernateUtil.factory.fromTransaction(session -> {
                HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
                JpaCriteriaQuery<GroupWelcomeInfo> query = builder.createQuery(GroupWelcomeInfo.class);
                JpaRoot<GroupWelcomeInfo> from = query.from(GroupWelcomeInfo.class);

                query.select(from);
                query.where(builder.equal(from.get("bot"), bot.getId()));
                List<GroupWelcomeInfo> list = session.createQuery(query).list();
                for (GroupWelcomeInfo info : list) {
                    if (info.getScope() == null) {
                        info.setScope(Objects.requireNonNull(ScopeUtil.getScope(info.getScopeMark())));
                    }
                }
                return list;
            });
        } catch (Exception e) {
            LOGGER.error("出错啦!", e);
        }

        if (welcomeInfoList == null || welcomeInfoList.isEmpty()) {
            subject.sendMessage("欢迎词为空");
            return;
        }

        ForwardMessageBuilder builder = new ForwardMessageBuilder(subject);
        builder.add(bot, new PlainText("以下本是bot所有群欢迎词↓"));
        for (GroupWelcomeInfo welcomeInfo : welcomeInfoList) {
            List<WelcomeMessage> welcomeMessages = welcomeInfo.getWelcomeMessages();
            Scope scope = welcomeInfo.getScope();
            MessageChainBuilder messages = new MessageChainBuilder();
            messages.add(new PlainText("欢迎词集合编号:" + welcomeInfo.getRandomMark()));
            messages.add(new PlainText("\n作用方式:" + scope.getScopeName()));
            if (scope.isGroupInfo()) {
                messages.add(new PlainText("\n群组编号:" + scope.getListId()));
            } else if (!scope.isGlobal()) {
                messages.add(new PlainText("\n群号:" + scope.getGroupNumber()));
            }
            messages.add(new PlainText("\n触发方式:" + (welcomeInfo.isRandom() ? "随机" : "轮询")));
            builder.add(bot, messages.build());
            ForwardMessageBuilder forwardMsgBuilder = new ForwardMessageBuilder(subject);
            for (WelcomeMessage welcomeMessage : welcomeMessages) {
                forwardMsgBuilder.add(bot, new PlainText("id:" + welcomeMessage.getId() + "\n==>").plus(MiraiCode.deserializeMiraiCode(welcomeMessage.getWelcomeMessage())));
            }
            builder.add(bot, forwardMsgBuilder.build());
        }
        subject.sendMessage(builder.build());
    }

    /**
     * 删除欢迎词
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/8/23 9:37
     */
    public void deleteGroupWelcomeInfo(MessageEvent event) {
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        Bot bot = event.getBot();

        String[] split = code.split("[:：]")[1].split(" +");
        int key = Integer.parseInt(split[0]);
        int toKey = 0;
        if (split.length > 1) {
            toKey = Integer.parseInt(split[1]);
        }
        GroupWelcomeInfo groupWelcomeInfo = null;
        try {
            groupWelcomeInfo = HibernateUtil.factory.fromTransaction(session -> {
                HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
                JpaCriteriaQuery<GroupWelcomeInfo> query = builder.createQuery(GroupWelcomeInfo.class);
                JpaRoot<GroupWelcomeInfo> from = query.from(GroupWelcomeInfo.class);

                query.select(from);
                query.where(builder.equal(from.get("id"), key));
                return session.createQuery(query).getSingleResult();
            });
        } catch (Exception e) {
            LOGGER.error("删除欢迎词查询出错!", e);
        }

        if (groupWelcomeInfo == null) {
            subject.sendMessage("没有要删除的欢迎词!");
            return;
        }
        if (toKey != 0) {
            List<WelcomeMessage> welcomeMessages = groupWelcomeInfo.getWelcomeMessages();
            int finalToKey = toKey;
            Optional<WelcomeMessage> first = welcomeMessages.stream().filter(it -> it.getId() == finalToKey).findFirst();
            first.ifPresent(it -> {
                if (it.remove()) {
                    subject.sendMessage("欢迎词删除成功!");
                } else {
                    subject.sendMessage("欢迎词删除失败!");
                }
            });
        } else {
            if (groupWelcomeInfo.remove()) {
                subject.sendMessage("欢迎词集合删除成功!");
            } else {
                subject.sendMessage("欢迎词集合删除失败!");
            }
        }
    }
}

