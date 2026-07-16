package cn.chahuyun.session.dialogue;

import cn.chahuyun.session.HuYanSession;
import cn.chahuyun.session.constant.Constant;
import cn.chahuyun.session.entity.Session;
import cn.chahuyun.session.utils.DynamicMessageUtil;
import cn.hutool.core.io.FileUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.SingleMessage;
import net.mamoe.mirai.utils.MiraiLogger;

/**
 * 对话消息
 *
 * @author Moyuyanli
 * @date 2023/8/31 9:46
 */
public class SessionDialogue extends AbstractDialogue {

    public MiraiLogger logger = HuYanSession.LOGGER;

    private Session session;

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    /**
     * 发送消息<p/>
     * 通过对其子类的类型判断实现消息发送
     *
     * @author Moyuyanli
     * @date 2023/8/31 9:39
     */
    @Override
    public void sendMessage() {
        MessageEvent event = getEvent();
        Contact subject = event.getSubject();
        MessageChain reply = MessageChain.deserializeFromMiraiCode(session.getReply(), subject);

        if (getSession().isDynamic()) {
            reply = DynamicMessageUtil.parseMessageParameter(event, getSession().getReply(), session);
            if (reply == null) {
                throw new RuntimeException("动态消息构造失败!");
            }
        }


        if (HuYanSession.CONFIG.getLocalCache()) {
            MessageChainBuilder chainBuilder = new MessageChainBuilder();
            //开启本地缓存，通过本地图片进行替换图片消息
            for (SingleMessage singleMessage : reply) {
                if (singleMessage.contentToString().equals(Constant.IMAGE_TYPE)) {
                    Image image = (Image) singleMessage;
                    String imageId = image.getImageId();
                    String imageAddress = Constant.IMG_PREFIX_ADDRESS +"/cache/"+ imageId;
                    Image imageMessage;
                    try {
                        imageMessage = Contact.uploadImage(subject, FileUtil.file(imageAddress));
                    } catch (Exception e) {
                        throw new RuntimeException("本地缓存图片加载错误!");
                    }
                    chainBuilder.append(imageMessage);
                } else {
                    chainBuilder.append(singleMessage);
                }
            }
            subject.sendMessage(chainBuilder.build());
        } else {
            subject.sendMessage(reply);
        }
    }
}
