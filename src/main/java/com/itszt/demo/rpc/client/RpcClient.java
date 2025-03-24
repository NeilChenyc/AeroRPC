package com.itszt.demo.rpc.client;

import com.itszt.demo.rpc.codec.RpcDecoder;
import com.itszt.demo.rpc.codec.RpcEncoder;
import com.itszt.demo.rpc.common.model.RpcRequest;
import com.itszt.demo.rpc.common.model.RpcResponse;
import com.itszt.demo.rpc.common.serializer.Serializer;
import com.itszt.demo.rpc.common.serializer.SerializerFactory;
import com.itszt.demo.rpc.common.serializer.SerializerType;
import com.itszt.demo.rpc.monitor.RpcMonitor;
import com.itszt.demo.rpc.protocol.MessageHeader;
import com.itszt.demo.rpc.protocol.MessageType;
import com.itszt.demo.rpc.protocol.RpcMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * RPC客户端
 */
public class RpcClient {

    private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    private final String serverAddress;
    private final SerializerType serializerType;
    private final EventLoopGroup eventLoopGroup;
    private final Bootstrap bootstrap;
    private Channel channel;
    private final AtomicLong requestIdGenerator = new AtomicLong(0);
    private final Map<Long, CompletableFuture<RpcResponse<?>>> pendingRequests = new ConcurrentHashMap<>();

    public RpcClient(String serverAddress, SerializerType serializerType) {
        this.serverAddress = serverAddress;
        this.serializerType = serializerType;
        this.eventLoopGroup = new NioEventLoopGroup();
        this.bootstrap = new Bootstrap();
        this.bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                // 空闲检测
                                .addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS))
                                // 解码器
                                .addLast(new RpcDecoder())
                                // 编码器
                                .addLast(new RpcEncoder())
                                // 心跳处理器
                                .addLast(new RpcClientHeartbeatHandler())
                                // 处理器
                                .addLast(new RpcClientHandler(pendingRequests));
                    }
                })
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
    }

    /**
     * 连接服务器
     */
    public void connect() throws InterruptedException {
        // 解析服务地址
        String[] addressArray = serverAddress.split(":");
        String host = addressArray[0];
        int port = Integer.parseInt(addressArray[1]);
        // 连接服务器
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port)).sync();
        if (future.isSuccess()) {
            this.channel = future.channel();
            logger.info("连接服务器成功: {}", serverAddress);
        } else {
            throw new RuntimeException("连接服务器失败: " + serverAddress);
        }
    }

    /**
     * 发送请求
     */
    public CompletableFuture<RpcResponse<?>> sendRequest(RpcRequest request) {
        if (channel == null || !channel.isActive()) {
            throw new IllegalStateException("未连接到服务器");
        }
        // 创建请求ID
        long requestId = requestIdGenerator.incrementAndGet();
        // 创建Future
        CompletableFuture<RpcResponse<?>> future = new CompletableFuture<>();
        pendingRequests.put(requestId, future);
        
        // 记录监控信息
        String serviceName = request.getInterfaceName();
        RpcMonitor.getInstance().recordCall(serviceName);
        // 创建消息
        RpcMessage message = new RpcMessage();
        MessageHeader header = new MessageHeader();
        header.setRequestId(requestId);
        header.setSerializerType((byte)serializerType.getCode());
        header.setMessageType((byte) MessageType.REQUEST.getCode());
        message.setHeader(header);
        // 序列化请求体
        Serializer serializer = SerializerFactory.getSerializer(serializerType);
        byte[] body = serializer.serialize(request);
        header.setBodyLength(body.length);
        message.setBody(body);
        // 发送请求
        channel.writeAndFlush(message);
        return future;
    }

    /**
     * 关闭客户端
     */
    public void close() {
        if (channel != null) {
            channel.close();
        }
        eventLoopGroup.shutdownGracefully();
        logger.info("客户端关闭: {}", serverAddress);
    }
}