package cn.shh.test.redisson.distributedserver;

import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.redisson.api.annotation.RInject;

import java.io.Serializable;

public class RunnableTask implements Runnable, Serializable {
    private static final long serialVersionUID = 43539787528346562L;
    @RInject
    private RedissonClient redissonClient;
    private long param;

    public RunnableTask() {
    }

    public RunnableTask(long param) {
        this.param = param;
    }

    @Override
    public void run() {
        RAtomicLong atomic = redissonClient.getAtomicLong("test-executorservice-runnable");
        System.out.println("RunnableTask run(): " + atomic.addAndGet(param));
    }
}
