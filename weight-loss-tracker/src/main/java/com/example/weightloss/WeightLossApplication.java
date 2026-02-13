package com.example.weightloss;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.weightloss.mapper")
public class WeightLossApplication {
    public static void main(String[] args) {
        SpringApplication.run(WeightLossApplication.class, args);
        System.out.println("减肥追踪系统启动成功！");
    }
}