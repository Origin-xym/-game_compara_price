package com.gameprice.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gameprice.platform.entity.Platform;
import org.apache.ibatis.annotations.Mapper;

/**
 * PlatformMapper接口
 * 该接口是一个数据访问层(DAO)接口，继承自BaseMapper<Platform>
 * 使用了@Mapper注解，表明这是一个MyBatis的映射器接口
 *
 * @param <Platform> 泛型参数，表示该映射器操作的是Platform实体类
 */
@Mapper  // 声明这是一个MyBatis映射器接口
public interface PlatformMapper extends BaseMapper<Platform> {  // 继承BaseMapper，获得基本的CRUD操作方法
}
