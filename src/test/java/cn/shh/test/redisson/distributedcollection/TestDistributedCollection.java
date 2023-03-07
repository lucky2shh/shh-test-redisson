package cn.shh.test.redisson.distributedcollection;

import org.junit.jupiter.api.Test;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 分布式集合
 */
@SpringBootTest
public class TestDistributedCollection {
    @Qualifier("redissonClientV1")
    @Autowired
    private RedissonClient redisson;

    /**
     * Map
     */
    @Test
    public void testMap() {
        RMap<String, String> map = redisson.getMap("test-map");
        map.put("k1", "v1");
        map.putIfAbsent("k2", "v2");
        map.fastPut("k3", "v3");
        map.fastRemove("k3");

        RFuture<String> putAsyncFuture = map.putAsync("k4", "v4");
        RFuture<Boolean> fastPutAsyncFuture = map.fastPutAsync("k5", "v5");

        map.fastPutAsync("k6", "v6");
        map.fastRemoveAsync("k6");

        System.out.println("map: " + map);
    }

    /**
     * 多值映射
     */
    @Test
    public void testMultimap() {
        RSetMultimap<String, String> map = redisson.getSetMultimap("test-setmultimap");
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k3", "v3");
        RSet<String> getResult = map.get("k1");
        for (String s : getResult) {
            System.out.println("s: " + s);
        }
        RListMultimap<String, String> map2 = redisson.getListMultimap("test-listmultimap");
        map2.put("k1", "v1");
        map2.put("k2", "v2");
        map2.put("k3", "v3");
        RSetMultimapCache<String, String> multimap = redisson.getSetMultimapCache("test-setmultimapcache");
        multimap.put("k1", "v1");
        multimap.put("k2", "v2");
        multimap.put("k3", "v3");
    }

    /**
     * 集合 Set
     */
    @Test
    public void testSet() {
        RSet<String> set = redisson.getSet("test-set");
        set.add("a");
        set.add("b");
        set.remove("a");
        System.out.println("set: " + set);
    }

    /**
     * 有序集合 SortedSet
     */
    @Test
    public void testSortedSet() {
        RSortedSet<Integer> sortedSet = redisson.getSortedSet("test-sortedset");
        sortedSet.add(3);
        sortedSet.add(1);
        sortedSet.add(2);
        sortedSet.removeAsync(0);
        sortedSet.addAsync(5);
        System.out.println("sortedSet: " + sortedSet);
    }

    /**
     * 计分有序集合 ScoredSortedSet
     */
    @Test
    public void testScoredSortedSet() {
        RScoredSortedSet<String> set = redisson.getScoredSortedSet("test-scotedsortedset");
        set.add(0.13, "a");
        set.addAsync(0.251, "b");
        set.add(0.302, "c");
        set.add(0.802, "d");
        set.add(0.502, "e");
        set.pollFirst();
        set.pollLast();

        int index = set.rank("b"); // 获取元素在集合中的位置
        Double score = set.getScore("c"); // 获取元素的评分

        System.out.println("index: " + index);
        System.out.println("score: " + score);
    }

    /**
     * 字典排序集合 LexSortedSet
     */
    @Test
    public void testLexSortedSet() {
        RLexSortedSet set = redisson.getLexSortedSet("test-LexSortedSet");
        set.add("d");
        set.addAsync("e");
        set.add("f");
        set.forEach(System.out::println);
    }

    /**
     * 列表 List
     */
    @Test
    public void testList() {
        RList<String> list = redisson.getList("test-List");
        list.add("a");
        list.add("b");
        list.add("c");
        list.get(0);
        list.remove("b");
        list.forEach(System.out::println);
    }

    /**
     * 队列 Queue
     */
    @Test
    public void testQueue() {
        RQueue<String> queue = redisson.getQueue("test-Queue");
        queue.add("a");
        queue.add("b");
        queue.add("c");
        System.out.println(queue.peek());
        System.out.println(queue.poll());
    }

    /**
     * 延迟队列 DelayedQueue
     *
     * 注意：使用完要销毁
     */
    @Test
    public void testDelayedQueue() {
        /*RQueue<String> distinationQueue = redisson.getQueue("test-DelayedQueue");
        RDelayedQueue<String> delayedQueue = new RedissonDelayedQueue<>();
        // 10秒钟以后将消息发送到指定队列
        delayedQueue.offer("msg1", 10, TimeUnit.SECONDS);
        // 一分钟以后将消息发送到指定队列
        delayedQueue.offer("msg2", 1, TimeUnit.MINUTES);*/
    }

    /**
     * 优先队列 PriorityQueue
     *
     * 注意：使用完要销毁
     */
    @Test
    public void testPriorityQueue() {
        RPriorityQueue<Integer> queue = redisson.getPriorityQueue("test-PriorityQueue");
        queue.add(3);
        queue.add(1);
        queue.add(2);
        System.out.println(queue.poll());
    }
}
