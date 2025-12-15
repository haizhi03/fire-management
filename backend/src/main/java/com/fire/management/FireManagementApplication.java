package com.fire.management;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 消防设施管理系统启动类
 */
@SpringBootApplication
@MapperScan("com.fire.management.mapper")
public class FireManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(FireManagementApplication.class, args);
        System.out.println("消防设施管理安全系统启动成功！");
    }
}
