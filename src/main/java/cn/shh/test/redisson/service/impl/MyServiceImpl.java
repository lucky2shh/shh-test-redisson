package cn.shh.test.redisson.service.impl;

import cn.shh.test.redisson.service.MyService;
import org.springframework.stereotype.Service;

@Service
public class MyServiceImpl implements MyService {
    @Override
    public void doSomeThing() {
        System.out.println(Thread.currentThread().getName() + " MyServiceImpl doSomeThing()");
    }
}
