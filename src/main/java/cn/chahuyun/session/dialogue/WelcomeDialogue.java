package cn.chahuyun.session.dialogue;

import cn.chahuyun.session.entity.GroupWelcomeInfo;
import cn.chahuyun.session.entity.WelcomeMessage;

/**
 * 欢迎词消息
 *
 * @author Moyuyanli
 * @date 2023/8/31 9:47
 */
public class WelcomeDialogue extends AbstractDialogue {

    private GroupWelcomeInfo welcomeMessage;

    public GroupWelcomeInfo getWelcomeMessage() {
        return welcomeMessage;
    }

    public void setWelcomeMessage(GroupWelcomeInfo welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }
}
