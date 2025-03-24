package com.itszt.demo.rpc.loadbalance.impl;

import com.itszt.demo.rpc.common.model.RpcRequest;
import com.itszt.demo.rpc.loadbalance.LoadBalance;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 一致性哈希负载均衡实现
 */
public class ConsistentHashLoadBalance implements LoadBalance {

    private final Map<String, ConsistentHashSelector> selectors = new ConcurrentHashMap<>();

    @Override
    public String select(List<String> serviceAddresses, RpcRequest request) {
        if (serviceAddresses == null || serviceAddresses.isEmpty()) {
            return null;
        }
        if (serviceAddresses.size() == 1) {
            return serviceAddresses.get(0);
        }

        // 使用服务名 + 方法名作为key
        String key = request.getInterfaceName() + "." + request.getMethodName();
        int identityHashCode = System.identityHashCode(serviceAddresses);

        ConsistentHashSelector selector = selectors.get(key);
        // 如果服务地址列表有变化，则重新创建选择器
        if (selector == null || selector.identityHashCode != identityHashCode) {
            selectors.put(key, new ConsistentHashSelector(serviceAddresses, 160, identityHashCode));
            selector = selectors.get(key);
        }

        // 使用请求ID作为哈希值的key
        return selector.select(request.getRequestId());
    }

    /**
     * 一致性哈希选择器
     */
    private static class ConsistentHashSelector {

        // 虚拟节点与真实节点的映射关系
        private final TreeMap<Long, String> virtualNodes;
        // 标识服务地址列表的哈希码
        private final int identityHashCode;

        /**
         * 构造方法
         *
         * @param addresses 服务地址列表
         * @param replicaNumber 虚拟节点数
         * @param identityHashCode 服务地址列表的哈希码
         */
        ConsistentHashSelector(List<String> addresses, int replicaNumber, int identityHashCode) {
            this.virtualNodes = new TreeMap<>();
            this.identityHashCode = identityHashCode;

            for (String address : addresses) {
                for (int i = 0; i < replicaNumber / 4; i++) {
                    byte[] digest = md5(address + i);
                    for (int h = 0; h < 4; h++) {
                        long hash = hash(digest, h);
                        virtualNodes.put(hash, address);
                    }
                }
            }
        }

        /**
         * 选择节点
         *
         * @param key 键
         * @return 节点
         */
        String select(String key) {
            byte[] digest = md5(key);
            long hash = hash(digest, 0);
            // 顺时针找到第一个虚拟节点
            Map.Entry<Long, String> entry = virtualNodes.ceilingEntry(hash);
            // 如果没有找到，则取第一个虚拟节点
            if (entry == null) {
                entry = virtualNodes.firstEntry();
            }
            return entry.getValue();
        }

        /**
         * 计算MD5值
         */
        private byte[] md5(String key) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
                return md.digest(key.getBytes(StandardCharsets.UTF_8));
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
        
        /**
         * 计算哈希值
         *
         * @param digest MD5摘要
         * @param index  索引
         * @return 哈希值
         */
        private long hash(byte[] digest, int index) {
            return ((long) (digest[3 + index * 4] & 0xFF) << 24)
                    | ((long) (digest[2 + index * 4] & 0xFF) << 16)
                    | ((long) (digest[1 + index * 4] & 0xFF) << 8)
                    | (digest[index * 4] & 0xFF);
        }
    }
}