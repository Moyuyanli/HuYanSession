package cn.chahuyun.session.entity;

/**
 * 基础实体接口
 *
 * @author Moyuyanli
 * @date 2023/8/4 10:32
 */
public interface BaseEntity {

    /**
     * 修改 this 所保存的数据
     * 用于保存或更新
     *
     * @return boolean t 成功
     * @author Moyuyanli
     * @date 2023/8/4 10:33
     */
    boolean merge();

    /**
     * 删除
     *
     * @return boolean t 成功
     * @author Moyuyanli
     * @date 2023/8/4 10:34
     */
    boolean remove();

}
