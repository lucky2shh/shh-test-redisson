package cn.shh.test.redisson.distributedserver;

import cn.shh.test.redisson.service.MyService;
import cn.shh.test.redisson.service.impl.MyServiceImpl;
import org.junit.jupiter.api.Test;
import org.redisson.api.*;
import org.redisson.api.condition.Conditions;
import org.redisson.api.mapreduce.RMapReduce;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 分布式服务
 */
@SpringBootTest
public class TestDistributedServer {
    @Qualifier("redissonClientV1")
    @Autowired
    private RedissonClient redisson;

    /**
     * 分布式远程服务 RemoteService
     *
     * 服务端（远端）
     */
    @Test
    public void testRemoteService_Server(){
        RRemoteService remoteService = redisson.getRemoteService();
        MyServiceImpl myServiceImpl = new MyServiceImpl();
        // 在调用远程方法以前，应该首先注册远程服务
        // 只注册了一个服务端工作者实例，只能同时执行一个并发调用
        remoteService.register(MyService.class, myServiceImpl);
        // 注册了12个服务端工作者实例，可以同时执行12个并发调用
        //remoteService.register(MyService.class, myServiceImpl, 12);

        try {
            System.out.println("服务启动成功，等待提供服务！");
            System.in.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 分布式远程服务 RemoteService
     *
     * 客户端（本地）
     */
    @Test
    public void testRemoteService_Client(){
        RRemoteService remoteService = redisson.getRemoteService();
        MyService myService = remoteService.get(MyService.class);
        myService.doSomeThing();
    }

    /**
     * 分布式实时对象 LiveObject
     */
    @Test
    public void testLiveObject(){
        RLiveObjectService service = redisson.getLiveObjectService();
        MyLiveObject1 myObject = new MyLiveObject1();
        myObject.setId(1L);
        // 将myObject对象当前的状态持久化到Redis里并与之保持同步。
        myObject = service.persist(myObject);
        System.out.println("myObject: " + myObject);

        MyLiveObject1 myObject2 = new MyLiveObject1(2L);
        // 抛弃myObject对象当前的状态，并与Redis里的数据建立连接并保持同步。
        myObject2 = service.attach(myObject2);
        System.out.println("myObject2: " + myObject2);

        MyLiveObject1 myObject3 = new MyLiveObject1(3L);
        // 将myObject对象当前的状态与Redis里的数据合并之后与之保持同步。
        myObject3 = service.merge(myObject3);
        System.out.println("myObject3: " + myObject3);

        // 通过ID获取分布式实时对象
        MyLiveObject1 myObject1 = service.get(MyLiveObject1.class, 1L);
        System.out.println("myObject1: " + myObject1);

        // 通过索引查找分布式实时对象
        Collection<MyLiveObject1> myObjects = service.find(MyLiveObject1.class,
                Conditions.in("value", "somevalue", "somevalue2"));
        myObjects.forEach(System.out::println);
    }

    /**
     * 分布式执行服务 ExecutorService
     */
    @Test
    public void testExecutorService() throws ExecutionException, InterruptedException {
        ExecutorOptions options = ExecutorOptions.defaults();
        // 指定重新尝试执行任务的时间间隔。
        // ExecutorService的工作节点将等待10分钟后重新尝试执行任务
        // 设定为0则不进行重试
        // 默认值为5分钟
        options.taskRetryInterval(5, TimeUnit.SECONDS);
        RExecutorService executorService = redisson.getExecutorService("test-executorservice-test", options);
        executorService.submit(new RunnableTask(123));

        RExecutorService executorService2 = redisson.getExecutorService("test-executorservice-test", options);
        Future<Integer> future = executorService2.submit(new CallableTask());
        System.out.println("futureResult: " + future.get());
    }

    /**
     * 分布式调度任务服务 SchedulerService
     */
    @Test
    public void testSchedulerService() throws ExecutionException, InterruptedException {
        RScheduledExecutorService executorService = redisson.getExecutorService("test-schedulerservice-test");
        ScheduledFuture<Integer> future = executorService.schedule(new CallableTask(), 3, TimeUnit.SECONDS);
        System.out.println(future.get());

        // 通过CRON表达式设定任务计划
        RScheduledExecutorService executorService2 = redisson.getExecutorService("test-schedulerservice-test");
        executorService2.schedule(new RunnableTask(), CronSchedule.of("10 0/5 * * * ?"));
    }

    /**
     * 分布式映射归纳服务 MapReduce
     */
    @Test
    public void testMapReduce(){
        RMap<String, String> map = redisson.getMap("test-mapreduce-map");
        map.put("line1", "Alice was beginning to get very tired");
        map.put("line2", "of sitting by her sister on the bank and");
        map.put("line3", "of having nothing to do once or twice she");
        map.put("line4", "had peeped into the book her sister was reading");
        map.put("line5", "but it had no pictures or conversations in it");
        map.put("line6", "and what is the use of a book");
        map.put("line7", "thought Alice without pictures or conversation");

        RMapReduce<String, String, String, Integer> mapReduce
                = map.<String, Integer>mapReduce().mapper(new WordMapper()).reducer(new WordReducer());

        // 统计词频
        Map<String, Integer> mapToNumber = mapReduce.execute();
        mapToNumber.forEach((k, v) -> {
            System.out.println("词语/数量：" + k + "/" + v);
        });

        // 统计字数
        Integer totalWordsAmount = mapReduce.execute(new WordCollator());
        System.out.println("字数：" + totalWordsAmount);
    }
}
