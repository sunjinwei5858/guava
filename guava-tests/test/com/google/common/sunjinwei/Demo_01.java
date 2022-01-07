package com.google.common.sunjinwei;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import junit.framework.TestCase;

import java.util.concurrent.TimeUnit;

public class Demo_01 extends TestCase {

    /**
     * 根据访问时间的过期策略
     */
    public void testCache01() {

        Cache<Integer, String> cache = CacheBuilder.newBuilder().expireAfterAccess(2, TimeUnit.SECONDS).build();

        Integer key = 1;
        String val = "a";
        cache.put(key, val);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        val = cache.getIfPresent(key);

        System.out.println(val);


    }


}
