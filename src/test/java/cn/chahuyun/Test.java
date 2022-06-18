package cn.chahuyun;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :
 * @Date 2022/6/17 19:35
 */
public class Test {

    public static void main(String[] args) {

        Map<String,String> map = new HashMap<>();
        map.put("卧槽", "abc");
        map.put("卧", "abc");
        map.put("槽", "abc");
        String s = map.keySet().toString();
        System.out.println(s);
        String a = "卧";
        System.out.println(s.indexOf(a));
        String s1 = "abcdef";
        String s2 = "bc";
        String s3 = "abc";
        String s4 = "ef";
        System.out.println(s1.indexOf(s2));
        System.out.println(s1.indexOf(s3));
        System.out.println(s1.indexOf(s4));
        System.out.println(s1.length()-s4.length());

        System.out.println(s1.substring(0,1));

        System.out.println(s1.substring(s1.length() - s4.length()));
        System.out.println(s1.substring(s1.length() - s4.length()));

        String[] strings = new String[2];
        System.out.println(strings.length);

        System.out.println("-------------------");
        String commandPattern = "查询|学习 |删除 |([+-][\\d\\[\\]\\w\\:]{6,}>[\\w]+)";
        System.out.println(Pattern.matches(commandPattern,"+572490972>session"));
        System.out.println(Pattern.matches(commandPattern,"学习"));
        System.out.println(Pattern.matches(commandPattern,"学习 "));
        System.out.println(Pattern.matches(commandPattern,"学习 a b"));

        Matcher matcher = Pattern.compile("\\d+").matcher("51651561");
        matcher.find();
        System.out.println(matcher.group());


    }


}
