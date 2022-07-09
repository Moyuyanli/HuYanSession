package cn.chahuyun.enums;

/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :匹配方式枚举
 * @Date 2022/7/9 17:10
 */
public enum Mate {

    //ACCURATE("精准",1),
    ACCURATE("精准",1),
    //VAGUE("模糊",2),
    VAGUE("模糊",2),
    //START("头部",3),
    START("头部",3),
    //END("结尾",4);
    END("结尾",4);

    private String mateName;
    private int mateType;

    Mate(String mateName, int mateType) {
        this.mateName = mateName;
        this.mateType = mateType;
    }

    public String getMateName() {
        return mateName;
    }

    public void setMateName(String mateName) {
        this.mateName = mateName;
    }

    public int getMateType() {
        return mateType;
    }

    public void setMateType(int mateType) {
        this.mateType = mateType;
    }
}