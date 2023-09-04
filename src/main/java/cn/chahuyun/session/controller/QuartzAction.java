package cn.chahuyun.session.controller;

import cn.chahuyun.session.entity.QuartzInfo;
import cn.chahuyun.session.entity.QuartzSession;
import cn.chahuyun.session.entity.Scope;
import cn.chahuyun.session.job.TimingJob;
import cn.chahuyun.session.utils.HibernateUtil;
import cn.chahuyun.session.utils.ListUtil;
import cn.chahuyun.session.utils.ShareUtils;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.CronTask;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.*;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.chahuyun.session.HuYanSession.LOGGER;
import static cn.chahuyun.session.utils.ShareUtils.DYNAMIC_MESSAGE_PATTERN;

/**
 * 定时器工具类
 *
 * @author Moyuyanli
 * @Date 2022/8/27 18:50
 */
public class QuartzAction {


    /**
     * 初始化定时任务
     *
     * @author Moyuyanli
     * @date 2022/8/27 19:12
     */
    public static void init() {
        CronUtil.setMatchSecond(true);
        CronUtil.start(true);
        List<QuartzInfo> quartzInfos = HibernateUtil.factory.fromTransaction(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<QuartzInfo> query = builder.createQuery(QuartzInfo.class);
            JpaRoot<QuartzInfo> from = query.from(QuartzInfo.class);
            query.select(from);
            return session.createQuery(query).list();
        });
        //加载时遍历定时器，对启动中的定时添加到调度器中
        for (QuartzInfo quartzInfo : quartzInfos) {
            if (quartzInfo.isStatus()) {
                String id = quartzInfo.getId() + "." + quartzInfo.getName();
                CronTask timingJob = TimingJob.createTask(id, quartzInfo.getCronString());
                try {
                    CronUtil.schedule(id, quartzInfo.getCronString(), timingJob);
                } catch (Exception e) {
                    LOGGER.error("!!!∑(ﾟДﾟノ)ノ 添加定时任务出错:" + quartzInfo.getName());
                    continue;
                }
                quartzInfo.setStatus(true);
                updateQuartz(quartzInfo);
            }
        }
        LOGGER.info("定时器加载成功!");
    }

    /**
     * 通过id获取定时任务信息
     *
     * @param id 定时任务的信息类id
     * @return cn.chahuyun.session.entity.QuartzInfo
     * @author Moyuyanli
     * @date 2022/9/19 9:44
     */
    public static QuartzInfo getQuartzInfo(int id) {
        return HibernateUtil.factory.fromTransaction(session -> session.get(QuartzInfo.class, id));
    }

    /**
     * 轮询次数递增
     *
     * @param quartzInfo 定时任务
     * @author Moyuyanli
     * @date 2022/8/22 16:32
     */
    public static void increase(QuartzInfo quartzInfo) {
        quartzInfo.setPollingNumber(quartzInfo.getPollingNumber() + 1);
        HibernateUtil.factory.fromTransaction(session -> session.merge(quartzInfo));
    }

    /**
     * 保存定时器新的状态
     *
     * @author Moyuyanli
     * @date 2022/8/27 20:33
     */
    private static void updateQuartz(QuartzInfo quartzInfo) {
        HibernateUtil.factory.fromTransaction(session -> session.merge(quartzInfo));
    }

    /**
     * 添加定时任务
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/8/27 20:06
     */
    public void addQuartz(MessageEvent event) {
        User user = event.getSender();
        Contact subject = event.getSubject();
        Bot bot = event.getBot();
        //获取名称
        subject.sendMessage("请输入定时器名称:");
        MessageEvent nextNameEvent = ShareUtils.getNextMessageEventFromUser(user);
        if (ShareUtils.isQuit(nextNameEvent)) {
            return;
        }
        String name = nextNameEvent.getMessage().serializeToMiraiCode();
        //获取定时频率
        String cron = null;
        boolean cronSure = true;
        while (cronSure) {
            subject.sendMessage("请输入定时器频率(cron表达式):");
            MessageEvent nextCronStringEvent = ShareUtils.getNextMessageEventFromUser(user);
            if (ShareUtils.isQuit(nextCronStringEvent)) {
                return;
            }
            String cronString = nextCronStringEvent.getMessage().contentToString();
            Matcher matcher = Pattern.compile("^\\$cron\\(\\S+?( +\\S+){5}?\\)").matcher(cronString);
            if (matcher.find()) {
                cron = matcher.group().split("\\(")[1].split("\\)")[0];
                subject.sendMessage(String.format("识别到cron表达式-> %s <- 是否确认", cron));
            } else {
                subject.sendMessage("沒有识别到cron表达式!请重新输入");
                continue;
            }
            MessageEvent nextCronStringSure = ShareUtils.getNextMessageEventFromUser(user);
            if (ShareUtils.isQuit(nextCronStringSure)) {
                return;
            }
            String nextCronStringSureCode = nextCronStringSure.getMessage().serializeToMiraiCode();
            if (Pattern.matches("!|！|1|ok|确认", nextCronStringSureCode)) {
                cronSure = false;
            }
        }
        //获取参数
        subject.sendMessage("请输入定时器参数:(参数中间以空格隔开)");
        MessageEvent nextParamsEvent = ShareUtils.getNextMessageEventFromUser(user);
        if (ShareUtils.isQuit(nextParamsEvent)) {
            return;
        }

        boolean isPolling = false;
        boolean isRandom = false;
        Scope scope = new Scope(bot.getId(), "当前", false, false, subject.getId(), "null");
        //识别参数
        String[] params = nextParamsEvent.getMessage().serializeToMiraiCode().split(" +");
        for (String param : params) {
            switch (param) {
                case "lx":
                case "轮询":
                    isPolling = true;
                    break;
                case "sj":
                case "随机":
                    isRandom = true;
                    break;
                case "0":
                case "全局":
                    scope = new Scope(bot.getId(), "全局", true, false, subject.getId(), "null");
                    break;
                default:
                    String listPattern = "gr[\\dA-z]+|群组[\\dA-z]+";
                    if (Pattern.matches(listPattern, param)) {
                        String listId = param.substring(2);
                        if (ListUtil.isContainsList(bot, listId)) {
                            subject.sendMessage("该群组不存在!");
                            return;
                        }
                        scope = new Scope(bot.getId(), "群组" + listId, false, true, subject.getId(), listId);
                    }
                    break;
            }
        }
        //单条消息的定时任务
        if (!isPolling && !isRandom) {
            subject.sendMessage("请输入发送内容:");
            MessageEvent nextReplyEvent = ShareUtils.getNextMessageEventFromUser(user);
            if (ShareUtils.isQuit(nextReplyEvent)) {
                return;
            }
            MessageChain nextReplyEventMessage = nextReplyEvent.getMessage();
            String miraiCode = nextReplyEventMessage.serializeToMiraiCode();
            //判断是否存在动态消息
            boolean dynamic = false;
            Pattern compile = Pattern.compile(DYNAMIC_MESSAGE_PATTERN);
            if (compile.matcher(miraiCode).find() || compile.matcher(miraiCode).find()) {
                dynamic = true;
            }
            //判断消息是否是转发消息或音频消息
            boolean other = false;
            if (nextReplyEventMessage.contains(ForwardMessage.Key) || nextReplyEventMessage.contains(Audio.Key)) {
                dynamic = false;
                other = true;
                miraiCode = MessageChain.serializeToJsonString(nextReplyEventMessage);
            }

            QuartzInfo quartzInfo = new QuartzInfo(bot.getId(), name, cron, dynamic, other, miraiCode, false, false, scope);
            if (saveQuartz(quartzInfo, scope)) {
                subject.sendMessage(String.format("定时任务 %s 添加成功!", name));
                return;
            }
            subject.sendMessage(String.format("定时任务 %s 添加失败!", name));
            return;
        }

        //多条消息的定时任务
        QuartzInfo quartzInfo = new QuartzInfo(bot.getId(), name, cron, false, false, "miraiCode", isPolling, isRandom, scope);
        List<QuartzSession> quartzSessions = quartzInfo.getQuartzSession();

        boolean isQuit = false;
        while (!isQuit) {
            subject.sendMessage("请发送多词条回复消息:");
            MessageEvent nextEvent = ShareUtils.getNextMessageEventFromUser(user);
            if (ShareUtils.isQuit(nextEvent)) {
                return;
            }
            MessageChain nextEventMessage = nextEvent.getMessage();
            String miraiCode = nextEventMessage.serializeToMiraiCode();
            if ("！！".equals(miraiCode) || "!!".equals(miraiCode)) {
                isQuit = true;
            }
            if ("！".equals(miraiCode) || "!".equals(miraiCode)) {
                if (quartzSessions.size() > 2) {
                    quartzSessions.remove(quartzSessions.size() - 1);
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
            QuartzSession manySession = new QuartzSession(bot.getId(), dynamic, other, miraiCode);
            quartzSessions.add(manySession);
            if (isQuit) {
                break;
            }
            subject.sendMessage("添加成功!");
        }

        if (saveQuartz(quartzInfo, scope)) {
            subject.sendMessage(String.format("定时任务 %s 添加成功!", name));
            return;
        }
        subject.sendMessage(String.format("定时任务 %s 添加失败!", name));
    }

    /**
     * 查询定时任务
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/9/19 9:52
     */
    public void queryQuartz(MessageEvent event) {
        Bot bot = event.getBot();
        Contact subject = event.getSubject();

        List<QuartzInfo> quartzInfos;
        try {
            quartzInfos = HibernateUtil.factory.fromTransaction(session -> {
                HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
                JpaCriteriaQuery<QuartzInfo> query = builder.createQuery(QuartzInfo.class);
                JpaRoot<QuartzInfo> from = query.from(QuartzInfo.class);

                query.select(from);
                query.where(builder.equal(from.get("bot"), bot.getId()));
                return session.createQuery(query).list();
            });
        } catch (Exception e) {
            LOGGER.error("出错拉~");
            subject.sendMessage("查询定时任务出错!");
            return;
        }

        if (quartzInfos == null || quartzInfos.isEmpty()) {
            subject.sendMessage("定时任务为空！");
            return;
        }

        ForwardMessageBuilder builder = new ForwardMessageBuilder(subject);
        builder.add(bot, new PlainText("以下是所有多词条消息↓"));

        for (QuartzInfo value : quartzInfos) {
            List<QuartzSession> manySessions = value.getQuartzSession();
            MessageChainBuilder chainBuilder = new MessageChainBuilder();
            chainBuilder.add(new PlainText(String.format("定时器条编号:%d%n定时器名称:%s%n定时器频率: %s%n", value.getId(), value.getName(), value.getCronString())));
            chainBuilder.add(new PlainText(String.format("定时器是否开启:%s%n", value.isStatus() ? "开启" : "关闭")));
            chainBuilder.add(new PlainText(String.format("作用域:%s%n", value.getScope().getScopeName())));
            chainBuilder.add(new PlainText(String.format("当前群是否触发:%b%n", ShareUtils.mateScope(event, value.getScope()) ? "是" : "否")));
            if (!value.isPolling() && !value.isRandom()) {
                chainBuilder.add(new PlainText("定时器回复内容:"));
                chainBuilder.add(MiraiCode.deserializeMiraiCode(value.getReply()));
            } else {
                chainBuilder.add(new PlainText(String.format("定时器回复方式:%s", value.isPolling() ? "轮询" : "随机")));
                //todo 定时任务的消息查询
            }
            builder.add(bot, chainBuilder.build());
            if (value.isPolling() && value.isRandom()) {
                continue;
            }
            ForwardMessageBuilder messageBuilder = new ForwardMessageBuilder(subject);
            for (QuartzSession session : manySessions) {
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
     * 删除定时器
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/8/27 22:29
     */
    public void deleteQuartz(MessageEvent event) {
        //-ds:id
        Bot bot = event.getBot();
        Contact subject = event.getSubject();
        String code = event.getMessage().serializeToMiraiCode();

        String id = code.split("[:：]")[1];
        List<QuartzInfo> quartzInfos;
        try {
            quartzInfos = HibernateUtil.factory.fromTransaction(session -> {
                HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
                JpaCriteriaQuery<QuartzInfo> query = builder.createQuery(QuartzInfo.class);
                JpaRoot<QuartzInfo> from = query.from(QuartzInfo.class);

                query.select(from);
                query.where(builder.equal(from.get("bot"), bot.getId()));
                query.where(builder.equal(from.get("id"), id));
                return session.createQuery(query).list();
            });
        } catch (Exception e) {
            LOGGER.error("出错拉~", e);
            subject.sendMessage("查询定时任务出错!");
            return;
        }
        if (quartzInfos == null || quartzInfos.isEmpty()) {
            subject.sendMessage("该定时任务不存在");
            return;
        }
        QuartzInfo quartzInfo = quartzInfos.get(0);
        String quartzId = quartzInfo.getId() + "." + quartzInfo.getName();
        try {
            CronUtil.remove(quartzId);
        } catch (Exception ignored) {
        }
        try {
            HibernateUtil.factory.fromTransaction(session -> {
                session.remove(quartzInfo);
                return true;
            });
        } catch (Exception e) {
            subject.sendMessage("定时任务删除失败!");
            LOGGER.error("出错啦~", e);
            return;
        }
        subject.sendMessage("定时任务删除成功!");
    }


    //=========================================================================

    /**
     * 切换定时任务
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/9/19 9:52
     */
    public void switchQuartz(MessageEvent event) {
        //%ds:(id|name)
        Bot bot = event.getBot();
        Contact subject = event.getSubject();
        String code = event.getMessage().serializeToMiraiCode();

        String id = code.split("[:：]")[1];
        QuartzInfo quartzInfo;
        try {
            quartzInfo = HibernateUtil.factory.fromTransaction(session -> {
                try {
                    int i = Integer.parseInt(id);
                    return session.get(QuartzInfo.class, i);
                } catch (NumberFormatException e) {
                    HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
                    JpaCriteriaQuery<QuartzInfo> query = builder.createQuery(QuartzInfo.class);
                    JpaRoot<QuartzInfo> from = query.from(QuartzInfo.class);
                    query.select(from);
                    query.where(builder.equal(from.get("bot"), bot.getId()));
                    query.where(builder.equal(from.get("name"), id));
                    return session.createQuery(query).getSingleResult();
                }
            });
        } catch (Exception e) {
            LOGGER.error("出错拉~", e);
            subject.sendMessage("查询定时任务出错!");
            return;
        }
        if (quartzInfo == null) {
            subject.sendMessage("该定时任务不存在");
            return;
        }
        String timingId = quartzInfo.getId() + "." + quartzInfo.getName();
        CronTask timingJob = TimingJob.createTask(id, quartzInfo.getCronString());
        quartzInfo.setStatus(!quartzInfo.isStatus());
        if (quartzInfo.isStatus()) {
            CronUtil.schedule(timingId, quartzInfo.getCronString(), timingJob);
        } else {
            try {
                CronUtil.remove(timingId);
            } catch (Exception e) {
                LOGGER.warning("定时器未启用!");
                subject.sendMessage(String.format("定时器 %s %s失败!", quartzInfo.getName(), !quartzInfo.isStatus() ? "开启" : "关闭"));
                updateQuartz(quartzInfo);
                return;
            }
        }
        subject.sendMessage(String.format("定时器 %s %s成功!", quartzInfo.getName(), quartzInfo.isStatus() ? "开启" : "关闭"));
        updateQuartz(quartzInfo);
    }

    /**
     * 保存定时任务信息
     *
     * @param quartzInfo 定时任务信息
     * @param scope      作用域
     * @return boolean
     * @author Moyuyanli
     * @date 2022/8/27 21:53
     */
    private boolean saveQuartz(QuartzInfo quartzInfo, Scope scope) {
        try {
            HibernateUtil.factory.fromTransaction(session -> {
                session.persist(quartzInfo);
                return true;
            });
        } catch (Exception e) {
            LOGGER.error("出错啦~", e);
            return false;
        }
        return true;
    }


}
