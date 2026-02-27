package com.jepai.exam;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 捷评智航考试系统 - 启动类
 */
@SpringBootApplication
@MapperScan("com.jepai.exam.modules.*.mapper")
@EnableScheduling
public class ExamApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExamApplication.class, args);
    }
}
