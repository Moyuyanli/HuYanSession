package cn.chahuyun.dialogue;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.Image;

import java.io.IOException;
import java.net.URL;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :各种类型的消息对话
 * @Date 2022/7/10 23:31
 */
public class Dialogue {

    public static final Dialogue INSTANCE = new Dialogue();

    public void dialogue(MessageEvent event) {
        Image image = null;
        try {
            image = Contact.uploadImage(event.getSubject(), new URL("").openStream());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        event.getSubject().sendMessage(image);
    }

}
