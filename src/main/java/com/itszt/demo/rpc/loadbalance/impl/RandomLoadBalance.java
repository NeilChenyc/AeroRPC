package com.itszt.demo.rpc.loadbalance.impl;

import com.itszt.demo.rpc.common.model.RpcRequest;
import com.itszt.demo.rpc.loadbalance.LoadBalance;

import java.util.List;
import java.util.Random;

/**
 * 随机负载均衡实现
 */
public class RandomLoadBalance implements LoadBalance {

    private final Random random = new Random();

    @Override
    public String select(List<String> serviceAddresses, RpcRequest request) {
        if (serviceAddresses == null || serviceAddresses.isEmpty()) {
            return null;
        }
        if (serviceAddresses.size() == 1) {
            return serviceAddresses.get(0);
        }
        return serviceAddresses.get(random.nextInt(serviceAddresses.size()));
    }
}