package com.itszt.demo.rpc.registry.zk;

import com.itszt.demo.rpc.common.model.RpcRequest;
import com.itszt.demo.rpc.loadbalance.LoadBalance;
import com.itszt.demo.rpc.loadbalance.impl.RandomLoadBalance;
import com.itszt.demo.rpc.registry.ServiceDiscovery;
import com.itszt.demo.rpc.registry.ServiceRegistry;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 基于Zookeeper的服务注册与发现
 */
public class ZkServiceRegistry implements ServiceRegistry, ServiceDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(ZkServiceRegistry.class);

    private final Map<String, List<String>> serviceAddressMap = new ConcurrentHashMap<>();
    private final LoadBalance loadBalance;
    private final CuratorFramework zkClient;

    private static final String ZK_REGISTRY_PATH = "/rpc";

    public ZkServiceRegistry(String zkAddress) {
        this(zkAddress, new RandomLoadBalance());
    }

    public ZkServiceRegistry(String zkAddress, LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
        // 创建Zookeeper客户端
        this.zkClient = CuratorFrameworkFactory.builder()
                .connectString(zkAddress)
                .sessionTimeoutMs(60000)
                .connectionTimeoutMs(15000)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        this.zkClient.start();
        try {
            // 等待连接建立
            if (!this.zkClient.blockUntilConnected(30, TimeUnit.SECONDS)) {
                throw new RuntimeException("连接Zookeeper超时");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("连接Zookeeper被中断", e);
        }
    }

    @Override
    public void register(String serviceName, InetSocketAddress address) {
        try {
            // 创建服务节点
            String servicePath = ZK_REGISTRY_PATH + "/" + serviceName;
            if (zkClient.checkExists().forPath(servicePath) == null) {
                zkClient.create().creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(servicePath);
            }
            // 创建地址节点
            String addressPath = servicePath + "/" + address.getHostString() + ":" + address.getPort();
            zkClient.create().withMode(CreateMode.EPHEMERAL).forPath(addressPath);
            logger.info("注册服务: {}, 地址: {}", serviceName, address);
        } catch (Exception e) {
            logger.error("注册服务异常", e);
        }
    }

    @Override
    public void unregister(String serviceName, InetSocketAddress address) {
        try {
            String servicePath = ZK_REGISTRY_PATH + "/" + serviceName;
            String addressPath = servicePath + "/" + address.getHostString() + ":" + address.getPort();
            
            // 删除地址节点
            zkClient.delete().forPath(addressPath);
            logger.info("注销服务: {}, 地址: {}", serviceName, address);
            
            // 检查服务节点是否为空
            if (zkClient.getChildren().forPath(servicePath).isEmpty()) {
                zkClient.delete().forPath(servicePath);
                logger.info("清理空服务节点: {}", servicePath);
            }
        } catch (Exception e) {
            logger.error("注销服务异常", e);
        }
    }

    @Override
    public String lookupService(RpcRequest request) {
        String serviceName = request.getInterfaceName();
        // 从缓存中获取服务地址列表
        List<String> addressList = serviceAddressMap.get(serviceName);
        if (addressList == null || addressList.isEmpty()) {
            // 从Zookeeper获取服务地址列表
            try {
                String servicePath = ZK_REGISTRY_PATH + "/" + serviceName;
                addressList = zkClient.getChildren().forPath(servicePath);
                // 更新缓存
                serviceAddressMap.put(serviceName, addressList);
                // 注册监听
                registerWatcher(serviceName, servicePath);
            } catch (Exception e) {
                logger.error("获取服务地址异常", e);
                return null;
            }
        }
        // 负载均衡选择服务地址
        return loadBalance.select(addressList, request);
    }

    /**
     * 注册监听
     */
    private void registerWatcher(String serviceName, String servicePath) throws Exception {
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);
        pathChildrenCache.getListenable().addListener((client, event) -> {
            if (event.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED ||
                    event.getType() == PathChildrenCacheEvent.Type.CHILD_REMOVED ||
                    event.getType() == PathChildrenCacheEvent.Type.CHILD_UPDATED) {
                // 更新缓存
                List<String> addressList = client.getChildren().forPath(servicePath);
                serviceAddressMap.put(serviceName, addressList);
            }
        });
        pathChildrenCache.start();
    }

    /**
     * 关闭
     */
    public void close() {
        this.zkClient.close();
    }
}