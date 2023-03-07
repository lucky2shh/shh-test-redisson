package cn.shh.test.redisson.extrafeatures;

import org.junit.jupiter.api.Test;
import org.redisson.api.*;
import org.redisson.connection.ConnectionListener;
import org.redisson.transaction.TransactionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * 额外功能
 */
@SpringBootTest
public class TestExtraFeatures {
    @Autowired
    private RedissonClient redisson;

    /**
     * 对节点的操作
     */
    @Test
    public void testOperNode(){
        NodesGroup nodesGroup = redisson.getNodesGroup();
        nodesGroup.addConnectionListener(new ConnectionListener() {
            public void onConnect(InetSocketAddress addr) {
                // Redis节点连接成功
                System.out.println("redis连接成功：" + addr);
            }

            public void onDisconnect(InetSocketAddress addr) {
                // Redis节点连接断开
                System.out.println("redis连接断开：" + addr);
            }
        });

        // ping 测试
        System.out.println("==========ping测试==========");
        NodesGroup nodesGroup2 = redisson.getNodesGroup();
        Collection<Node> allNodes = nodesGroup2.getNodes();
        for (Node n : allNodes) {
            n.ping();
        }
        nodesGroup2.pingAll();

        try {
            System.in.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 对象化操作
     */
    @Test
    public void testCollectionOper(){
        RMap<RSet<RList>, RList<RMap>> map = redisson.getMap("test:collection:myMap");
        RSet<RList> set = redisson.getSet("test:collection:mySet");
        RList<RMap> list = redisson.getList("test:collection:myList");

        map.put(set, list);
        // 在特殊引用对象的帮助下，我们甚至可以构建一个循环引用，这是通过普通序列化方式实现不了的。
        set.add(list);
        list.add(map);

        System.out.println("map: " + map);
        System.out.println("set: " + set);
        System.out.println("list: " + list);
    }

    /**
     * 命令批量执行
     */
    @Test
    public void testBatchOper(){
        RBatch batch = redisson.createBatch();
        batch.getMap("test:map").fastPutAsync("1", "2");
        batch.getMap("test:map").fastPutAsync("2", "3");
        batch.getMap("test:map").putAsync("2", "5");
        BatchResult res = batch.execute();
        // 或者
        //RFuture<BatchResult<?>> asyncRes = batch.executeAsync();
        List<?> response = res.getResponses();
        res.getSyncedSlaves();
    }

    /**
     * Redisson事务
     */
    @Test
    public void testTransaction(){
        RTransaction transaction = redisson.createTransaction(TransactionOptions.defaults());

        RMap<String, String> map = transaction.getMap("myMap");
        map.put("1", "2");
        String value = map.get("3");
        RSet<String> set = transaction.getSet("mySet");
        set.add(value);

        try {
            transaction.commit();
        } catch(TransactionException e) {
            transaction.rollback();
        }
    }

    /**
     * 脚本执行
     */
    @Test
    public void testEscriptExecute() throws ExecutionException, InterruptedException {
        redisson.getBucket("foo").set("bar");
        String r = redisson.getScript().eval(RScript.Mode.READ_ONLY,
                "return redis.call('get', 'foo')", RScript.ReturnType.VALUE);
        System.out.println("r: " + r);

        // 通过预存的脚本进行同样的操作
        RScript s = redisson.getScript();
        // 首先将脚本保存到所有的Redis主节点
        String res = s.scriptLoad("return redis.call('get', 'foo')");
        // 返回值 res == 282297a0228f48cd3fc6a55de6316f31422f5d17
        System.out.println("res: " + res);

        // 再通过SHA值调用脚本
        Future<Object> r1 = redisson.getScript().evalShaAsync(RScript.Mode.READ_ONLY,
                "282297a0228f48cd3fc6a55de6316f31422f5d17",
                RScript.ReturnType.VALUE, Collections.emptyList());
        System.out.println("get: " + r1.get());
    }
}
