package com.itszt.demo.rpc.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * RPC性能监控
 */
public class RpcMonitor {

    private static final Logger logger = LoggerFactory.getLogger(RpcMonitor.class);

    // 单例实例
    private static final RpcMonitor INSTANCE = new RpcMonitor();

    // 服务调用次数统计
    private final Map<String, LongAdder> serviceCallCountMap = new ConcurrentHashMap<>();

    // 服务调用成功次数统计
    private final Map<String, LongAdder> serviceSuccessCountMap = new ConcurrentHashMap<>();

    // 服务调用失败次数统计
    private final Map<String, LongAdder> serviceFailCountMap = new ConcurrentHashMap<>();

    // 服务调用耗时统计（总耗时）
    private final Map<String, LongAdder> serviceTimeMap = new ConcurrentHashMap<>();

    // 服务最大调用耗时统计
    private final Map<String, AtomicLong> serviceMaxTimeMap = new ConcurrentHashMap<>();
    
    // 服务调用开始时间记录（ThreadLocal避免线程安全问题）
    private final ThreadLocal<Map<String, Long>> startTimeThreadLocal = ThreadLocal.withInitial(HashMap::new);
    
    // 定时统计任务执行器
    private final ScheduledExecutorService scheduledExecutorService;

    private RpcMonitor() {
        // 启动定时统计任务
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "rpc-monitor");
            thread.setDaemon(true);
            return thread;
        });
        startStatisticsTask();
    }

    /**
     * 获取实例
     */
    public static RpcMonitor getInstance() {
        return INSTANCE;
    }

    /**
     * 记录调用开始
     *
     * @param serviceName 服务名称
     */
    public void recordCall(String serviceName) {
        // 增加调用次数
        serviceCallCountMap.computeIfAbsent(serviceName, k -> new LongAdder()).increment();
        // 记录开始时间
        startTimeThreadLocal.get().put(serviceName, System.currentTimeMillis());
    }

    /**
     * 记录调用成功
     *
     * @param serviceName 服务名称
     */
    public void recordSuccess(String serviceName) {
        // 增加成功次数
        serviceSuccessCountMap.computeIfAbsent(serviceName, k -> new LongAdder()).increment();
        // 记录调用耗时
        recordTime(serviceName);
    }

    /**
     * 记录调用失败
     *
     * @param serviceName 服务名称
     */
    public void recordFail(String serviceName) {
        // 增加失败次数
        serviceFailCountMap.computeIfAbsent(serviceName, k -> new LongAdder()).increment();
        // 记录调用耗时
        recordTime(serviceName);
    }

    /**
     * 记录调用耗时
     *
     * @param serviceName 服务名称
     */
    private void recordTime(String serviceName) {
        Map<String, Long> startTimeMap = startTimeThreadLocal.get();
        Long startTime = startTimeMap.remove(serviceName);
        if (startTime != null) {
            long costTime = System.currentTimeMillis() - startTime;
            // 累加总耗时
            serviceTimeMap.computeIfAbsent(serviceName, k -> new LongAdder()).add(costTime);
            // 更新最大耗时
            AtomicLong maxTime = serviceMaxTimeMap.computeIfAbsent(serviceName, k -> new AtomicLong(0));
            long current;
            do {
                current = maxTime.get();
                if (costTime <= current) {
                    break;
                }
            } while (!maxTime.compareAndSet(current, costTime));
        }
        
        // 如果Map为空，清理ThreadLocal，避免内存泄漏
        if (startTimeMap.isEmpty()) {
            startTimeThreadLocal.remove();
        }
    }

    /**
     * 启动定时统计任务
     */
    private void startStatisticsTask() {
        // 每分钟输出一次统计信息
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                // 遍历所有服务
                for (String serviceName : serviceCallCountMap.keySet()) {
                    // 获取统计数据
                    long callCount = serviceCallCountMap.getOrDefault(serviceName, new LongAdder()).sum();
                    long successCount = serviceSuccessCountMap.getOrDefault(serviceName, new LongAdder()).sum();
                    long failCount = serviceFailCountMap.getOrDefault(serviceName, new LongAdder()).sum();
                    long totalTime = serviceTimeMap.getOrDefault(serviceName, new LongAdder()).sum();
                    long maxTime = serviceMaxTimeMap.getOrDefault(serviceName, new AtomicLong(0)).get();

                    // 计算成功率和平均耗时
                    double successRate = callCount > 0 ? (double) successCount / callCount * 100 : 0;
                    double avgTime = callCount > 0 ? (double) totalTime / callCount : 0;

                    // 输出统计信息
                    logger.info("服务: {}, 调用次数: {}, 成功次数: {}, 失败次数: {}, 成功率: {}%, 平均耗时: {}ms, 最大耗时: {}ms",
                            serviceName, callCount, successCount, failCount, String.format("%.2f", successRate), String.format("%.2f", avgTime), maxTime);
                }
            } catch (Exception e) {
                logger.error("统计任务异常", e);
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    /**
     * 关闭监控
     */
    public void shutdown() {
        scheduledExecutorService.shutdown();
        logger.info("关闭监控");
    }
}