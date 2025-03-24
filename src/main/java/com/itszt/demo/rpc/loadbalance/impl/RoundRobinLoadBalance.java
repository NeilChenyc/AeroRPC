package com.itszt.demo.rpc.loadbalance.impl;

import com.itszt.demo.rpc.common.model.RpcRequest;
import com.itszt.demo.rpc.loadbalance.LoadBalance;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询负载均衡实现
 */
public class RoundRobinLoadBalance implements LoadBalance {

    private final AtomicInteger atomicInteger = new AtomicInteger(0);

    @Override
    public String select(List<String> serviceAddresses, RpcRequest request) {
        if (serviceAddresses == null || serviceAddresses.isEmpty()) {
            return null;
        }
        if (serviceAddresses.size() == 1) {
            return serviceAddresses.get(0);
        }
        
        int size = serviceAddresses.size();
        // 使用CAS操作保证原子性，避免多线程问题
        int current;
        int next;
        do {
            current = atomicInteger.get();
            next = (current + 1) % size;
        } while (!atomicInteger.compareAndSet(current, next));
        
        return serviceAddresses.get(next);
    }
}