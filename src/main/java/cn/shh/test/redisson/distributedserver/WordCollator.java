package cn.shh.test.redisson.distributedserver;

import org.redisson.api.mapreduce.RCollator;

import java.util.Map;

public class WordCollator implements RCollator<String, Integer, Integer> {
    @Override
    public Integer collate(Map<String, Integer> resultMap) {
        int result = 0;
        for (Integer count : resultMap.values()) {
            result += count;
        }
        return result;
    }
}
