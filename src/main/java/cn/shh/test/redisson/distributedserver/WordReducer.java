package cn.shh.test.redisson.distributedserver;

import org.redisson.api.mapreduce.RReducer;
import java.util.Iterator;

public class WordReducer implements RReducer<String, Integer> {
    @Override
    public Integer reduce(String reducedKey, Iterator<Integer> iter) {
        int sum = 0;
        while (iter.hasNext()) {
            Integer i = (Integer) iter.next();
            sum += i;
        }
        return sum;
    }
}
