package com.itszt.demo.rpc.loadbalance;

import com.itszt.demo.rpc.common.model.RpcRequest;

import java.util.List;

/**
 * 负载均衡接口
 */
public interface LoadBalance {

    /**
     * 从服务地址列表中选择一个
     *
     * @param serviceAddresses 服务地址列表
     * @param request RPC请求
     * @return 选择的服务地址
     */
    String select(List<String> serviceAddresses, RpcRequest request);
}