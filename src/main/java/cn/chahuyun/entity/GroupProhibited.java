package cn.chahuyun.entity;

import jakarta.persistence.*;

/**
 * GroupProhibited
 * 群违禁词
 *
 * @author Zhangjiaxing
 * @date 2022/8/16 10:33
 */
@Entity
@Table
public class GroupProhibited {

    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    /**
     * 触发词
     */
    private String trigger;
    /**
     * 回复消息
     */
    private String reply;



}