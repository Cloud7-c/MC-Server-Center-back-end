package com.yunjic.mcsc;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.yunjic.mcsc.mapper")
public class MCSCApplication {

    public static void main(String[] args) {
        SpringApplication.run(MCSCApplication.class, args);
    }

}
