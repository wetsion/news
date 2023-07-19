package org.example;

import org.example.tool.RedisUtil;
import org.redisson.api.RBucket;

public class RedisTest {

    public static void main(String[] args) {
        RBucket<String> rBucket = RedisUtil.redissonClient().getBucket("redisTest");
        rBucket.set("sh");

        System.out.println(rBucket.get());
    }
}
