package cn.chahuyun.command;

import lombok.Data;

/**
 * 指令实体
 *
 * @author Zhangjiaxing
 * @description 包含指令的基本信息
 * @date 2022/6/8 9:41
 */
@Data
public class CommandBase {


    private String PREFIX = "#";

    private String commandMessage;

}