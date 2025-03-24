package com.itszt.demo.rpc.client;

import com.itszt.demo.rpc.common.model.RpcResponse;
import com.itszt.demo.rpc.common.serializer.Serializer;
import com.itszt.demo.rpc.common.serializer.SerializerFactory;
import com.itszt.demo.rpc.common.serializer.SerializerType;
import com.itszt.demo.rpc.monitor.RpcMonitor;
import com.itszt.demo.rpc.protocol.MessageType;
import com.itszt.demo.rpc.protocol.RpcMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * RPC客户端处理器
 */
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcMessage> {

    private static final Logger logger = LoggerFactory.getLogger(RpcClientHandler.class);

    private final Map<Long, CompletableFuture<RpcResponse<?>>> pendingRequests;

    public RpcClientHandler(Map<Long, CompletableFuture<RpcResponse<?>>> pendingRequests) {
        this.pendingRequests = pendingRequests;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage msg) throws Exception {
        byte messageType = msg.getHeader().getMessageType();
        long requestId = msg.getHeader().getRequestId();

        // 处理心跳响应
        if (messageType == MessageType.HEARTBEAT_RESPONSE.getCode()) {
            logger.debug("收到心跳响应: {}", requestId);
            return;
        }

        // 处理RPC响应
        if (messageType == MessageType.RESPONSE.getCode()) {
            CompletableFuture<RpcResponse<?>> future = pendingRequests.remove(requestId);
            if (future != null) {
                // 反序列化响应
                byte serializerType = msg.getHeader().getSerializerType();
                Serializer serializer = SerializerFactory.getSerializer(SerializerType.valueOf(serializerType));
                RpcResponse<?> response = serializer.deserialize((byte[]) msg.getBody(), RpcResponse.class);
                
                // 记录监控信息
                String serviceName = response.getServiceName();
                if (serviceName != null) {
                    if (response.getCode() == RpcResponse.SUCCESS_CODE) {
                        RpcMonitor.getInstance().recordSuccess(serviceName);
                    } else {
                        RpcMonitor.getInstance().recordFail(serviceName);
                    }
                }
                
                // 完成Future
                future.complete(response);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("客户端异常", cause);
        ctx.close();
    }
}