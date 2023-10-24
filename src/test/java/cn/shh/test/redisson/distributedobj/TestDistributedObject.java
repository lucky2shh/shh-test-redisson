package cn.shh.test.redisson.distributedobj;

import org.junit.jupiter.api.Test;
import org.redisson.api.*;
import org.redisson.api.listener.MessageListener;
import org.redisson.api.listener.PatternMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 分布式对象
 */
@SpringBootTest
public class TestDistributedObject {
    @Qualifier("redissonClientV1")
    @Autowired
    private RedissonClient redissonClient;

    /**
     * 通用对象桶
     */
    @Test
    public void testObjectBucket(){
        /*RBucket<String> rBucket = redissonClient.getBucket("test-bucket");
        rBucket.set("buckey-entity");
        String val = rBucket.get();
        System.out.println("val: " + val);*/

        // 存
        Map<String, Object> map = new HashMap<>();
        map.put("k1", new String("v1"));
        map.put("k2", new String("v2"));
        map.put("k3", new String("v3"));
        RBuckets rBuckets = redissonClient.getBuckets();
        // 保存所有对象，有一个失败，其它的都放弃。
        // rBuckets.trySet(map);
        // 保存所有对象。
        rBuckets.set(map);

        // 取
        Map<String, Object> mapResult = rBuckets.get("k1", "k2", "k3");
        mapResult.forEach((k, v) -> {
            System.out.println("k/v: " + k + "/" + v);
        });
    }

    /**
     * 二进制流
     */
    @Test
    public void testBinaryStream(){
        RBinaryStream binaryStream = redissonClient.getBinaryStream("test-binary-stream");
        binaryStream.set("hello binary stream".getBytes());

        try {
            InputStream is = binaryStream.getInputStream();
            byte[] bytes = new byte[512];
            is.read(bytes);
            System.out.println("bytes: " + new String(bytes));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            OutputStream os = binaryStream.getOutputStream();
            os.write("hello binary stream 2".getBytes());
            InputStream is = binaryStream.getInputStream();
            byte[] bytes = new byte[512];
            is.read(bytes);
            System.out.println("bytes2: " + new String(bytes));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 地理空间对象桶
     */
    @Test
    public void testGeospatialBucket(){
        RGeo<String> geo = redissonClient.getGeo("test-geospatial-bucket");
        geo.add(new GeoEntry(13.361389, 38.115556, "Palermo"), // 巴勒莫
                new GeoEntry(15.087269, 37.502669, "Catania")); // 卡塔尼亚
        geo.addAsync(37.618423, 55.751244, "Moscow"); // 莫斯科

        Double distance = geo.dist("Palermo", "Catania", GeoUnit.METERS);
        System.out.println("巴勒莫与卡塔尼亚的距离（米）：" + distance);
    }

    /**
     * 位集合
     */
    @Test
    public void testBitSet(){
        RBitSet set = redissonClient.getBitSet("test-bitset");
        set.set(0, true);
        set.set(1812, false);
        System.out.println("set: " + set);
    }

    /**
     * 原子Long数据类型
     */
    @Test
    public void testAtomicLong(){
        RAtomicLong atomicLong = redissonClient.getAtomicLong("test-atomic-long");
        atomicLong.set(3);
        System.out.println("result1: " + atomicLong.incrementAndGet());
        System.out.println("result2: " + atomicLong.decrementAndGet());
    }

    /**
     * 原子Double数据类型
     */
    @Test
    public void testAtomicDouble(){
        RAtomicDouble atomicDouble = redissonClient.getAtomicDouble("test-atomic-double");
        atomicDouble.set(2.61);
        System.out.println("result1: " + atomicDouble.addAndGet(1.11));
        System.out.println("result2: " + atomicDouble.get());
    }

    /**
     * 订阅分发-监听消息
     */
    @Test
    public void testSub(){
        RTopic topic = redissonClient.getTopic("test-topic");
        topic.addListener(String.class, new MessageListener<String>() {
            @Override
            public void onMessage(CharSequence channel, String msg) {
                System.out.println("收到了msg: " + msg);
            }
        });

        RPatternTopic topic1 = redissonClient.getPatternTopic("test-topic-pattern.*");
        int listenerId = topic1.addListener(String.class, new PatternMessageListener<String>() {
            @Override
            public void onMessage(CharSequence pattern, CharSequence channel, String msg) {
                System.out.println("收到了Pattern msg: " + msg);
            }
        });
        System.out.println("正在监听消息......");
        try {
            System.in.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 订阅分发-发布消息
     */
    @Test
    public void testPub(){
        RTopic topic = redissonClient.getTopic("test-topic");
        long pubResult = topic.publish(new String("hello topic"));
        System.out.println("pubResult: " + pubResult);

        RTopic topic2 = redissonClient.getTopic("test-topic-pattern.a");
        long pubResult2 = topic2.publish(new String("hello pattern"));
        System.out.println("pubResult2: " + pubResult2);
    }

    /**
     * 布隆过滤器
     */
    @Test
    public void testBloomFilter(){
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter("test-bloomfilter");
        // 初始化布隆过滤器，预计统计元素数量为55000000，期望误差率为0.03
        bloomFilter.tryInit(55000000L, 0.03);
        bloomFilter.add(new String("field1Value"));
        bloomFilter.add(new String("field2Value"));
        bloomFilter.add(new String("field3Value"));
        System.out.println(bloomFilter.contains(new String("field3Value")));
    }

    /**
     * 基数估计算法 HyperLogLog
     */
    @Test
    public void testHyperLogLog(){
        RHyperLogLog<Integer> log = redissonClient.getHyperLogLog("test-hyperloglog");
        log.add(1);
        log.add(2);
        log.add(3);
        System.out.println(log.count());
    }

    /**
     * Long型累加器
     *
     * 注意：使用完要销毁。
     */
    @Test
    public void testLongAdder(){
        RLongAdder atomicLong = redissonClient.getLongAdder("test-longadder");
        atomicLong.add(12);
        atomicLong.increment();
        atomicLong.decrement();
        System.out.println(atomicLong.sum());   // 12
        // 使用完要销毁。
        atomicLong.destroy();
    }

    /**
     * Double型累加器
     *
     * 注意：使用完要销毁。
     */
    @Test
    public void testDoubleAdder(){
        RDoubleAdder atomicDouble = redissonClient.getDoubleAdder("test-longadder");
        atomicDouble.add(12);
        atomicDouble.increment();
        atomicDouble.decrement();
        System.out.println(atomicDouble.sum());
        // 使用完要销毁。
        atomicDouble.destroy();
    }

    /**
     * 限流器 RateLimiter
     */
    @Test
    public void testRateLimiter(){
        RRateLimiter rateLimiter = redissonClient.getRateLimiter("test-ratelimiter");
        // 初始化，最大流速 = 每1秒钟产生2个令牌
        rateLimiter.trySetRate(RateType.OVERALL, 2, 1, RateIntervalUnit.SECONDS);

        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                rateLimiter.acquire();
                System.out.println(Thread.currentThread().getName() + "正在操作！");
            }, "t" + i).start();
        }

        try {
            System.in.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}