package cn.shh.test.redisson.distributedserver;

import org.redisson.api.annotation.REntity;
import org.redisson.api.annotation.RId;
import org.redisson.api.annotation.RIndex;

/**
 * 分布式实时对象
 *
 * 注意：
 *      - @REntity和@RId两个注解是分布式实时对象的必要条件。
 *      - 使用分布式实时对象前，需要先通过Redisson服务将指定的对象连接（attach），合并（merge）
 *          或持久化（persist）到Redis里。
 *
 * 使用方法：
 */
@REntity
public class MyLiveObject1 {
    @RId
    private Long id;
    @RIndex
    private String name;

    public MyLiveObject1() {
    }

    public MyLiveObject1(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "MyLiveObject1{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
