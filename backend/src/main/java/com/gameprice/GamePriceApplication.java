package com.gameprice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@MapperScan("com.gameprice.**.mapper")
@SpringBootApplication
public class GamePriceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GamePriceApplication.class, args);
    }
}
