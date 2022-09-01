package cn.chahuyun.controller;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.config.ConfigData;
import cn.chahuyun.data.StaticData;
import cn.chahuyun.entity.Power;
import cn.chahuyun.utils.HibernateUtil;
import kotlin.coroutines.EmptyCoroutineContext;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.event.ConcurrencyKind;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.EventPriority;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.*;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaRoot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.chahuyun.HuYanSession.log;

/**
 * 权限工具
 *
 * @author Moyuyanli
 * @Description : 权限操作
 * @Date 2022/8/14 17:29
 */
public class PowerAction {


    /**
     * 加载或更新权限数据!
     *
     * @param type true 加载 false 更新
     * @author Moyuyanli
     * @date 2022/8/14 19:58
     */
    public static void init(boolean type) {

        List<Power> powerList = HibernateUtil.factory.fromTransaction(session -> {

            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<Power> query = builder.createQuery(Power.class);
            JpaRoot<Power> from = query.from(Power.class);

            query.select(from);

            return session.createQuery(query).list();
        });

        Map<Long, Map<String, Power>> map = parseList(powerList);
        StaticData.setPowerMap(map);

        if (ConfigData.INSTANCE.getDebugSwitch() && type) {
            log.info("数据库权限信息初始化成功!");
            return;
        }
        if (ConfigData.INSTANCE.getDebugSwitch()) {
            log.info("权限信息更新成功!");
        }

    }

    /**
     * 修改权限信息
     *
     * @param event 消息事件
     * @param type  true 添加 false 删除
     * @author Moyuyanli
     * @date 2022/8/14 19:58
     */
    public void addOrUpdatePower(MessageEvent event, boolean type) {
        //+@\d+ \S+
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        Bot bot = event.getBot();

        long user = 0;
        long group = subject instanceof Group ? subject.getId() : 0;
        MessageChain message = event.getMessage();
        for (SingleMessage singleMessage : message) {
            if (singleMessage instanceof At) {
                user = ((At) singleMessage).getTarget();
            }
        }

        String mark = group + "." + user;

        Map<String, Power> powerMap = StaticData.getPowerMap(bot);
        Power power = null;
        if (powerMap == null || powerMap.isEmpty() || !powerMap.containsKey(mark)) {
            power = new Power(bot.getId(), group, user);
        } else if (powerMap.containsKey(mark)) {
            power = powerMap.get(mark);
        }

        String value = code.split(" +")[1];
        switch (value) {
            case "admin":
                power.setAdmin(type);
                break;
            case "list":
                power.setGroupList(type);
                break;
            case "session":
                power.setSession(type);
                break;
            case "sessionX":
            case "sessionx":
                power.setSessionX(type);
                break;
            case "sessionDct":
            case "sessiondct":
                power.setSessionDct(type);
                break;
            case "ds":
                power.setDs(type);
                break;
            case "dscz":
                power.setDscz(type);
                break;
            case "group":
                power.setGroupManage(type);
                break;
            case "grouphyc":
            case "groupHyc":
                power.setGroupHyc(type);
                break;
            case "groupjy":
            case "groupJy":
                power.setGroupJy(type);
                break;
            case "groupHmd":
            case "grouphmd":
                power.setGroupHmd(type);
                break;
            case "groupch":
            case "groupCh":
                power.setGroupCh(type);
                break;
            case "groupTr":
            case "grouptr":
                power.setGroupTr(type);
                break;
            case "all":
                if (type) {
                    power.setAll();
                }
                break;
            default:
                subject.sendMessage("未识别权限！");
                return;
        }

        NormalMember friend = bot.getGroup(group).get(user);

        if (!type && value.equals("all")) {
            Power finalPower = power;
            HibernateUtil.factory.fromTransaction(session -> {
                session.remove(finalPower);
                init(false);
                return 0;
            });
            subject.sendMessage("清除用户 " + friend.getRemark() + " 所有权限成功！");
            init(false);
            return;
        }

        Power updatePower = power;
        HibernateUtil.factory.fromTransaction(session -> {
            session.merge(updatePower);
            return 0;
        });
        if (type) {
            subject.sendMessage("添加用户 " + friend.getRemark() + " 权限 " + (value.equals("all") ? "全部" : value) + " 成功!");
        } else {
            subject.sendMessage("删除用户 " + friend.getRemark() + " 权限" + (value.equals("all") ? "全部" : value) + " 成功!");
        }
        init(false);
    }

    /**
     * 查询权限信息
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/8/14 22:24
     */
    public void queryPower(MessageEvent event) {
        //power:id?
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        Bot bot = event.getBot();

        init(false);

        String[] splits = code.split(" +");
        if (splits.length == 2) {
            String split = splits[1];
            //带参数 并且参数是 all
            if (split.equals("all")) {
                int pageNo = 1;
                Map<String, Power> powerMap = StaticData.getPowerMap(bot);
                if (powerMap.size() == 0) {
                    subject.sendMessage("目前没有权限信息");
                    return;
                }
                paginationQueryAll(event, pageNo);
                return;
            }
            //todo 先做一个识别全部吧  剩下的后面再补
            //一个识别群  一个识别个人
        }


    }

    //==========================================================================================

    /**
     * 解析参数
     *
     * @param powerList 权限集合
     * @return java.util.Map<java.lang.Long, java.util.Map < java.lang.String, cn.chahuyun.entity.Power>>
     * @author Moyuyanli
     * @date 2022/8/14 17:36
     */
    private static Map<Long, Map<String, Power>> parseList(List<Power> powerList) {

        if (powerList == null || powerList.isEmpty()) {
            return null;
        }
        Map<Long, Map<String, Power>> listMap = new HashMap<>();

        for (Power power : powerList) {
            long bot = power.getBot();
            //用户识别
            String key = power.getGroupId() + "." + power.getQq();

            if (!listMap.containsKey(bot)) {
                listMap.put(bot, new HashMap<>() {{
                    put(key, power);
                }});
                continue;
            }
            if (!listMap.get(bot).containsKey(key)) {
                listMap.get(bot).put(key, power);
            }
        }
        return listMap;

    }

    /**
     * 分也查询所有用户权限
     * 整个类的精髓所在地
     * todo 多个方法的案例所在地
     *
     * @param event  消息事件
     * @param pageNo 当前页数
     * @author Moyuyanli
     * @date 2022/8/14 22:23
     */
    private void paginationQueryAll(MessageEvent event, int pageNo) {
        Contact subject = event.getSubject();
        User user = event.getSender();
        Bot bot = event.getBot();
        List<Power> powerList = new ArrayList<>(StaticData.getPowerMap(bot).values());
        //排序
        powerList.sort((a, b) -> {
            if (a.getGroupId() >= b.getGroupId()) {
                return -1;
            } else {
                return 1;
            }
        });
        int pageTotal = powerList.size() % 10 == 0 ? powerList.size() / 10 : powerList.size() / 10 + 1;
        int pageMax = pageNo * 10;
        powerList = powerList.subList(pageMax - 10, Math.min(powerList.size(), pageMax));

        ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder(subject);
        forwardMessageBuilder.add(bot, singleMessages -> {
            singleMessages.add("以下是本bot的所有权限信息↓");
            return null;
        });
        long owner = ConfigData.INSTANCE.getOwner();
        Friend friend = bot.getFriend(owner);
        String ownerString = "主人:";
        if (friend != null) {
            ownerString += friend.getRemark();
        }
        ownerString += "(" + owner + ")";
        String finalOwnerString = ownerString;
        //下面这一堆方法，就为了拼一个好看一点的列表出来...
        forwardMessageBuilder.add(bot, singleMessages -> {
            singleMessages.add(finalOwnerString);
            return null;
        });
        for (Power power : powerList) {
            forwardMessageBuilder.add(bot, singleMessages -> {
                Group group = bot.getGroup(power.getGroupId());
                MessageChainBuilder builder = new MessageChainBuilder();
                String groupPowerString = "";
                String userPowerString = "";
                if (group == null) {
                    groupPowerString = "未知群(" + power.getGroupId() + ")";
                    userPowerString = "未知用户(" + power.getQq() + ")";
                    builder.append(userPowerString).append("\n")
                            .append("所属群:").append(groupPowerString).append("\n");
                }
                NormalMember member = group.get(power.getQq());
                if (member == null) {
                    userPowerString = "未知用户(" + power.getQq() + ")";
                    builder.append(userPowerString).append("\n");
                } else {
                    builder.append(NormalMemberKt.getNameCardOrNick(member)).append("(").append(power.getQq() + "").append(")\n");
                    builder.append("所属群:").append(group.getName()).append("(").append(power.getGroupId() + "").append(")\n");
                }
                singleMessages.add(builder.build());
                return null;
            });
            forwardMessageBuilder.add(bot, new ForwardMessageBuilder(subject).add(bot, singleMessages -> {
                singleMessages.add(power.toString());
                return null;
            }).build());
        }

        forwardMessageBuilder.add(bot, singleMessages -> {
            singleMessages.add("当前页数:" + pageNo + "/总页数:" + pageTotal);
            return null;
        });
        subject.sendMessage(forwardMessageBuilder.build());

        //循环判断是否进行下一页的显示
        EventChannel<MessageEvent> channel = GlobalEventChannel.INSTANCE.parentScope(HuYanSession.INSTANCE)
                .filterIsInstance(MessageEvent.class)
                .filter(nextEvent -> nextEvent.getSender().getId() == user.getId());
        channel.subscribeOnce(MessageEvent.class, EmptyCoroutineContext.INSTANCE,
                ConcurrencyKind.LOCKED, EventPriority.HIGH, nextEvent -> {
                    String string = nextEvent.getMessage().contentToString();
                    if (string.equals("下一页")) {
                        if (pageNo + 1 <= pageTotal) {
                            paginationQueryAll(nextEvent, pageNo + 1);
                        }
                    } else if (string.equals("上一页")) {
                        if (pageNo - 1 > 0) {
                            paginationQueryAll(nextEvent, pageNo - 1);
                        }
                    }
                });
    }


}
