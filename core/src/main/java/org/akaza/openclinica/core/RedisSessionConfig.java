package org.akaza.openclinica.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
@EnableRedisHttpSession
public class RedisSessionConfig {

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        String host = System.getenv("REDIS_HOST");
        if (host == null) {
            host = "localhost";
        }
        String portStr = System.getenv("REDIS_PORT");
        int port = (portStr != null) ? Integer.parseInt(portStr) : 6379;
        
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration(host, port));
    }
}
