package com.gameprice.game.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * 列表查询参数：分页 + 搜索 + 筛选。
 * 直接用 @ModelAttribute 绑定 GET 参数，校验失败由 BindException 统一转错误码。
 */
public class GameListQuery {

    @Min(value = 1, message = "page 最小为 1")
    private long page = 1;

    @Min(value = 1, message = "size 最小为 1")
    @Max(value = 50, message = "size 最大为 50")
    private long size = 12;

    /** 关键词（AC-2.1）：命中名称 / 英文名 / 类型名 */
    @Size(max = 50, message = "关键词最长 50 字符")
    private String keyword;

    /** 类型筛选，可多选（AC-3.1） */
    private List<Long> typeIds;

    /** 平台筛选，可多选（AC-3.1） */
    private List<Long> platformIds;

    /** 价格区间下限（人民币起价，AC-3.5，含边界） */
    private BigDecimal priceMin;

    /** 价格区间上限（含边界） */
    private BigDecimal priceMax;

    /** 只看有折扣且折扣未结束的（AC-6.1 前置） */
    private Boolean discountOnly;

    /* ---------- 以下两个字段仅内部使用，不对外暴露 ---------- */

    /** 关键词规范化结果：小写 + 去空格，由 Service 填充 */
    @JsonIgnore
    private String keywordNorm;

    /** 关键词命中的类型 id，由 Service 填充 */
    @JsonIgnore
    private List<Long> keywordTypeIds;

/**
 * 获取当前页码的方法
 * @return 返回当前页码的long类型值
 */
    public long getPage() {
        return page; // 返回page变量的值
    }

/**
 * 设置当前页码的方法
 * @param page 要设置的页码值，类型为long
 */
    public void setPage(long page) {
    // 将传入的页码值赋给当前对象的page属性
        this.page = page;
    }

/**
 * 获取对象的大小
 * @return 返回对象的size属性值
 */
    public long getSize() {
        return size;    // 返回size属性的值
    }

/**
 * 设置对象的大小
 * @param size 要设置的大小值，类型为long
 */
    public void setSize(long size) {
        this.size = size;  // 将传入的size参数赋值给对象的size属性
    }

/**
 * 获取关键词的方法
 * @return 返回当前设置的关键词字符串
 */
    public String getKeyword() {
        return keyword;    // 返回关键字段的值
    }

/**
 * 设置关键词的方法
 * @param keyword 要设置的关键词字符串
 */
    public void setKeyword(String keyword) {
    // 将传入的keyword参数赋值给当前对象keyword属性
        this.keyword = keyword;
    }

/**
 * 获取类型ID列表的方法
 * @return 返回类型ID的列表，类型为Long的列表
 */
    public List<Long> getTypeIds() {
        return typeIds;  // 返回类型ID列表
    }

/**
 * 设置类型ID列表的方法
 * @param typeIds 类型ID列表，使用Long类型的List集合
 */
    public void setTypeIds(List<Long> typeIds) {
        this.typeIds = typeIds;  // 将传入的typeIds列表赋值给当前对象的typeIds属性
    }

/**
 * 获取平台ID列表的方法
 * @return 返回平台ID列表，类型为Long类型的List集合
 */
    public List<Long> getPlatformIds() {
        return platformIds;  // 返回当前对象的platformIds属性值
    }

/**
 * 设置平台ID列表的方法
 * @param platformIds 平台ID列表，使用Long类型的List集合
 */
    public void setPlatformIds(List<Long> platformIds) {
        this.platformIds = platformIds;  // 将传入的platformIds列表赋值给当前对象的platformIds属性
    }

/**
 * 获取最小价格的方法
 * @return 返回最小价格，使用BigDecimal类型保证精度
 */
    public BigDecimal getPriceMin() {
        return priceMin;  // 返回当前对象的priceMin属性值
    }

/**
 * 设置最小价格的方法
 * @param priceMin 最小价格，使用BigDecimal类型保证精度
 */
    public void setPriceMin(BigDecimal priceMin) {
        this.priceMin = priceMin;  // 将传入的priceMin值赋值给当前对象的priceMin属性
    }

/**
 * 获取最大价格的方法
 * @return 返回最大价格，使用BigDecimal类型保证精度
 */
    public BigDecimal getPriceMax() {
        return priceMax;  // 返回当前对象的priceMax属性值
    }

/**
 * 设置最大价格的方法
 * @param priceMax 最大价格，使用BigDecimal类型保证精度
 */
    public void setPriceMax(BigDecimal priceMax) {
        this.priceMax = priceMax;  // 将传入的priceMax值赋值给当前对象的priceMax属性
    }

/**
 * 获取是否仅显示折扣商品的方法
 * @return 返回是否仅显示折扣商品的布尔值
 */
    public Boolean getDiscountOnly() {
        return discountOnly;  // 返回当前对象的discountOnly属性值
    }

/**
 * 设置是否仅显示折扣商品的方法
 * @param discountOnly 是否仅显示折扣商品的布尔值
 */
    public void setDiscountOnly(Boolean discountOnly) {
        this.discountOnly = discountOnly;  // 将传入的discountOnly值赋值给当前对象的discountOnly属性
    }

/**
 * 获取规范化关键词的方法
 * @return 返回规范化后的关键词字符串
 */
    public String getKeywordNorm() {
        return keywordNorm;  // 返回当前对象的keywordNorm属性值
    }

/**
 * 设置规范化关键词的方法
 * @param keywordNorm 规范化后的关键词字符串
 */
    public void setKeywordNorm(String keywordNorm) {
        this.keywordNorm = keywordNorm;  // 将传入的keywordNorm字符串赋值给当前对象的keywordNorm属性
    }

/**
 * 获取关键词类型ID列表的方法
 * @return 返回关键词类型ID列表，类型为Long类型的List集合
 */
    public List<Long> getKeywordTypeIds() {
        return keywordTypeIds;  // 返回当前对象的keywordTypeIds属性值
    }

/**
 * 设置关键词类型ID列表的方法
 * @param keywordTypeIds 关键词类型ID列表，使用Long类型的List集合
 */
    public void setKeywordTypeIds(List<Long> keywordTypeIds) {
        this.keywordTypeIds = keywordTypeIds;  // 将传入的keywordTypeIds列表赋值给当前对象的keywordTypeIds属性
    }
}
