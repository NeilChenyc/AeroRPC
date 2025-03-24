package com.itszt.demo.rpc.registry;

import java.net.InetSocketAddress;

/**
 * 服务注册接口
 */
public interface ServiceRegistry {

    /**
     * 注册服务
     *
     * @param serviceName 服务名称
     * @param address     服务地址
     */
    void register(String serviceName, InetSocketAddress address);

    /**
     * 注销服务
     *
     * @param serviceName 服务名称
     * @param address     服务地址
     */
    void unregister(String serviceName, InetSocketAddress address);
}