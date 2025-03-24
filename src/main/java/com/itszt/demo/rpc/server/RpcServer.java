package com.itszt.demo.rpc.server;

import com.itszt.demo.rpc.codec.RpcDecoder;
import com.itszt.demo.rpc.codec.RpcEncoder;
import com.itszt.demo.rpc.monitor.RpcMonitor;
import com.itszt.demo.rpc.registry.ServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * RPC服务端
 */
public class RpcServer {

    private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);

    private final String host;
    private final int port;
    private final ServiceRegistry serviceRegistry;
    private final Map<String, Object> serviceMap = new HashMap<>();
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public RpcServer(String host, int port, ServiceRegistry serviceRegistry) {
        this.host = host;
        this.port = port;
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * 注册服务
     *
     * @param interfaceClass 接口类
     * @param serviceBean    服务实现类
     */
    public void addService(Class<?> interfaceClass, Object serviceBean) {
        String serviceName = interfaceClass.getName();
        serviceMap.put(serviceName, serviceBean);
        logger.info("添加服务: {}", serviceName);
    }

    /**
     * 启动服务
     */
    public void start() {
        // 使用自定义线程工厂，便于问题排查
        bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("rpc-boss"));
        // 工作线程数默认为CPU核心数的2倍
        int workerThreads = Runtime.getRuntime().availableProcessors() * 2;
        workerGroup = new NioEventLoopGroup(workerThreads, new DefaultThreadFactory("rpc-worker"));
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    // 空闲检测
                                    .addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS))
                                    // 解码器
                                    .addLast(new RpcDecoder())
                                    // 编码器
                                    .addLast(new RpcEncoder())
                                    // 心跳处理器
                                    .addLast(new RpcServerHeartbeatHandler())
                                    // 请求处理器
                                    .addLast(new RpcServerHandler(serviceMap));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    // 启用TCP_NODELAY，禁用Nagle算法，减少延迟
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // 设置接收缓冲区大小
                    .childOption(ChannelOption.SO_RCVBUF, 65536)
                    // 设置发送缓冲区大小
                    .childOption(ChannelOption.SO_SNDBUF, 65536);

            // 绑定端口
            ChannelFuture future = bootstrap.bind(host, port).sync();
            logger.info("服务器启动成功: {}:{}, 工作线程数: {}", host, port, workerThreads);

            // 注册服务
            if (serviceRegistry != null) {
                for (String serviceName : serviceMap.keySet()) {
                    serviceRegistry.register(serviceName, new InetSocketAddress(host, port));
                    logger.info("注册服务到注册中心: {}", serviceName);
                }
            }

            // 等待服务端关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("服务器启动异常", e);
        } finally {
            stop();
        }
    }
    
    /**
     * 停止服务
     */
    public void stop() {
        logger.info("正在关闭服务器...");
        // 注销服务
        if (serviceRegistry != null) {
            for (String serviceName : serviceMap.keySet()) {
                try {
                    serviceRegistry.unregister(serviceName, new InetSocketAddress(host, port));
                    logger.info("成功注销服务: {}", serviceName);
                } catch (Exception e) {
                    logger.error("注销服务失败: {}", serviceName, e);
                }
            }
        }
        // 关闭监控
        RpcMonitor.getInstance().shutdown();
        
        // 优雅关闭线程组
        try {
            if (bossGroup != null) {
                bossGroup.shutdownGracefully(100, 300, TimeUnit.MILLISECONDS).sync();
                logger.info("Boss线程组已关闭");
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully(100, 300, TimeUnit.MILLISECONDS).sync();
                logger.info("Worker线程组已关闭");
            }
        } catch (InterruptedException e) {
            logger.error("线程组关闭异常", e);
            Thread.currentThread().interrupt();
        }
    }
}