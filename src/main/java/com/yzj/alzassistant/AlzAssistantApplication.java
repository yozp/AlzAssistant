package com.yzj.alzassistant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.yzj.alzassistant.mapper")
public class AlzAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlzAssistantApplication.class, args);
    }

}
