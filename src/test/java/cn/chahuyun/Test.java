package cn.chahuyun;

import java.util.HashMap;
import java.util.Map;

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

        System.out.println(s1.substring(s1.length() - s4.length()));
        System.out.println(s1.substring(s1.length() - s4.length()));



    }


}
