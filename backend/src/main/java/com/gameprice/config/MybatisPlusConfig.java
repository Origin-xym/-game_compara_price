package com.gameprice.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 插件配置。
 * 顺序要求：分页插件必须放在最后。
 */
@Configuration  // 声明为配置类，用于配置Spring容器
public class MybatisPlusConfig {



    /**
     * 配置MyBatis-Plus的拦截器
     * @return MybatisPlusInterceptor 拦截器实例
     */
    @Bean  // 声明为Bean，由Spring容器管理
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 防止误操作全表 update / delete（后台管理场景很需要）
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        // 分页插件：必须放最后
        PaginationInnerInterceptor pagination = new PaginationInnerInterceptor(DbType.MYSQL);
        pagination.setMaxLimit(200L);   // 单页上限，防止 size=10000 拖库
        pagination.setOverflow(false);  // 页码越界时返回空列表，不回到第一页
        interceptor.addInnerInterceptor(pagination);

        return interceptor;
    }
}
