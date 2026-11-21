package com.gameprice.common;

import java.util.Collections;
import java.util.List;

/**
 * 分页结果。列表页（AC-1.2）与"我的收藏"（AC-9.2）共用。
 * hasMore 供"加载更多"模式使用，避免前端自己算边界。
 */
public class PageResult<T> {

    // 总记录数
    private long total;
    // 当前页码
    private long page;
    // 每页大小
    private long size;
    // 当前页数据列表
    private List<T> list;

    /**
     * 默认构造方法
 * 创建一个空的PageResult对象，不进行任何初始化操作
 * 使用此构造方法创建的对象需要后续手动设置相关属性
     */
    public PageResult() {
    }

    /**
     * 构造方法，初始化分页结果
     * @param total 总记录数
     * @param page 当前页码
     * @param size 每页大小
     * @param list 当前页数据列表
     */
    public PageResult(long total, long page, long size, List<T> list) {
        this.total = total;
        this.page = page;
        this.size = size;
        this.list = list == null ? Collections.emptyList() : list;
    }

    /**
     * 创建分页结果
     * @param total 总记录数
     * @param page 当前页码
     * @param size 每页大小
     * @param list 当前页数据列表
     * @param <T> 数据类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(long total, long page, long size, List<T> list) {
        return new PageResult<>(total, page, size, list);
    }

    /**
     * 创建空分页结果
     * @param page 当前页码
     * @param size 每页大小
     * @param <T> 数据类型
     * @return 空分页结果
     */
    public static <T> PageResult<T> empty(long page, long size) {
        return new PageResult<>(0, page, size, Collections.emptyList());
    }

    /**
     * 获取总记录数
     * @return 总记录数
     */
    public long getTotal() {
        return total;
    }

    /**
     * 设置总记录数
     * @param total 总记录数
     */
    public void setTotal(long total) {
        this.total = total;
    }

    /**
     * 获取当前页码
     * @return 当前页码
     */
    public long getPage() {
        return page;
    }

    /**
     * 设置当前页码
     * @param page 当前页码
     */
    public void setPage(long page) {
        this.page = page;
    }

    /**
     * 获取每页大小
     * @return 每页大小
     */
    public long getSize() {
        return size;
    }

    /**
     * 设置每页大小
     * @param size 每页大小
     */
    public void setSize(long size) {
        this.size = size;
    }

    /**
     * 获取当前页数据列表
     * @return 当前页数据列表
     */
    public List<T> getList() {
        return list;
    }

    /**
     * 设置当前页数据列表
     * @param list 当前页数据列表
     */
    public void setList(List<T> list) {
        this.list = list;
    }

    /** 总页数 */
    public long getTotalPages() {
        if (size <= 0) {
            return 0;
        }
        return (total + size - 1) / size;
    }

    /** 是否还有下一页（AC-1.2 加载更多） */
    public boolean isHasMore() {
        return page * size < total;
    }
}
