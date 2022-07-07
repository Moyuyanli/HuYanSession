package cn.chahuyun.sessionManager;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.entity.SessionDataBase;
import cn.chahuyun.files.PluginData;
import net.mamoe.mirai.contact.BotIsBeingMutedException;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.utils.MiraiLogger;

import java.util.ArrayList;
import java.util.Random;

/**
 * SessionDialogue
 *
 * @author Zhangjiaxing
 * @description 关键词对话的消息触发
 * @date 2022/6/16 14:36
 */
public class SessionDialogue {

    public static final SessionDialogue INSTANCE = new SessionDialogue();
    private MiraiLogger l = HuYanSession.INSTANCE.getLogger();

    /**
     * @description 传递消息监视和指定的关键词对话
     * @author zhangjiaxing
     * @param messageEvent 消息事件
     * @param sessionDataBase 对话类
     * @date 2022/6/16 15:17
     */
    public void session(MessageEvent messageEvent,SessionDataBase sessionDataBase) {
        Contact subject = messageEvent.getSubject();
        switch (sessionDataBase.getType()) {
            case 2:
                //轮询回复
                l.info("轮询回复");
                //获取key
                String key = sessionDataBase.getKey();
                //获取下一次轮询下标
                int i = PluginData.INSTANCE.addPollNum(key);
                ArrayList<String> values = sessionDataBase.getValues();
                //判断轮询坐标
                i = i % values.size();
                //发送
                try {
                    subject.sendMessage(MiraiCode.deserializeMiraiCode(values.get(i)));
                } catch (Exception e) {
                    if (e instanceof BotIsBeingMutedException) {
                        l.warning("消息发送失败!你的机器人被禁言了哦!");
                    } else {
                        l.warning("消息发送失败,检查一下你的机器人是不是被举报!");
                    }
                }
                break;
            case 3:
                l.info("随机回复");
                //随机回复
                //获取随机回复下标
                Random random = new Random();
                ArrayList<String> valueList = sessionDataBase.getValues();
                int nextInt = random.nextInt(valueList.size());
                //发送
                try {
                    subject.sendMessage(MiraiCode.deserializeMiraiCode(valueList.get(nextInt)));
                } catch (Exception e) {
                    if (e instanceof BotIsBeingMutedException) {
                        l.warning("消息发送失败!你的机器人被禁言了哦!");
                    } else {
                        l.warning("消息发送失败,检查一下你的机器人是不是被举报!");
                    }
                }
                break;
            default:
                try {
                    subject.sendMessage(MiraiCode.deserializeMiraiCode(sessionDataBase.getValue()));
                } catch (Exception e) {
                    if (e instanceof BotIsBeingMutedException) {
                        l.warning("消息发送失败!你的机器人被禁言了哦!");
                    } else {
                        l.warning("消息发送失败,检查一下你的机器人是不是被举报!");
                    }
                }
                break;
        }


    }

}