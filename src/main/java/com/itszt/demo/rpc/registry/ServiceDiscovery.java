package com.itszt.demo.rpc.registry;

import com.itszt.demo.rpc.common.model.RpcRequest;

/**
 * 服务发现接口
 */
public interface ServiceDiscovery {

    /**
     * 查找服务
     *
     * @param request RPC请求
     * @return 服务地址，格式为host:port
     */
    String lookupService(RpcRequest request);
}