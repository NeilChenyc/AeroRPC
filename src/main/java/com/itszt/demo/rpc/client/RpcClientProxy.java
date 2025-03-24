package com.itszt.demo.rpc.client;

import com.itszt.demo.rpc.common.model.RpcRequest;
import com.itszt.demo.rpc.common.model.RpcResponse;
import com.itszt.demo.rpc.registry.ServiceDiscovery;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RPC客户端代理
 */
public class RpcClientProxy implements InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(RpcClientProxy.class);
    
    private final ServiceDiscovery serviceDiscovery;
    private final RpcClientPool clientPool;
    private final long timeout;
    private final String version;
    private final String group;
    private final RpcRetryHandler retryHandler;

    public RpcClientProxy(ServiceDiscovery serviceDiscovery, RpcClientPool clientPool) {
        this(serviceDiscovery, clientPool, 5000, "", "");
    }

    public RpcClientProxy(ServiceDiscovery serviceDiscovery, RpcClientPool clientPool, long timeout, String version, String group) {
        this(serviceDiscovery, clientPool, timeout, version, group, new RpcRetryHandler());
    }
    
    public RpcClientProxy(ServiceDiscovery serviceDiscovery, RpcClientPool clientPool, long timeout, String version, String group, RpcRetryHandler retryHandler) {
        this.serviceDiscovery = serviceDiscovery;
        this.clientPool = clientPool;
        this.timeout = timeout;
        this.version = version;
        this.group = group;
        this.retryHandler = retryHandler;
    }

    /**
     * 创建代理
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 如果是Object类的方法，直接调用
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }
        
        // 创建RPC请求
        final RpcRequest request = createRequest(method, args);
        
        try {
            // 使用重试处理器执行RPC调用
            return retryHandler.execute(retryCount -> {
                // 查找服务地址（每次重试都重新查找，以便负载均衡和服务发现）
                String serviceAddress = serviceDiscovery.lookupService(request);
                if (serviceAddress == null) {
                    throw new RuntimeException("服务不可用: " + request.getInterfaceName());
                }
                
                // 获取客户端
                RpcClient client = clientPool.getClient(serviceAddress);
                
                // 如果不是第一次调用，重新生成请求ID
                if (retryCount > 0) {
                    request.setRequestId(UUID.randomUUID().toString());
                    logger.info("重试调用服务: {}, 方法: {}, 重试次数: {}", 
                            request.getInterfaceName(), request.getMethodName(), retryCount);
                }
                
                // 发送请求
                CompletableFuture<RpcResponse<?>> future = client.sendRequest(request);
                
                // 等待响应
                try {
                    RpcResponse<?> response = future.get(timeout, TimeUnit.MILLISECONDS);
                    if (response.getCode() == RpcResponse.SUCCESS_CODE) {
                        return response.getData();
                    } else {
                        throw new RuntimeException("调用失败: " + response.getMessage());
                    }
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    throw new RuntimeException("调用异常: " + e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            logger.error("RPC调用失败, 服务: {}, 方法: {}, 异常: {}", 
                    request.getInterfaceName(), request.getMethodName(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * 创建RPC请求
     *
     * @param method 方法
     * @param args   参数
     * @return RPC请求
     */
    private RpcRequest createRequest(Method method, Object[] args) {
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setInterfaceName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);
        request.setVersion(version);
        request.setGroup(group);
        return request;
    }
}