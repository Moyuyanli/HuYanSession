package cn.chahuyun.Session.Criticaldialog;

import cn.chahuyun.HuYanSession;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChainBuilder;

/**
 * PuDialogue
 *
 * @author Zhangjiaxing
 * @description
 * @date 2022/6/16 10:23
 */
public class SpecialDialogue {

    public static final SpecialDialogue INSTANCE = new SpecialDialogue();

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


}