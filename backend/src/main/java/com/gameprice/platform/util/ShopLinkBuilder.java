package com.gameprice.platform.util;

/** 商店链接生成：模板 + 外部 ID（AC-7.2） */
public final class ShopLinkBuilder {

    // 外部ID的占位符常量，用于模板字符串中替换实际的外部ID值
    private static final String EXTERNAL_ID_PLACEHOLDER = "{externalId}";

/**
 * ShopLinkBuilder的私有构造方法
 * 使用私有构造方法是为了实现单例模式，防止外部代码直接创建该类的实例
 * 这样可以确保全局只有一个ShopLinkBuilder实例存在
 */
    private ShopLinkBuilder() {
    // 私有构造方法，不执行任何操作
    // 构造方法为空，表示不需要进行初始化操作
    }

/**
 * 根据模板和外部ID构建链接
 * @param linkTemplate 链接模板字符串，其中包含占位符
 * @param externalId 外部ID，用于替换模板中的占位符
 * @return 构建完成的链接字符串，如果输入参数无效则返回null
 */
    public static String build(String linkTemplate, String externalId) {
        // 检查链接模板是否为空或空白字符串
        if (linkTemplate == null || linkTemplate.isBlank()) {
            return null;
        }
        // 检查外部ID是否为空或空白字符串
        if (externalId == null || externalId.isBlank()) {
            return null;   // 缺 ID 生成不了，交由上层标为不可跳转（AC-7.3）
        }
        // 替换模板中的占位符为修剪后的外部ID，并返回构建完成的链接
        return linkTemplate.replace(EXTERNAL_ID_PLACEHOLDER, externalId.trim());
    }
}
