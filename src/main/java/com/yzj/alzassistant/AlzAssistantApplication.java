package com.yzj.alzassistant;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableAsync
@MapperScan("com.yzj.alzassistant.mapper")
public class AlzAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlzAssistantApplication.class, args);
    }

}
