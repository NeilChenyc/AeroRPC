package com.itszt.demo.rpc.client;

import com.itszt.demo.rpc.protocol.MessageHeader;
import com.itszt.demo.rpc.protocol.MessageType;
import com.itszt.demo.rpc.protocol.RpcMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * RPC客户端心跳处理器
 */
public class RpcClientHeartbeatHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(RpcClientHeartbeatHandler.class);

    private final AtomicLong requestIdGenerator = new AtomicLong(0);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE) {
                // 发送心跳请求
                RpcMessage message = new RpcMessage();
                MessageHeader header = new MessageHeader();
                header.setRequestId(requestIdGenerator.incrementAndGet());
                header.setMessageType((byte)MessageType.HEARTBEAT_REQUEST.getCode());
                header.setBodyLength(0);
                message.setHeader(header);
                ctx.writeAndFlush(message);
                logger.debug("发送心跳请求: {}", header.getRequestId());
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}