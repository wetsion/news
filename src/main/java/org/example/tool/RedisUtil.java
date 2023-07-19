package org.example.tool;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import javax.annotation.PreDestroy;
import java.util.Objects;

public class RedisUtil {

    private final static RedisUtil REDIS_UTIL = new RedisUtil();

    private Config config;

    private RedissonClient redissonClient;

    private RedisUtil() {
        config = new Config();
        config.useSingleServer().setAddress("redis://10.200.0.7:6379")
                .setPassword("weixin").setDatabase(1);

        redissonClient = Redisson.create(config);
    }

    public static RedissonClient redissonClient() {
        return REDIS_UTIL.redissonClient;
    }


    @PreDestroy
    public void destroy() {
        if (Objects.nonNull(redissonClient)) {
            redissonClient.shutdown();
        }
    }
}
