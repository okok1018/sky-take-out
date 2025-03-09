package com.sky.config;

import com.sky.properties.MinioProperties;
import com.sky.utils.MinioOssUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@AllArgsConstructor
public class OssConfiguration {
    private  MinioProperties minioProperties;

    @Bean
    @ConditionalOnMissingBean
    public MinioOssUtil minioOssUtil() {
    log.info("创建工具类对象{}",minioProperties);
        return new MinioOssUtil(minioProperties);
    }
}
