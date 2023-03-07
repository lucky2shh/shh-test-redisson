package cn.shh.test.redisson;

import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestRedissonBase {
    @Qualifier("redissonClientV1")
    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void test(){
        System.out.println("redissonClient: " + redissonClient);
    }
}
