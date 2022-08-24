package cn.chahuyun.utils;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.entity.Blacklist;
import cn.chahuyun.entity.Scope;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.SingleMessage;
import net.mamoe.mirai.utils.MiraiLogger;

import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :黑名单工具类
 * @Date 2022/8/24 18:24
 */
public class BlackListUtil {

    private final static MiraiLogger l = HuYanSession.INSTANCE.getLogger();

    /**
     * 添加黑名单
     *
     * @param event 消息事件
     * @author Moyuyanli
     * @date 2022/8/24 18:34
     */
    public static void addBlackList(MessageEvent event) throws ExecutionException, InterruptedException {
        //+hmd:at ...
        MessageChain message = event.getMessage();
        Contact subject = event.getSubject();
        User user = event.getSender();
        String code = message.serializeToMiraiCode();
        Bot bot = event.getBot();

        Group group = null;
        if (subject instanceof Group) {
            group = (Group) subject;
        }

        long userId = 0;
        for (SingleMessage sing : message) {
            if (sing instanceof At) {
                userId = ((At) sing).getTarget();
            }
        }

        String[] split = code.split(" +");
        String reason = "违反规则!";
        boolean kick = true;
        boolean prohibit = true;
        boolean withdraw = true;
        Scope scope = new Scope(bot.getId(), "当前", false, false, subject.getId(), 0);
        if (split.length > 1) {
            for (int i = 1; i < split.length; i++) {
                String s = split[i];
                switch (s) {
                    case "t":
                    case "tr":
                    case "kick":
                        kick = false;
                        break;
                    case "jy":
                        prohibit = false;
                        break;
                    case "ch":
                        withdraw = false;
                        break;
                    case "0":
                    case "全局":
                        scope.setScopeName("全局");
                        scope.setGlobal(true);
                        break;
                    case "%":
                        //获取下一次消息
                        subject.sendMessage("请输入封禁理由:");
                        reason = ShareUtils.getNextMessageEventFromUser(user).getMessage().serializeToMiraiCode();
                        break;
                    default:
                        if (Pattern.matches("gr\\d+", s)) {
                            scope.setScopeName("群组" + s.substring(1));
                            scope.setGroupInfo(true);
                            scope.setListId(Integer.parseInt(s.substring(1)));
                        }
                        break;
                }
            }
        }
        Blacklist blacklist = new Blacklist(bot.getId(), userId, reason, kick, prohibit, withdraw, scope);

        if (!saveBlackList(blacklist, scope)) {
            subject.sendMessage("黑名单添加失败!");
        }

        subject.sendMessage("黑名单添加成功!");
        if (group == null || group.getBotPermission() == MemberPermission.MEMBER) {
            return;
        }
        if (blacklist.isKick()) {
            try {
                group.get(userId).kick(reason);
                subject.sendMessage("检测到黑名单用户->" + userId + " 已踢出,理由:" + reason);
            } catch (Exception e) {
                l.error("出错啦~",e);
                subject.sendMessage("检测到黑名单用户->" + userId + " 踢出失败!");
            }
        } else {
            if (blacklist.isProhibit()) {
                group.get(userId).mute(999999999);
                subject.sendMessage("检测到黑名单用户->" + userId + " 已禁言,理由:" + reason);
            }
        }
    }

    /**
     * 保存黑名单
     *
     * @param blacklist 黑名单
     * @param scope 作用域
     * @return boolean
     * @author Moyuyanli
     * @date 2022/8/24 23:31
     */
    public static boolean saveBlackList(Blacklist blacklist,Scope scope) {
        try {
            HibernateUtil.factory.fromTransaction(session -> {
                //判断对应作用域是否存在
                if (!ScopeUtil.isScopeEmpty(scope)) {
                    //不存在则先添加作用域
                    session.persist(scope);
                }
                session.merge(blacklist);
                return null;
            });
        } catch (Exception e) {
            l.error("出错啦~",e);
            return false;
        }
        return true;
    }


}
