package cn.chahuyun.data;


import cn.chahuyun.GroupSession;
import net.mamoe.mirai.utils.MiraiLogger;

import java.util.ArrayList;
import java.util.List;

import static cn.chahuyun.GroupSession.sessionData;
/**
 * 说明
 *
 * @author Moyuyanli
 * @Description :会话分页显示
 * @Date 2022/6/16 21:31
 */
public class SessionDataPaging {

    private static MiraiLogger l = GroupSession.INSTANCE.getLogger();

    private ArrayList<SessionDataBase> sessionPattern = (ArrayList<SessionDataBase>) sessionData.getSession();


    /**
     * 当前分页页数
     */
    private int pageNum;
    /**
     * 分页大小
     */
    private int pageSize;
    /**
     * 总页数
     */
    private int pageAll;
    /**
     * 总条数
     */
    private int pageAllSize;

    private ArrayList<SessionDataBase> list;

    public SessionDataPaging() {
    }

    /**
     * 处理List集合数据进行分页
     *
     * @param currentPage 当前页
     * @param pageSize    每页数据个数
     * @param list        进行分页的数据
     * @return
     */
    public static SessionDataPaging  queryPageInfo(int currentPage, int pageSize, ArrayList<SessionDataBase> list) {
        ArrayList<SessionDataBase> subList = null;
        //list的总数量
        int total = list.size();
        //如果总数大于每页数据量
        if (total > pageSize) {
            //获取需要截取页数的最大下标
            int toIndex = pageSize * currentPage;
            //如果list的最大没有页数的总数大
            if (toIndex > total) {
                //将list的最大改存为目前最大下标
                toIndex = total;
            }
            //截取list，从该页的位置数截取起
            subList = (ArrayList<SessionDataBase>) list.subList(pageSize * (currentPage - 1), toIndex);
        }else {
            subList = list;
        }

        SessionDataPaging paging = new SessionDataPaging(currentPage, pageSize,
                /*
                总分页信息
                 */
                (total + pageSize) / pageSize,
                total, subList
        );

        return paging;
    }

    /**
     * @description
     * @author zhangjiaxing
     * @param pageNum 当前分页页数
     * @param pageSize 分页大小
     * @param pageAll 总页数
     * @param pageAllSize 总条数
     * @param list 数据
     * @date 2022/6/16 22:49
     * @return
     */
    public SessionDataPaging(int pageNum, int pageSize, int pageAll, int pageAllSize,ArrayList<SessionDataBase> list) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pageAll = pageAll;
        this.pageAllSize = pageAllSize;
        this.list = list;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageAll() {
        return pageAll;
    }

    public void setPageAll(int pageAll) {
        this.pageAll = pageAll;
    }

    public int getPageAllSize() {
        return pageAllSize;
    }

    public void setPageAllSize(int pageAllSize) {
        this.pageAllSize = pageAllSize;
    }

    public ArrayList<SessionDataBase> getList() {
        return list;
    }

    public void setList(ArrayList<SessionDataBase> list) {
        this.list = list;
    }
}
