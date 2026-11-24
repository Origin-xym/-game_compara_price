package com.gameprice.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gameprice.platform.entity.Platform;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;


/**
 * PlatformMapper接口
 * 该接口是一个数据访问层(DAO)接口，继承自BaseMapper<Platform>
 * 使用了@Mapper注解，表明这是一个MyBatis的映射器接口
 *
 */
@Mapper  // 声明这是一个MyBatis映射器接口
public interface PlatformMapper extends BaseMapper<Platform> {  // 继承BaseMapper，获得基本的CRUD操作方法
    /** 绕过 @TableLogic：查同编码同区域（含已删除），用于“删了再建”时恢复 */
    @Select("SELECT * FROM platform WHERE code = #{code} AND region = #{region} LIMIT 1")
    Platform selectByCodeRegionIgnoreDeleted(@Param("code") String code,
                                             @Param("region") String region);

    /** 把逻辑删除的平台复活，并刷新表单字段 */
    @Update("UPDATE platform SET deleted = 0, name = #{p.name}, currency = #{p.currency}, "
            + "link_template = #{p.linkTemplate}, data_source_type = #{p.dataSourceType}, "
            + "api_ready = #{p.apiReady}, enabled = #{p.enabled}, remark = #{p.remark} "
            + "WHERE id = #{p.id}")
    int restore(@Param("p") Platform p);

}
