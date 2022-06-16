package cn.chahuyun.enumerate;

/**
 * DataEnum
 *
 * @author Zhangjiaxing
 * @description 枚举类
 * @date 2022/6/16 10:32
 */
public enum DataEnum {

    //ACCURATE("精准",1),
    ACCURATE("精准",1),
    //VAGUE("模糊",2),
    VAGUE("模糊",2),
    //END("结尾",3),
    END("结尾",3),
    //START("开头",4);
    START("开头",4);


    /**
     * 匹配类型
     */
    private String type;
    /**
     * 匹配类型id
     */
    private int typeInt;


     DataEnum(String type, int typeInt) {
        this.type = type;
        this.typeInt = typeInt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getTypeInt() {
        return typeInt;
    }

    public void setTypeInt(int typeInt) {
        this.typeInt = typeInt;
    }
}