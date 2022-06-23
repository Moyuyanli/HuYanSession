package cn.chahuyun.utils;

import cn.chahuyun.HuYanSession;
import cn.chahuyun.data.ScopeInfo;
import cn.chahuyun.enumerate.DataEnum;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.utils.MiraiLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MessageUtil
 *
 * @author Zhangjiaxing
 * @description 消息类的大部分统一方法工具类
 * @date 2022/6/22 11:31
 */
public class MessageUtil {

    public static final MessageUtil INSTANCE = new MessageUtil();
    private MiraiLogger l = HuYanSession.INSTANCE.getLogger();

    /**
     * miraiCode匹配正则
     */
    private final String miraiCode = "\\[mirai:\\w+:[\\S]+\\]";

    /**
     * 学习正则
     * 学习(多词条)?([\s]+[\S]+){2}(\s+(精准|模糊|头部|结尾|当前|全局|轮询|随机)){0,3}
     */
    public String studyPattern = "学习(多词条)?([\\s]+[\\S]+){2}(\\s+(精准|模糊|头部|结尾|当前|全局|轮询|随机)){0,3}";

    /**
     * 删除正则
     * 删除(多词条)?\s+(\S)+
     */
    public String deletePattern = "删除(多词条)?\\s+(\\S)+\\s?(\\S)?";


    /**
     * 匹配器，重复利用
     */
    public Matcher matcher;

    /**
     * @description 判断是否是学习类指令
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
     * @description 学习参数解析
     * @author zhangjiaxing
     * @param event 消息事件
     * @date 2022/6/23 16:38
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    public Map<String, Object> spotStudyCommandParam(MessageEvent event) {
        //获取匹配数据
        String group = matcher.group();
        //删除换行
        String replace = group.replace("\\n", "");
        //分割参数
        String[] split = replace.split("\\s+");
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
        ScopeInfo scopeInfo = new ScopeInfo("当前", true, event.getSubject().getId());
        DataEnum dataEnum = DataEnum.ACCURATE;
        //有参数就判断参数，没有就不判断
        if (split.length >= 4) {
            for (int i = 3; i < split.length; i++) {
                String s = split[i];
                switch (s) {
                    case "模糊":
                        dataEnum = DataEnum.VAGUE;break;
                    case "头部":
                        dataEnum = DataEnum.START;break;
                    case "结尾":
                        dataEnum = DataEnum.END;break;
                    case "全局":
                        scopeInfo = new ScopeInfo("全局", false, null);break;
                    case "随机":
                        if (studyType) {
                            contentType = 3;
                        }
                        break;
                    default:break;
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
        map.put("scopeInfo", scopeInfo);
        map.put("dataEnum", dataEnum);
        map.put("key", key);
        map.put("value", value);
        return map;
    }

    /**
     * @description 判断是否是删除指令
     * @author zhangjiaxing
     * @param event
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
     * @description 删除
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
        if (split[0].equals("删除多词条")) {
            return "! " + split[1]+" "+split[2];
        }
        return split[1];

    }

    


}