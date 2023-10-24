package cn.shh.test.redisson.distributedlock;

import org.junit.jupiter.api.Test;
import org.redisson.RedissonMultiLock;
import org.redisson.RedissonRedLock;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁和同步器
 */
@SpringBootTest
public class TestDistributedLock {
    @Qualifier("redissonClientV1")
    @Autowired
    private RedissonClient redisson;

    /**
     * 重入锁 ReentrantLock
     */
    @Test
    public void testReentrantLock(){
        CountDownLatch latch = new CountDownLatch(1);

        new Thread(() -> {
            RLock lock = redisson.getLock("test:lock:ReentrantLock");
            lock.lock();
            System.out.println(Thread.currentThread().getName() + "正在运行。");
            try {
                latch.countDown();
                TimeUnit.SECONDS.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            lock.unlock();
        }, "t1").start();

        // 等待线程启动成功。
        try {latch.await();} catch (InterruptedException e) {throw new RuntimeException(e);}

        // 测试锁的排他性（线程t1执行完毕释放了锁，程序才会继续执行。）
        System.out.println("另一个线程过来了....");
        RLock lock = redisson.getLock("test:lock:ReentrantLock");
        lock.lock();
        System.out.println(Thread.currentThread().getName() + "正在运行。");
        lock.unlock();
    }

    /**
     * 公平锁 FairLock
     */
    @Test
    public void testFairLock(){
        RLock fairLock = redisson.getFairLock("test:lock:FairLock");
        fairLock.lock();
        try {TimeUnit.SECONDS.sleep(1000);} catch (InterruptedException e) {throw new RuntimeException(e);}
        fairLock.unlock();
    }

    /**
     * 联锁 MultiLock
     */
    @Test
    public void testMultiLock(){
        RLock lock = redisson.getLock("test:lock:MultiLock1");
        RLock lock2 = redisson.getLock("test:lock:MultiLock2");
        RLock lock3 = redisson.getLock("test:lock:MultiLock3");
        RedissonMultiLock multiLock = new RedissonMultiLock(lock, lock2, lock3);
        // 同时加锁lock、lock2、lock3，全部加锁成功才算加锁成功。
        multiLock.lock();
        // 给lock，lock2，lock3加锁，如果没有手动解开的话，10秒钟后将会自动解开
        lock.lock(10, TimeUnit.SECONDS);
        try {
            // 为加锁等待100秒时间，并在加锁成功10秒钟后自动解开
            boolean res = lock.tryLock(100, 10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // 业务操作
        multiLock.unlock();
    }

    /**
     * 红锁 RedLock
     */
    @Test
    public void testRedLock(){
        RLock lock = redisson.getLock("test:lock:RedLock1");
        RLock lock2 = redisson.getLock("test:lock:RedLock2");
        RLock lock3 = redisson.getLock("test:lock:RedLock3");
        RedissonRedLock redLock = new RedissonRedLock(lock, lock2, lock3);
        // 同时加锁lock、lock2、lock3，大部分节点加锁成功就算加锁成功。
        redLock.lock();
        // 业务操作...
        // 最终解锁
        redLock.unlock();
    }

    /**
     * 读写锁 ReadWriteLock
     *
     * 获取到读锁后，不能再次获取写锁。
     * 获取到写锁后，可以再次获取读锁。
     */
    @Test
    public void testReadWriteLock(){
        RReadWriteLock readWriteLock = redisson.getReadWriteLock("test:lock:ReadWriteLock");

        readWriteLock.writeLock().lock();
        System.out.println("获取写锁成功。");

        readWriteLock.readLock().lock();
        System.out.println("获取读锁成功。");

        readWriteLock.writeLock().unlock();
        System.out.println("释放写锁成功。");

        readWriteLock.readLock().unlock();
        System.out.println("释放读锁成功。");
    }

    /**
     * 信号量
     */
    @Test
    public void testSemaphore() throws InterruptedException {
        RSemaphore semaphore = redisson.getSemaphore("test:Semaphore");

        // 获取信号量
        semaphore.acquire();    // 同步阻塞
        semaphore.acquireAsync();   // 异步非阻塞
        semaphore.acquire(23);  // 固定超时时间内同步阻塞
        semaphore.tryAcquire();
        semaphore.tryAcquireAsync();
        semaphore.tryAcquire(23, TimeUnit.SECONDS);
        semaphore.tryAcquireAsync(23, TimeUnit.SECONDS);

        // 释放信号量
        semaphore.release(10);
        semaphore.release();
        semaphore.releaseAsync();
    }

    /**
     * 过期信号量 PermitExpirableSemaphore
     */
    @Test
    public void testPermitExpirableSemaphore() throws InterruptedException {
        RPermitExpirableSemaphore semaphore = redisson.getPermitExpirableSemaphore("test:PermitExpirableSemaphore");
        String permitId = semaphore.acquire();
        // 获取一个信号，有效期只有2秒钟。
        String permitId2 = semaphore.acquire(2, TimeUnit.SECONDS);
        // 业务操作
        semaphore.release(permitId2);
    }

    /**
     * 闭锁 CountDownLatch
     */
    @Test
    public void testCountDownLatch() throws InterruptedException {
        RCountDownLatch countDownLatch = redisson.getCountDownLatch("test:CountDownLatch");
        countDownLatch.trySetCount(1);
        countDownLatch.await();

        // 其它线程中执行
        RCountDownLatch countDownLatch2 = redisson.getCountDownLatch("test-CountDownLatch");
        countDownLatch.countDown();
    }
}