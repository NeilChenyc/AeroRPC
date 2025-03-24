package com.itszt.demo.thread;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TestThread {


    static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 10, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            threadPoolExecutor.submit(() -> {
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        while (threadPoolExecutor.getActiveCount() != 0) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("线程未执行完等待1s------------------------------活跃:{},核心:{}", threadPoolExecutor.getActiveCount(),threadPoolExecutor.getCorePoolSize());
        }
        int activeCount = threadPoolExecutor.getActiveCount();

        log.info("end-------------线程执行完------------------------------活跃:{},核心:{}", activeCount,threadPoolExecutor.getCorePoolSize());

    }
}
