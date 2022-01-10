package com.google.common.sunjinwei;

import com.google.common.cache.*;
import junit.framework.TestCase;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class Demo_01 extends TestCase {

    /**
     * CacheLoader的创建方式
     */
    public void testCache01() throws Exception {

        // 1 LoadingCache：在缓存项不存在时 可以自动加载缓存
        LoadingCache<Integer, String> cache = CacheBuilder.newBuilder()
                // 2 设置缓存写入后2s失效
                .expireAfterWrite(2, TimeUnit.SECONDS)
                // 3 设置缓存容器的初始容量为10
                .initialCapacity(10)
                // 4 设置缓存的最大容量为100
                .maximumSize(100)
                // 5 统计缓存的命中率
                .recordStats()
                // 6 设置缓存的移除通知
                .removalListener(new RemovalListener<Object, Object>() {
                    @Override
                    public void onRemoval(RemovalNotification<Object, Object> notification) {
                        System.out.println(notification.getKey() + " is removed and cause is " + notification.getCause());
                    }
                })
                // 6 指定CacheLoader 在缓存不存在时 通过CacheLoader自动加载缓存
                .build(new CacheLoader<Integer, String>() {
                    @Override
                    public String load(Integer key) throws Exception {
                        System.out.println("loading data : " + key);
                        return "a";
                    }
                });

        Integer key = 1;

        for (int i = 0; i < 20; i++) {
            String s = cache.get(key);
            System.out.println(s);
            Thread.sleep(2000);
        }

        System.out.println("=========");

        for (int i = 0; i < 10; i++) {
            System.out.println(cache.get(key));
        }

        System.out.println("cache stats====" + cache.stats());

    }

    /**
     * Callable的方式创建
     */
    public void testCache02() throws Exception {

        Cache<Integer, String> cache = CacheBuilder.newBuilder().maximumSize(1).build();
        String val = cache.get(1, new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "aaaa";
            }
        });

        System.out.println(val);

    }


}
