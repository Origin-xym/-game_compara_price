package com.gameprice.game.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gameprice.game.dto.GameListQuery;
import com.gameprice.game.entity.Game;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 游戏数据访问接口，继承自BaseMapper
 * 该接口用于与数据库中的游戏表进行交互
 * 使用MyBatis-Plus提供的BaseMapper接口，可以获得基本的CRUD操作
 * @Mapper 注解表示这是一个MyBatis的Mapper接口
 */
@Mapper  // 声明为MyBatis的Mapper接口
public interface GameMapper extends BaseMapper<Game> {  // 继承BaseMapper<Game>，获得Game实体的基本数据库操作能力
/**
 * 分页查询游戏列表
 * @param page 分页参数对象，包含分页信息如当前页码、每页大小等
 * @param query 查询条件对象，用于封装查询参数
 * @return 返回分页后的游戏列表结果
 */
    List<Game> selectAppPage(IPage<Game> page, @Param("q")GameListQuery query);

    /**
     * 忽略空白与全角空格的名称查重（Steam 返回“赛博朋克 2077”，库里是“赛博朋克2077”）。
     * 仅用于 Steam 导入的“是否已存在”判断，不改变 uk_name 的严格唯一约束。
     */
    @Select("SELECT * FROM game WHERE REPLACE(REPLACE(name, ' ', ''), '　', '') = #{normalized} LIMIT 1")
    Game selectByNameLoose(@Param("normalized") String normalized);
}
