package org.example.tool;

import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import javax.annotation.PreDestroy;
import java.util.Objects;

public class RedisUtil {

    private final static RedisUtil REDIS_UTIL = new RedisUtil();

    private Config config;

    private RedissonClient redissonClient;

    private RedisUtil() {
        String local = System.getProperty("local");
        if (StringUtils.isNotBlank(local)) {
            return;
        }
        config = new Config();
        config.useSingleServer().setAddress("redis://10.203.0.15:6379")
                .setPassword("weixin").setDatabase(1);

        redissonClient = Redisson.create(config);
    }

    public static RedissonClient redissonClient() {
        return REDIS_UTIL.redissonClient;
    }

    public static RLock getLock(String key) {
        String local = System.getProperty("local");
        if (StringUtils.isNotBlank(local)) {
            return new LocalLock();
        }
        return REDIS_UTIL.redissonClient.getLock(key);
    }


    @PreDestroy
    public void destroy() {
        if (Objects.nonNull(redissonClient)) {
            redissonClient.shutdown();
        }
    }
}
