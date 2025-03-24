package com.itszt.demo.rpc.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * RPC重试处理器
 * 用于处理RPC调用失败时的重试逻辑
 */
public class RpcRetryHandler {

    private static final Logger logger = LoggerFactory.getLogger(RpcRetryHandler.class);

    /**
     * 最大重试次数
     */
    private final int maxRetries;

    /**
     * 重试间隔（毫秒）
     */
    private final long retryInterval;

    /**
     * 是否使用指数退避策略
     */
    private final boolean useExponentialBackoff;

    public RpcRetryHandler() {
        this(3, 1000, true);
    }

    public RpcRetryHandler(int maxRetries, long retryInterval, boolean useExponentialBackoff) {
        this.maxRetries = maxRetries;
        this.retryInterval = retryInterval;
        this.useExponentialBackoff = useExponentialBackoff;
    }

    /**
     * 执行带重试的操作
     *
     * @param retryCallback 重试回调
     * @param <T>           返回类型
     * @return 操作结果
     * @throws Exception 操作异常
     */
    public <T> T execute(RetryCallback<T> retryCallback) throws Exception {
        int retryCount = 0;
        Exception lastException = null;

        while (retryCount <= maxRetries) {
            try {
                // 执行操作
                return retryCallback.doWithRetry(retryCount);
            } catch (Exception e) {
                lastException = e;
                retryCount++;

                // 如果已经达到最大重试次数，则抛出异常
                if (retryCount > maxRetries) {
                    logger.error("重试次数已达到最大值 {}, 操作失败", maxRetries, e);
                    throw e;
                }

                // 计算重试间隔
                long interval = retryInterval;
                if (useExponentialBackoff) {
                    // 指数退避策略：interval = retryInterval * 2^(retryCount-1)
                    interval = retryInterval * (long) Math.pow(2, retryCount - 1);
                }

                logger.warn("操作失败，将在 {}ms 后进行第 {} 次重试, 异常: {}", interval, retryCount, e.getMessage());

                try {
                    // 等待重试间隔
                    TimeUnit.MILLISECONDS.sleep(interval);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("重试被中断", ie);
                }
            }
        }

        // 不应该到达这里，但为了编译通过
        throw lastException;
    }

    /**
     * 重试回调接口
     *
     * @param <T> 返回类型
     */
    public interface RetryCallback<T> {
        /**
         * 执行重试操作
         *
         * @param retryCount 当前重试次数
         * @return 操作结果
         * @throws Exception 操作异常
         */
        T doWithRetry(int retryCount) throws Exception;
    }
}