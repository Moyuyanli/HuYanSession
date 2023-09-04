package cn.chahuyun.session.entity;

import cn.chahuyun.session.utils.HibernateUtil;
import cn.chahuyun.session.utils.ScopeUtil;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

import static cn.chahuyun.session.HuYanSession.LOGGER;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :群欢迎词
 * @Date 2022/8/17 19:48
 */
@Entity
@Table(name = "GroupWelcomeInfo")
public class GroupWelcomeInfo extends BaseMessage implements BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    /**
     * 所属机器人
     */
    private long bot;
    /**
     * 是否随机
     */
    private boolean random;
    /**
     * 轮询次数
     */
    private int pollingNumber;
    /**
     * 群欢迎消息随机标识 - 用于匹配欢迎消息
     */
    private int randomMark;
    /**
     * 作用域标识
     */
    private String scopeMark;
    /**
     * 欢迎消息集合
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, targetEntity = WelcomeMessage.class,mappedBy = "groupWelcomeInfoId")
    private List<WelcomeMessage> welcomeMessages = new ArrayList<>();
    /**
     * 作用域
     */
    @Transient
    private Scope scope;

    public GroupWelcomeInfo() {
    }

    public GroupWelcomeInfo(long bot, boolean random, int randomMark, Scope scope) {
        this.bot = bot;
        this.random = random;
        this.pollingNumber = 1;
        this.randomMark = randomMark;
        if (scope.isGlobal()) {
            this.scopeMark = bot + ".";
        } else if (scope.isGroupInfo()) {
            this.scopeMark = bot + ".gr" + scope.getListId();
        } else {
            this.scopeMark = bot + "." + scope.getGroupNumber();
        }
        this.scope = scope;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getBot() {
        return bot;
    }

    public void setBot(long bot) {
        this.bot = bot;
    }

    public boolean isRandom() {
        return random;
    }

    public void setRandom(boolean random) {
        this.random = random;
    }

    public int getPollingNumber() {
        return pollingNumber;
    }

    public void setPollingNumber(int pollingNumber) {
        this.pollingNumber = pollingNumber;
    }

    public String getScopeMark() {
        return scopeMark;
    }

    public void setScopeMark(String scopeMark) {
        this.scopeMark = scopeMark;
    }

    public List<WelcomeMessage> getWelcomeMessages() {
        return welcomeMessages;
    }

    public void setWelcomeMessages(List<WelcomeMessage> welcomeMessages) {
        this.welcomeMessages = welcomeMessages;
    }

    public int getRandomMark() {
        return randomMark;
    }

    public void setRandomMark(int randomMark) {
        this.randomMark = randomMark;
    }

    public Scope getScope() {
        return ScopeUtil.getScope(this.scopeMark);
    }

    public void setScope(Scope scope) {
        if (scope.isGlobal()) {
            this.scopeMark = bot + ".";
        } else if (scope.isGroupInfo()) {
            this.scopeMark = bot + ".gr" + scope.getListId();
        } else {
            this.scopeMark = bot + "." + scope.getGroupNumber();
        }
        this.scope = scope;
    }


    /**
     * 修改 this 所保存的数据
     * 用于保存或更新
     *
     * @return boolean t 成功
     * @author Moyuyanli
     * @date 2023/8/4 10:33
     */
    @Override
    public boolean merge() {
        try {
            HibernateUtil.factory.fromTransaction(session -> {
                GroupWelcomeInfo merge = session.merge(this);
                merge.getWelcomeMessages().forEach(it -> {
                    it.setMark(merge.getId());
                });
                return null;
            });
        } catch (Exception e) {
            if (e.getMessage().equals("Converting `org.hibernate.exception.ConstraintViolationException` to JPA `PersistenceException` : could not execute statement")) {
                HibernateUtil.factory.fromTransaction(session -> {
                    session.createNativeQuery("drop table WELCOMEMESSAGE").executeUpdate();
                    return null;
                });
                LOGGER.error("请重启MiraiConsole！");
                return false;
            }
            LOGGER.error("欢迎词保存失败！");
            return false;
        }
        return true;
    }

    /**
     * 删除
     *
     * @return boolean t 成功
     * @author Moyuyanli
     * @date 2023/8/4 10:34
     */
    @Override
    public boolean remove() {
        try {
            HibernateUtil.factory.fromTransaction(session -> {
                session.remove(this);
                return null;
            });
        } catch (Exception e) {
            LOGGER.error("群组信息删除失败！");
            return false;
        }
        return true;
    }
}
