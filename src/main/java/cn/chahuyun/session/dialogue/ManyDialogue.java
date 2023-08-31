package cn.chahuyun.session.dialogue;

import cn.chahuyun.session.entity.ManySession;
import cn.chahuyun.session.entity.ManySessionInfo;
import net.mamoe.mirai.event.events.MessageEvent;

/**
 * 多词条消息
 *
 * @author Moyuyanli
 * @date 2023/8/31 9:46
 */
public class ManyDialogue extends AbstractDialogue{

    private ManySessionInfo manySession;

    public ManySessionInfo getManySession() {
        return manySession;
    }

    public void setManySession(ManySessionInfo manySession) {
        this.manySession = manySession;
    }
}
