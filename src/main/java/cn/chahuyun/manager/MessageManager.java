package cn.chahuyun.manager;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.entity.ScopeInfoBase;
import cn.chahuyun.entity.SessionDataBase;
import cn.chahuyun.enumerate.DataEnum;
import cn.chahuyun.files.GroupData;
import cn.chahuyun.files.PluginData;
import kotlin.coroutines.EmptyCoroutineContext;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.ConcurrencyKind;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.EventPriority;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.utils.MiraiLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MessageUtil
 *
 * @author Zhangjiaxing
 * @description 普通指令消息管理
 * @date 2022/6/22 11:31
 */
public class MessageManager {

    public static final MessageManager INSTANCE = new MessageManager();
    private final MiraiLogger l = HuYanSession.INSTANCE.getLogger();

    /**
     * miraiCode匹配正则
     */
    private final String miraiCode = "\\[mirai:\\w+:[\\S]+\\]";

    /**
     * 学习正则
     * 学习(多词条)?([\s]+[\S]+){2}(\s+(精准|模糊|头部|结尾|当前|全局|轮询|随机)){0,3}
     */
    public String studyPattern = "学习(多词条)?([\\s]+[\\S]+){2}(\\s+(精准|模糊|头部|结尾|当前|全局|轮询|随机|(gr\\d+))){0,3}";

    /**
     * 删除正则
     * 删除(多词条)?\s+(\S)+
     */
    public String deletePattern = "删除(多词条)?\\s+(\\S)+\\s?(\\S+)?";


    /**
     * 匹配器，重复利用
     */
    public Matcher matcher;

    /**
     *  判断是否是学习类指令
     * @author zhangjiaxing
     * @param event 消息事件
     * @date 2022/6/23 16:37
     * @return boolean
     */
    public boolean isStudyCommand(MessageEvent event) {
        //MiraiCode转码
        String code = event.getMessage().serializeToMiraiCode();
        //创建正则匹配器
        matcher = Pattern.compile(studyPattern).matcher(code);
        //判断是否有匹配数据
        return matcher.find();
    }

    /**
     *  学习参数解析
     * @author zhangjiaxing
     * @param event 消息事件
     * @date 2022/6/23 16:38
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    public Map<String, Object> spotStudyCommandParam(MessageEvent event) {
        //获取匹配数据
        String group = matcher.group();
        //分割参数
        String[] split = group.split("\\s+");
        //识别换行信息
        split[1] = split[1].replace("\\n", "\n");
        //后面的第一个参数如果是换行的话，给清除
        String substring = split[2];
        if (substring.startsWith("\\n")) {
            substring = substring.substring(2);
        }
        split[2] = substring.replace("\\n", "\n");
        //判断学习方式类型
        boolean studyType = split[0].equals("学习多词条");
        //获取值
        String key = split[1];
        String value = split[2];
        //判断数据类型
        int contentType = 0;
        //只要是有带MiraiCode码的都改为其他类匹配
        if (Pattern.matches(miraiCode, key) || Pattern.matches(miraiCode, value)) {
            contentType = 1;
        }
        //但是如果学习类型为多词条，改为多词条
        if (studyType) {
            contentType = 2;
        }
        //作用域和匹配机制
        ScopeInfoBase scopeInfoBase = new ScopeInfoBase("当前", true, false, event.getSubject().getId(), 0);
        DataEnum dataEnum = DataEnum.ACCURATE;
        //有参数就判断参数，没有就不判断
        if (split.length >= 4) {
            for (int i = 3; i < split.length; i++) {
                String s = split[i];
                switch (s) {
                    case "模糊":
                        dataEnum = DataEnum.VAGUE;
                        break;
                    case "头部":
                        dataEnum = DataEnum.START;
                        break;
                    case "结尾":
                        dataEnum = DataEnum.END;
                        break;
                    case "全局":
                        scopeInfoBase = new ScopeInfoBase("全局", false, false, null, 0);
                        break;
                    case "随机":
                        if (studyType) {
                            contentType = 3;
                        }
                        break;
                    default:
                        if (Pattern.matches("gr\\d+|群组\\d+", s)) {
                            int groupNum = Integer.parseInt(s.substring(2));
                            boolean containsKey = GroupData.INSTANCE.getGroupList().containsKey(groupNum);
                            if (!containsKey) {
                                event.getSubject().sendMessage("没有该群组信息，请检查群组!");
                                return null;
                            }
                            scopeInfoBase = new ScopeInfoBase("群组", false, true, null, groupNum);
                        }
                        break;
                }
            }
        }
        //装入参数
        HashMap<String, Object> map = new HashMap<>();
        /*
          studyType 学习类型
          contentType 数据保存类型
          scopeInfo 作用域
          dataEnum 匹配机制
          key k
          value v
         */
        map.put("studyType", studyType);
        map.put("contentType", contentType);
        map.put("scopeInfo", scopeInfoBase);
        map.put("dataEnum", dataEnum);
        map.put("key", key);
        map.put("value", value);
        return map;
    }

    /**
     *  判断是否是删除指令
     * @author zhangjiaxing
     * @param event 消息事件
     * @date 2022/6/23 22:15
     * @return boolean
     */
    public boolean isDeleteCommand(MessageEvent event) {
        String code = event.getMessage().serializeToMiraiCode();
        //同理
        matcher = Pattern.compile(deletePattern).matcher(code); 
        return matcher.find();
    }

    /**
     *  删除
     * @author zhangjiaxing
     * @date 2022/6/23 22:15
     * @return java.lang.String
     */
    public String  deleteParam() {
        //获取匹配数据
        String group = matcher.group();
        //删除换行
        String replace = group.replace("\\n", "");
        //分割参数
        String[] split = replace.split("\\s+");
        if ("删除多词条".equals(split[0])) {
            if (split.length == 3) {
                return "! " + split[1] + " " + split[2];
            } else {
                return null;
            }
        }
        return split[1] ;

    }


    /**
     * 用于批量添加多词条的list和key
     */
    private ArrayList<String> repeatedlyList;
    private String repeatedKey;

    /**
     *  判断是不是批量添加多词条，如果是，就直接进入方法不返回值了
     * @author zhangjiaxing
     * @param event 消息事件
     * @date 2022/6/27 10:19
     * @return void
     */
    public void isRepeatedlyAddMessage(MessageEvent event) {
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();
        //判断格式
        if (!Pattern.matches("添加多词条 (\\S+)", code)) {
            subject.sendMessage("格式错误，请检查!");
            return;
        }
        //获取参数
        String[] split = code.split(" ");
        Map<String, SessionDataBase> sessionMap = PluginData.INSTANCE.loadSessionMap();
        //判断有没有这条数据
        if (!sessionMap.containsKey(split[1])) {
            subject.sendMessage("没有找到该多词条，请查询！");
            return;
        }
        //判断是不是多词条
        int type = sessionMap.get(split[1]).getType();
        if (type != 2 && type != 3) {
            subject.sendMessage("没有找到该多词条，请查询！");
            return;
        }
        //保存key和新建批量内容
        repeatedKey = split[1];
        repeatedlyList = new ArrayList<String>();
        subject.sendMessage("请发送你要添加的内容，'!'删除上一次添加，'!!!'结束添加。");
        //结束当前事件监听
        event.intercept();
        //开始循环
        repeatedlyAddMessage(event);
    }




    /**
     * 通过不断重复调用来实现循环添加，每一次调用都会重新监听一次消息
     * @author zhangjiaxing
     * @param event
     * @date 2022/6/27 10:22
     * @return void
     */
    public void repeatedlyAddMessage(MessageEvent event) {
        EventChannel<MessageEvent> channel = GlobalEventChannel.INSTANCE.filterIsInstance(MessageEvent.class)
                .filter(it -> it.getSender().getId() == event.getSender().getId());


        channel.subscribeOnce(MessageEvent.class,EmptyCoroutineContext.INSTANCE,ConcurrencyKind.LOCKED, EventPriority.HIGH, mt -> {
            String code = mt.getMessage().serializeToMiraiCode();
            if ("!".equals(code)|| "！".equals(code)) {
                if (repeatedlyList.size() > 0) {
                    repeatedlyList.remove(repeatedlyList.size() - 1);
                    mt.getSubject().sendMessage("删除上一条添加成功-'!'删除上一次添加，'!!!'结束添加。");
                    repeatedlyAddMessage(mt);
                }
            } else if (Pattern.matches("[!！]{3}", code)) {
                MessageChain messages = PluginData.INSTANCE.addPolyglotMessage(repeatedKey, repeatedlyList);
                mt.getSubject().sendMessage(messages);
                mt.intercept();
            }else {
                addMessage(mt);
            }
        });

    }

    /**
     * 多词条消息轮询回复的重复调用方法
     * @author zhangjiaxing
     * @param event 消息事件
     * @date 2022/6/30 19:29
     */
    public void addMessage(MessageEvent event) {
        String code = event.getMessage().serializeToMiraiCode();
        Contact subject = event.getSubject();

        repeatedlyList.add(code);
        subject.sendMessage("添加成功-'!'删除上一次添加，'!!!'结束添加。");
        event.intercept();
        repeatedlyAddMessage(event);
    }

    public void init() {
        boolean judge = false;
        ArrayList<SessionDataBase> sessionPattern = new ArrayList<>(PluginData.INSTANCE.loadSessionMap().values());
        for (SessionDataBase dataBase : sessionPattern) {
            try {
                Boolean groupType = dataBase.getScopeInfo().getGroupType();
                if (groupType) {
                    judge = false;
                }
            } catch (Exception e) {
                judge = true;
            }
        }

        if (judge) {
            l.warning( "您有多条消息回复为旧版数据，请更新!");
        }
    }

}