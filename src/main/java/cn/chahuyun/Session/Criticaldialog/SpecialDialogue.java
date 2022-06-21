package cn.chahuyun.Session.Criticaldialog;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.config.PowerConfig;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.utils.MiraiLogger;

/**
 * PuDialogue
 *
 * @author Zhangjiaxing
 * @description
 * @date 2022/6/16 10:23
 */
public class SpecialDialogue {

    public static final SpecialDialogue INSTANCE = new SpecialDialogue();

    private MiraiLogger l = HuYanSession.INSTANCE.getLogger();


    /**
     * @description 噗~ -> 噗~
     * @author zhangjiaxing
     * @param messageEvent 消息事件
     * @date 2022/6/16 14:20
     */
    public  void sessionPu(MessageEvent messageEvent) {
        try {
            messageEvent.getSubject().sendMessage("噗~");
        } catch (Exception e) {
            HuYanSession.INSTANCE.getLogger().error(e.getMessage());
        }
    }

    /**
     * @description 斗地主帮助
     * @author zhangjiaxing
     * @param event 消息事件
     * @date 2022/6/18 0:09
     * @return void
     */
    public  void sessionDou(MessageEvent event) {
        Contact subject = event.getSubject();
        MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
        messageChainBuilder.append(
                        "欢迎查看斗地主帮助!\n"+
                        "在有权限的群内发送'开桌'即可创建斗地主对局.\n"+
                        "需求自定义底分的对局可发送'开桌 [底分]'即\n"+
                        "可创建自定义底分对局.\n"+
                        "对局创建后，发送'上桌'即可上桌，当人满3人就\n" +
                        "可以发送'发牌'让机器人给你们发牌.\n" +
                        "在对局未开始之前上桌可以发送'下桌'下桌.\n" +
                        "游戏开始后进入抢地主，可发送'抢','抢地主'\n" +
                        "或'不抢'进行地主的博弈.\n" +
                        "进入出牌阶段，发送'！[牌]'即可出牌.\n" +
                        "当你要不起时可以发送'过','要不起','不要'来跳过.\n" +
                        "在任何阶段，管理员可以发送'掀桌'结束对局.\n" +
                        "指令帮助.\n" +
                        "#d beg 领取每日500豆\n" +
                        "#d me 查看胜率\n" +
                        "以上就是斗地主帮助，祝你游戏愉快!");
        subject.sendMessage(messageChainBuilder.build());
    }


    /**
     * @description 帮助
     * @author zhangjiaxing
     * @param event 消息事件
     * @date 2022/6/20 20:53
     * @return void
     */
    public void sessionHelp(MessageEvent event) {
        Contact subject = event.getSubject();
        MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
        messageChainBuilder.append("壶言小帮手(*￣︶￣)~\n")
                .append("学习请发送：\n")
                .append("学习 触发词 回复内容 [精准|模糊|头部|结尾][当前|全局]\n")
                .append("附加参数默认第一个，自定义加上就行！[全局]只有我的哦秀金撒嘛能用！\n")
                .append("查询就是查询，删除就是删除 [关键词]\n")
                .append("其他帮助请回复详细帮助~~");
        subject.sendMessage(messageChainBuilder.build());
        event.intercept();
        //判断该用户的下一条消息
        GlobalEventChannel.INSTANCE.filterIsInstance(MessageEvent.class)
                .filter(at -> at.getSender().getId() == event.getSender().getId())
                .subscribeOnce(GroupMessageEvent.class, this::sessionHelpInfo);
    }

    /**
     * @description 详细帮助
     * @author zhangjiaxing
     * @param event 消息事件
     * @date 2022/6/20 20:53
     * @return void
     */
    private void sessionHelpInfo(MessageEvent event) {
        String string = event.getMessage().contentToString();
        Contact subject = event.getSubject();

        if (!string.equals("详细帮助")) {
            event.intercept();
            return;
        }

        ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder(event.getSubject());
        MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
        messageChainBuilder.append("--详细帮助--\n")
                .append("以下为基础指令帮助↓\n")
                .append("()为必填 []为选填 |为或 \n\n")
                .append("学习指令全格式:\n")
                .append("学习 (触发内容<任意内容，图片也可以>) (回复内容<同理>) [精准|模糊|头部|结尾] [当前|全局]\n\n")
                .append("查询指令全格式:\n")
                .append("查询<后面的空格和参数可带可不带> [触发内容]\n\n")
                .append("删除指令全格式:\n")
                .append("删除 (触发内容<必填>)");

        MessageChainBuilder powermcb = new MessageChainBuilder();
        powermcb.append("以下为高级指令帮助↓\n")
                .append("()为必填 []为选填 |为或 \n\n")
                .append("添加权限(+)/删除权限(-):\n\n")
                .append("(+|-) (@群员) (session|admin|group|all)\n\n")
                .append("session(会话操作权限)\n")
                .append("admin(权限操作权限)\n")
                .append("group(群操作权限)\n");
        MessageChainBuilder end = new MessageChainBuilder();
        end.append("更多插件使用帮助请查看↓\n")
           .append("https://mirai.mamoe.net/topic/1310/%E5%A3%B6%E8%A8%80-%E4%B8%80%E6%AC%BE%E8%87%AA%E5%AE%9A%E4%B9%89%E6%B6%88%E6%81%AF%E5%9B%9E%E5%A4%8D%E6%8F%92%E4%BB%B6");

        forwardMessageBuilder.add(event.getBot(), messageChainBuilder.build());
        forwardMessageBuilder.add(event.getBot(), powermcb.build());
        //是否显示链接
        if (PowerConfig.INSTANCE.getLinkSwitch().get()) {
            forwardMessageBuilder.add(event.getBot(), end.build());
        }

        subject.sendMessage(forwardMessageBuilder.build());
        event.intercept();
    }

}