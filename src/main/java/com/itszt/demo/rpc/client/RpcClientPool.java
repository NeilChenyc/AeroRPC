package com.itszt.demo.rpc.client;

import com.itszt.demo.rpc.common.serializer.SerializerType;
import com.itszt.demo.rpc.monitor.RpcMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RPC客户端连接池
 * 用于管理和复用客户端连接，提高性能
 */
public class RpcClientPool {

    private static final Logger logger = LoggerFactory.getLogger(RpcClientPool.class);

    private final Map<String, RpcClient> clientMap = new ConcurrentHashMap<>();
    private final SerializerType serializerType;
    
    /**
     * 最大连接数
     */
    private final int maxConnections;
    
    /**
     * 连接超时时间（毫秒）
     */
    private final int connectionTimeout;

    public RpcClientPool(SerializerType serializerType) {
        this(serializerType, 10, 5000);
    }

    public RpcClientPool(SerializerType serializerType, int maxConnections, int connectionTimeout) {
        this.serializerType = serializerType;
        this.maxConnections = maxConnections;
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * 获取客户端
     *
     * @param address 服务地址
     * @return 客户端
     */
    public RpcClient getClient(String address) {
        // 从连接池中获取客户端
        RpcClient client = clientMap.get(address);
        if (client == null) {
            synchronized (this) {
                client = clientMap.get(address);
                if (client == null) {
                    // 检查连接数是否超过最大限制
                    if (clientMap.size() >= maxConnections) {
                        logger.warn("连接池已达到最大连接数: {}", maxConnections);
                        // 可以考虑实现连接淘汰策略，这里简单返回错误
                        throw new RuntimeException("连接池已满，无法创建新连接");
                    }
                    
                    // 创建新的客户端
                    client = new RpcClient(address, serializerType);
                    try {
                        // 连接服务器，设置连接超时
                        long startTime = System.currentTimeMillis();
                        client.connect();
                        long costTime = System.currentTimeMillis() - startTime;
                        
                        // 添加到连接池
                        clientMap.put(address, client);
                        logger.info("创建新的客户端连接: {}, 耗时: {}ms", address, costTime);
                    } catch (Exception e) {
                        logger.error("创建客户端连接失败: {}", address, e);
                        throw new RuntimeException("创建客户端连接失败: " + address, e);
                    }
                }
            }
        }
        return client;
    }

    /**
     * 关闭连接池
     */
    public void close() {
        for (RpcClient client : clientMap.values()) {
            try {
                client.close();
            } catch (Exception e) {
                logger.error("关闭客户端连接失败", e);
            }
        }
        clientMap.clear();
        // 关闭监控
        RpcMonitor.getInstance().shutdown();
        logger.info("关闭连接池");
    }
}