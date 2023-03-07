package cn.shh.test.redisson.distributedserver;

import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.api.annotation.RInject;

import java.io.Serializable;
import java.util.concurrent.Callable;

public class CallableTask implements Callable<Integer>, Serializable {
    private static final long serialVersionUID = 4353465346562L;
    @RInject
    private RedissonClient redissonClient;

    @Override
    public Integer call() throws Exception {
        RMap<String, Integer> map = redissonClient.getMap("test-executorservice-callable");
        Integer result = 0;
        for (Integer value : map.values()) {
            result += value;
        }
        return result;
    }
}
