package com.itszt.demo.rpc.server;

import com.itszt.demo.rpc.common.model.RpcRequest;
import com.itszt.demo.rpc.common.model.RpcResponse;
import com.itszt.demo.rpc.common.serializer.Serializer;
import com.itszt.demo.rpc.common.serializer.SerializerFactory;
import com.itszt.demo.rpc.common.serializer.SerializerType;
import com.itszt.demo.rpc.monitor.RpcMonitor;
import com.itszt.demo.rpc.protocol.MessageHeader;
import com.itszt.demo.rpc.protocol.MessageType;
import com.itszt.demo.rpc.protocol.RpcMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * RPC服务端处理器
 */
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcMessage> {

    private static final Logger logger = LoggerFactory.getLogger(RpcServerHandler.class);

    private final Map<String, Object> serviceMap;

    public RpcServerHandler(Map<String, Object> serviceMap) {
        this.serviceMap = serviceMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage msg) throws Exception {
        byte messageType = msg.getHeader().getMessageType();
        byte serializerType = msg.getHeader().getSerializerType();
        long requestId = msg.getHeader().getRequestId();

        // 处理心跳请求
        if (messageType == MessageType.HEARTBEAT_REQUEST.getCode()) {
            logger.debug("收到心跳请求: {}", requestId);
            // 发送心跳响应
            RpcMessage response = new RpcMessage();
            MessageHeader header = new MessageHeader();
            header.setRequestId(requestId);
            header.setMessageType((byte) MessageType.HEARTBEAT_RESPONSE.getCode());
            header.setBodyLength(0);
            response.setHeader(header);
            ctx.writeAndFlush(response);
            return;
        }

        // 处理RPC请求
        if (messageType == MessageType.REQUEST.getCode()) {
            // 反序列化请求
            Serializer serializer = SerializerFactory.getSerializer(SerializerType.valueOf(serializerType));
            RpcRequest request = serializer.deserialize((byte[]) msg.getBody(), RpcRequest.class);
            logger.debug("收到请求: {}", request);

            // 记录监控信息
            String serviceName = request.getInterfaceName();
            RpcMonitor.getInstance().recordCall(serviceName);
            
            // 处理请求
            RpcResponse<?> response;
            try {
                Object result = handleRequest(request);
                response = RpcResponse.success(result, request.getRequestId(), serviceName);
                // 记录成功
                RpcMonitor.getInstance().recordSuccess(serviceName);
            } catch (Exception e) {
                logger.error("处理请求异常", e);
                response = RpcResponse.fail(e.getMessage(), request.getRequestId(), serviceName);
                // 记录失败
                RpcMonitor.getInstance().recordFail(serviceName);
            }

            // 发送响应
            byte[] responseBody = serializer.serialize(response);
            RpcMessage responseMessage = new RpcMessage();
            MessageHeader header = new MessageHeader();
            header.setRequestId(requestId);
            header.setSerializerType(serializerType);
            header.setMessageType((byte) MessageType.RESPONSE.getCode());
            header.setBodyLength(responseBody.length);
            responseMessage.setHeader(header);
            responseMessage.setBody(responseBody);
            ctx.writeAndFlush(responseMessage);
        }
    }

    /**
     * 处理请求
     */
    private Object handleRequest(RpcRequest request) throws Exception {
        String serviceName = request.getInterfaceName();
        String version = request.getVersion();
        String group = request.getGroup();

        // 获取服务实例
                // 构造复合键
        String serviceKey = String.format("%s:%s:%s", serviceName, version, group);
        Object serviceBean = serviceMap.get(serviceKey);
        if (serviceBean == null) {
            throw new RuntimeException(String.format("服务不存在: 服务名=%s, 版本=%s, 分组=%s", serviceName, version, group));
        }

        // 参数类型校验
        Class<?>[] paramTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();
        if (paramTypes.length != parameters.length) {
            throw new IllegalArgumentException("参数数量不匹配，期望" + paramTypes.length + "个，实际" + parameters.length + "个");
        }
        for (int i = 0; i < paramTypes.length; i++) {
            if (!paramTypes[i].isInstance(parameters[i])) {
                throw new IllegalArgumentException(String.format("第%d个参数类型不匹配，期望：%s，实际：%s",
                        i + 1, paramTypes[i].getName(), parameters[i].getClass().getName()));
            }
        }

        // 获取方法
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Method method = serviceClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);

        // 调用方法
        return method.invoke(serviceBean, request.getParameters());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("服务端异常", cause);
        ctx.close();
    }
}