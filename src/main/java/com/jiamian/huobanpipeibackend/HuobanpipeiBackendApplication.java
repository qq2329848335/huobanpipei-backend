package com.jiamian.huobanpipeibackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;

@SpringBootApplication
@MapperScan("com.jiamian.huobanpipeibackend.mapper")
public class HuobanpipeiBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HuobanpipeiBackendApplication.class, args);
    }

}
