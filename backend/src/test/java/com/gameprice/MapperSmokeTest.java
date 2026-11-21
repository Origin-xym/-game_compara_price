package com.gameprice;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gameprice.game.entity.Game;
import com.gameprice.game.mapper.GameMapper;
import com.gameprice.game.mapper.GameTypeMapper;
import com.gameprice.platform.mapper.PlatformMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MapperSmokeTest {

    @Autowired
    private GameMapper gameMapper;
    @Autowired
    private GameTypeMapper gameTypeMapper;
    @Autowired
    private PlatformMapper platformMapper;

    @Test
    void mappersShouldConnectAndQuery() {
        long games = gameMapper.selectCount(new LambdaQueryWrapper<Game>().eq(Game::getStatus, 1));
        long types = gameTypeMapper.selectCount(null);
        long platforms = platformMapper.selectCount(null);

        System.out.println("=== 冒烟查询 ===");
        System.out.println("上架游戏数 = " + games);
        System.out.println("游戏类型数 = " + types);
        System.out.println("平台数     = " + platforms);

        // 顺带验证时间字段自动填充与列映射（插入一条再删）
        Game g = new Game();
        g.setName("__smoke_test_game__");
        g.setNameEn("Smoke Test");
        g.setGameTypeId(1L);
        g.setStatus(0);
        g.setSortWeight(0);
        gameMapper.insert(g);
        System.out.println("插入后 createTime = " + g.getCreateTime());
        gameMapper.deleteById(g.getId());
    }
}
