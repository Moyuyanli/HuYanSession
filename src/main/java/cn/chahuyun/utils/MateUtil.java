package cn.chahuyun.utils;

import cn.chahuyun.enums.Mate;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :匹配方式工具类
 * @Date 2022/8/17 19:35
 */
public class MateUtil {

    /**
     * 传入匹配方式编号 返回 匹配方式
     *
     * @param mateType 匹配方式int
     * @return cn.chahuyun.enums.Mate
     * @author Moyuyanli
     * @date 2022/8/17 19:37
     */
    public static Mate getMate(int mateType) {
        switch (mateType) {
            case 1:
                return Mate.ACCURATE;
            case 2:
                return Mate.VAGUE;
            case 3:
                return Mate.START;
            case 4:
                return Mate.END;
        }
        return Mate.ACCURATE;
    }

}
