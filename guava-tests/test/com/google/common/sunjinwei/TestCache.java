package com.google.common.sunjinwei;

import com.google.common.cache.*;
import junit.framework.TestCase;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class TestCache extends TestCase {

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

    /**
     * refreshAfterWrite使用示例：
     * 场景1：线程1和线程2并发去请求，只有一个线程进行load数据，其余线程阻塞。【保证只有一个线程才能load数据，避免了热点key的大量请求给后端造成的性能压力】
     * 场景2：缓存未过期 线程3和线程4都能命中缓存。
     * 场景3：缓存已经过期 线程5和线程6并发去请求 线程5进行load新数据，线程6返回旧值。【refreshAfterWrite返回旧值的处理方式解决了大量线程阻塞等待的问题】
     */
    public void testCache03() throws InterruptedException {

        LoadingCache<Object, Object> cache = CacheBuilder.newBuilder()
                .refreshAfterWrite(5, TimeUnit.SECONDS)
                .build(new CacheLoader<Object, Object>() {
                    @Override
                    public Object load(Object key) throws Exception {
                        System.out.println(Thread.currentThread().getName() + ", load data begin");
                        Thread.sleep(3000);
                        System.out.println(Thread.currentThread().getName() + ", load data end");
                        return key.toString() + System.currentTimeMillis();
                    }
                });

        // 场景1：线程1和线程2并发去请求，只有一个线程进行load数据，其余线程阻塞。
        Thread thread1 = createThread(1, cache);
        Thread thread2 = createThread(2, cache);

        thread1.join();
        thread2.join();

        System.out.println("==========");
        // 缓存未过期 线程3和线程4都能命中缓存。
        Thread thread3 = createThread(3, cache);
        Thread thread4 = createThread(4, cache);
        thread3.join();
        thread4.join();

        System.out.println("=========");
        // 缓存已经过期 线程5和线程6并发去请求 线程5进行load新数据，线程6返回旧值。
        Thread.sleep(5000);
        Thread thread5 = createThread(5, cache);
        Thread thread6 = createThread(6, cache);
        thread5.join();
        thread6.join();
    }

    private Thread createThread(Integer threadNum, LoadingCache<Object, Object> cache) {
        Thread thread = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + ", get data begin");
            try {
                System.out.println(Thread.currentThread().getName() + " get cache is :  " + cache.get(1));
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + ", get data end");
        }, "thread name-" + threadNum);
        thread.start();
        return thread;
    }

}
