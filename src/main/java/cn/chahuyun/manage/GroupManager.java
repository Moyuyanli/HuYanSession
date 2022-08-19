package cn.chahuyun.manage;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.data.StaticData;
import cn.chahuyun.entity.BlackHouse;
import cn.chahuyun.entity.GroupProhibited;
import cn.chahuyun.entity.Scope;
import cn.chahuyun.enums.Mate;
import cn.chahuyun.utils.BlackHouseUtil;
import cn.chahuyun.utils.ShareUtils;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.MiraiLogger;
import xyz.cssxsh.mirai.hibernate.MiraiHibernateRecorder;
import xyz.cssxsh.mirai.hibernate.entry.MessageRecord;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * GroupManager
 * 群管理类
 *
 * @author Moyuyanli
 * @date 2022/8/15 12:52
 */
public class GroupManager {


    private final static MiraiLogger l = HuYanSession.INSTANCE.getLogger();


    /**
     * @param event 消息事件
     * @description 解禁言
     * @author Moyuyanli
     * @date 2022/6/21 16:44
     */
    public static void prohibit(MessageEvent event) {
        Contact subject = event.getSubject();
        String code = event.getMessage().serializeToMiraiCode();
        Bot bot = event.getBot();
        //判断bot的权限
        Group group = (Group) subject;
        boolean botIsAdmin = group.getBotPermission() == MemberPermission.MEMBER;
        if (botIsAdmin) {
            subject.sendMessage("人家还不是管理员哦~");
            return;
        }

        //获取群友对象
        Long qq = null;
        for (SingleMessage s : event.getMessage()) {
            if (s instanceof At) {
                qq = ((At) s).getTarget();
            }
        }
        if (qq == null) {
            subject.sendMessage("禁言失败，没有这个人呢！");
            return;
        }

        NormalMember member = bot.getGroup(event.getSubject().getId()).get(qq);
        //获取参数
        String[] split = code.split(" ");
        String param = split[1];
        //分解参数
        String type = param.substring(param.length() - 1);
        int timeParam = Integer.parseInt(param.substring(0, param.length() - 1));
        if (timeParam == 0) {
            assert member != null;
            member.unmute();
            subject.sendMessage("解禁成功！");
            return;
        }
        //禁言时间计算
        int time = 0;
        MessageChainBuilder messages = new MessageChainBuilder().append("禁言成功!");
        switch (type) {
            case "s":
                time = timeParam;
                messages.append("禁言:" + timeParam + "秒");
                break;
            case "m":
                time = timeParam * 60;
                messages.append("禁言:" + timeParam + "分钟");
                break;
            case "h":
                time = timeParam * 60 * 60;
                messages.append("禁言:" + timeParam + "小时");
                break;
            case "d":
                time = timeParam * 60 * 60 * 24;
                messages.append("禁言:" + timeParam + "天");
                break;
            default:
                break;
        }
        //禁言
        assert member != null;
        try {
            member.mute(time);
        } catch (Exception e) {
            if (e instanceof PermissionDeniedException) {
                subject.sendMessage("禁言失败,你机器居然不是管理员???");
            } else {
                subject.sendMessage("出错啦~");
                l.error("出错啦~", e);
            }
        }
        subject.sendMessage(messages.build());
    }

    /**
     * 三种形式撤回消息
     * ！recall 撤回上一条
     * ！recall 5 撤回不带本条的前 5 条消息
     * ！recall 0-5 撤回从本条消息开始算起的总共6条消息
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/8/15 17:15
     */
    public static void recall(MessageEvent event) {
        //!recall 0? - 0?
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();

        //只有是群的情况下才会触发
        Group group;
        if (subject instanceof Group) {
            group = (Group) subject;
        } else {
            return;
        }

        //如果不是管理员，就直接不反应
        boolean botIsAdmin = group.getBotPermission() == MemberPermission.MEMBER;
        if (botIsAdmin) {
            return;
        }

        //拿到所有本群的所有消息
        List<MessageRecord> records = MiraiHibernateRecorder.INSTANCE.get(group).collect(Collectors.toList());

        //识别参数并分割消息
        String[] split = code.split(" +");
        if (split.length == 1) {
            records = records.subList(1, 2);
        } else if (split.length == 2) {
            String string = split[1];
            if (string.contains("-") || string.contains("~")) {
                String[] strings = string.split("[~-]");
                int start = Integer.parseInt(strings[0]);
                int end = Integer.parseInt(strings[1]);
                l.info("s-" + start + " e-" + end);
                records = records.subList(start, end);
            } else {
                int end = Integer.parseInt(split[1]);
                records = records.subList(1, end);
            }
        }

        //循环撤回
        for (MessageRecord record : records) {
            try {
                MessageSource.recall(record.toMessageSource());
            } catch (PermissionDeniedException e) {
                l.warning("消息撤回冲突-无权操作");
            } catch (IllegalStateException e) {
                l.warning("消息撤回冲突-已被撤回 或 消息未找到");
            } catch (Exception e) {
                subject.sendMessage("消息撤回失败!");
                l.error("出错啦~", e);
            }
        }
    }

    /**
     * 判断是否是违禁词
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/8/16 17:26
     */
    public static boolean isProhibited(MessageEvent event) throws IOException {
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        User sender = event.getSender();
        Bot bot = event.getBot();

        if (!(subject instanceof Group)) {
            return false;
        }

        GroupProhibited groupProhibited = null;

        Map<Scope, List<GroupProhibited>> prohibitedMap = StaticData.getProhibitedMap(bot);

        for (Scope scope : prohibitedMap.keySet()) {
            if (ShareUtils.mateScope(event, scope)) {
                List<GroupProhibited> groupProhibiteds = prohibitedMap.get(scope);
                for (GroupProhibited prohibited : groupProhibiteds) {
                    int mateType = prohibited.getMateType();
                    Mate mate = Mate.VAGUE;
                    String trigger = prohibited.getTrigger();
                    if (mateType == 1) {
                        mate = Mate.ACCURATE;
                    } else if (mateType == 3) {
                        mate = Mate.START;
                    } else if (mateType == 4) {
                        mate = Mate.END;
                    }
                    if (ShareUtils.mateMate(code, mate, trigger)) {
                        groupProhibited = prohibited;
                    }
                }
            }
        }

        if (groupProhibited == null) {
            return false;
        }

        //撤回
        if (groupProhibited.isWithdraw()) {
            try {
                MessageSource.recall(event.getMessage());
            } catch (PermissionDeniedException e) {
                l.warning("违禁词撤回失败-权限不足");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //禁言
        if (groupProhibited.isProhibit()) {
            Member member = (Member) sender;
            if (groupProhibited.getProhibitTime() > 0) {
                member.mute(groupProhibited.getProhibitTime());
            }
        }

        if (groupProhibited.isAccumulate()) {
            BlackHouse blackHouse = BlackHouseUtil.getBlackHouse(bot, sender.getId());
            if (blackHouse == null) {
                blackHouse = new BlackHouse(bot.getId(), sender.getId(), groupProhibited.getId(), 1);
            } else {
                blackHouse.setNumber(blackHouse.getNumber()+1);
            }
            if (blackHouse.getNumber() >= groupProhibited.getAccumulateNumber()) {
                subject.sendMessage(sender.getNick() + "已经到达违禁词触发次数，将被踢出本群!");
                bot.getGroup(subject.getId()).get(sender.getId()).kick(sender.getNick()+"已经到达违禁词触发次数，将被踢出本群！");
                return true;
            }
            subject.sendMessage(MessageUtils.newChain()
                    .plus(new At(sender.getId()))
                    .plus(new PlainText("你已经违规 " + blackHouse.getNumber() + " 次，当违规 " + groupProhibited.getAccumulateNumber() + " 次就会被踢出!")));
            BlackHouseUtil.saveOrUpdate(blackHouse);
        }
        //回复消息
        MessageChain messages = ShareUtils.parseMessageParameter(event, groupProhibited.getReply(), groupProhibited);
        if (messages != null) {
            subject.sendMessage(messages);
        }

        return true;
    }


}