package cn.shh.test.redisson.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.TransportMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redis 核心配置
 */
@Configuration
public class RedissonConfig {
    /**
     * 单节点模式
     */
    @Bean("redissonClientV1")
    public RedissonClient redissonClientV1(){
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379")
                .setPassword("redis368.cn");
        // 设置编码
        //config.setCodec(new JsonJacksonCodec());
        // 线程数量，默认值: CPU核数 * 2
        //config.setThreads();
        // Netty线程数量，默认值: CPU核数 * 2
        //config.setNettyThreads();
        // 线程池
        //config.setExecutor();
        //config.setEventLoopGroup();
        // 传输模式
        //config.setTransportMode();
        // 看门狗时间
        //config.setLockWatchdogTimeout();
        // 订阅发布顺序
        //config.setKeepPubSubOrder();
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }

    /**
     * 哨兵模式
     */
    /*@Bean("redissonClientV2")
    public RedissonClient redissonClientV2(){
        Config config = new Config();
        config.useSentinelServers()
                .setMasterName("mymaster")
                .addSentinelAddress("127.0.0.1:26389", "127.0.0.1:26379");
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }*/

    /**
     * 主从模式
     */
    /*@Bean("redissonClientV3")
    public RedissonClient redissonClientV3(){
        Config config = new Config();
        config.useMasterSlaveServers()
                .setMasterAddress("redis://127.0.0.1:6379")
                .addSlaveAddress("redis://127.0.0.1:6389", "redis://127.0.0.1:6332");
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }*/
}