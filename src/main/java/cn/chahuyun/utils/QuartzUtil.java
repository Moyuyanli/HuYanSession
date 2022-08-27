package cn.chahuyun.utils;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.entity.ManySession;
import cn.chahuyun.entity.QuartzInfo;
import cn.chahuyun.entity.Scope;
import cn.chahuyun.manage.QuartzManager;
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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.chahuyun.utils.ShareUtils.DYNAMIC_MESSAGE_PATTERN;

/**
 * 定时器工具类
 *
 * @author Moyuyanli
 * @Date 2022/8/27 18:50
 */
public class QuartzUtil {

    private final static MiraiLogger l = HuYanSession.INSTANCE.getLogger();


    /**
     * 初始化定时任务
     *
     * @param type true 初始化
     * @author Moyuyanli
     * @date 2022/8/27 19:12
     */
    public static void init(boolean type) {
        List<QuartzInfo> quartzInfos = HibernateUtil.factory.fromTransaction(session -> {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<QuartzInfo> query = builder.createQuery(QuartzInfo.class);
            JpaRoot<QuartzInfo> from = query.from(QuartzInfo.class);

            query.select(from);
            return session.createQuery(query).list();
        });
        //加载时遍历定时器，对启动中的定时添加到调度器中
        for (QuartzInfo quartzInfo : quartzInfos) {
            if (!quartzInfo.isStatus()) {
                boolean addSchedulerJob = QuartzManager.addSchedulerJob(quartzInfo);
                if (addSchedulerJob) {
                    quartzInfo.setStatus(true);
                    updateQuartz(quartzInfo);
                }
            }
        }
        l.info("定时器加载成功!");
    }

    /**
     * 添加定时任务
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/8/27 20:06
     */
    public static void addQuartz(MessageEvent event) {
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
            String cronString = nextCronStringEvent.getMessage().serializeToMiraiCode();
            Matcher matcher = Pattern.compile("\\$cron\\(\\d( [\\d?*,]+){5}\\)").matcher(cronString);
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
            String nextCronStringSureCode = nextCronStringEvent.getMessage().serializeToMiraiCode();
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
        Scope scope = new Scope(bot.getId(), "当前", false, false, subject.getId(), -1);
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
                    scope = new Scope(bot.getId(), "全局", true, false, subject.getId(), -1);
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
        List<ManySession> manySessions = quartzInfo.getManySessions();

        boolean isQuit = false;
        while (!isQuit) {
            subject.sendMessage("请发送多词条回复消息:");
            MessageEvent nextEvent = ShareUtils.getNextMessageEventFromUser(user);
            if (ShareUtils.isQuit(nextEvent)) {
                return;
            }
            MessageChain nextEventMessage = nextEvent.getMessage();
            String miraiCode = nextEventMessage.serializeToMiraiCode();
            if (miraiCode.equals("！！") || miraiCode.equals("!!")) {
                isQuit = true;
            }
            if (miraiCode.equals("！") || miraiCode.equals("!")) {
                if (manySessions.size() > 2) {
                    manySessions.remove(manySessions.size() - 1);
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
            ManySession manySession = new ManySession(bot.getId(), miraiCode, dynamic, other, miraiCode);
            manySessions.add(manySession);
            subject.sendMessage("添加成功!");
        }

        if (saveQuartz(quartzInfo, scope)) {
            subject.sendMessage(String.format("定时任务 %s 添加成功!", name));
            return;
        }
        subject.sendMessage(String.format("定时任务 %s 添加失败!", name));
    }

    public static void queryQuartz(MessageEvent event) {
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
            l.error("出错拉~", e);
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
            List<ManySession> manySessions = value.getManySessions();
            MessageChainBuilder chainBuilder = new MessageChainBuilder();
            chainBuilder.add(new PlainText(String.format("定时器条编号:%d%n定时器名称:%s%n定时器频率: %s%n", value.getId(), value.getName(), value.getCronString())));
            new PlainText(String.format("定时器是否开启:%s%n", value.isStatus() ? "开启" : "关闭"));
            new PlainText(String.format("作用域:%s%n", value.getScope().getScopeName()));
            new PlainText(String.format("当前群是否触发:%b", ShareUtils.mateScope(event, value.getScope()) ? "是" : "否"));
            if (value.isPolling() && value.isRandom()) {
                chainBuilder.add(new PlainText("定时器回复内容:"));
                chainBuilder.add(MiraiCode.deserializeMiraiCode(value.getReply()));
            } else {
                chainBuilder.add(new PlainText(String.format("定时器回复方式:%s", value.isPolling() ? "轮询" : "随机")));
            }
            builder.add(bot, chainBuilder.build());
            if (value.isPolling() && value.isRandom()) {
                continue;
            }
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

    }

    /**
     * 删除定时器
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/8/27 22:29
     */
    public static void deleteQuartz(MessageEvent event) {
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
            l.error("出错拉~", e);
            subject.sendMessage("查询定时任务出错!");
            return;
        }
        if (quartzInfos == null || quartzInfos.isEmpty()) {
            subject.sendMessage("该定时任务不存在");
            return;
        }
        QuartzInfo quartzInfo = quartzInfos.get(0);
        try {
            HibernateUtil.factory.fromTransaction(session -> {
                session.remove(quartzInfo);
                return true;
            });
        } catch (Exception e) {
            subject.sendMessage("定时任务删除失败!");
            l.error("出错啦~", e);
            return;
        }
        subject.sendMessage("定时任务删除成功!");
    }


    public static void switchQuartz(MessageEvent event) {
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
            l.error("出错拉~", e);
            subject.sendMessage("查询定时任务出错!");
            return;
        }
        if (quartzInfos == null || quartzInfos.isEmpty()) {
            subject.sendMessage("该定时任务不存在");
            return;
        }
        QuartzInfo quartzInfo = quartzInfos.get(0);
        quartzInfo.setStatus(!quartzInfo.isStatus());
        if (QuartzManager.addSchedulerJob(quartzInfo)) {
            subject.sendMessage(String.format("定时器 %s %s成功!", quartzInfo.getName(), quartzInfo.isStatus() ? "开启" : "关闭"));
            updateQuartz(quartzInfo);
            return;
        }
        subject.sendMessage(String.format("定时器 %s %s失败!", quartzInfo.getName(), !quartzInfo.isStatus() ? "开启" : "关闭"));
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


    //=========================================================================

    /**
     * 保存定时器新的状态
     *
     * @param quartzInfo
     * @return void
     * @author Moyuyanli
     * @date 2022/8/27 20:33
     */
    private static void updateQuartz(QuartzInfo quartzInfo) {
        HibernateUtil.factory.fromTransaction(session -> session.merge(quartzInfo));
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
    private static boolean saveQuartz(QuartzInfo quartzInfo, Scope scope) {
        try {
            HibernateUtil.factory.fromTransaction(session -> {
                if (ScopeUtil.isScopeEmpty(scope)) {
                    session.persist(session);
                }
                session.persist(quartzInfo);
                return true;
            });
        } catch (Exception e) {
            l.error("出错啦~", e);
            return false;
        }
        return true;
    }


}
