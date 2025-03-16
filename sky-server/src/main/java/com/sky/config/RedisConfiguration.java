package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.pqc.crypto.newhope.NHSecretKeyProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfiguration {

    @Bean//创建实例
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.info("开始创建redis模板对象...");
//RedisTemplate充当了 Java 应用程序与 Redis 之间的桥梁
        RedisTemplate redisTemplate = new RedisTemplate();

//简而言之，这行代码配置了 RedisTemplate 的连接信息
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        /**
         * 序列化：Redis 是基于内存的数据存储，而 Java 对象需要转换成字节流才能存储在 Redis 中。
         * 序列化器（如 StringRedisSerializer）负责将 Java 对象转换成适合存储的格式（反之亦然）。
         * 在这里，你选择了将键以字符串形式存储，这是最常见的做法。
         */
//        意味着 Redis 中的键将以字符串格式进行序列化和存储。
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        return redisTemplate;
    }
}
