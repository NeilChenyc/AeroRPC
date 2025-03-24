package com.itszt.demo.rpc.codec;

import com.itszt.demo.rpc.protocol.MessageHeader;
import com.itszt.demo.rpc.protocol.RpcMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * RPC编码器
 * <p>
 * 消息格式：
 * +-------+-------+---------------+---------------+---------------+---------------+
 * | 魔数   | 版本号 | 序列化类型     | 消息类型        | 状态           | 请求ID         |
 * | 2字节  | 1字节  | 1字节         | 1字节          | 1字节          | 8字节          |
 * +-------+-------+---------------+---------------+---------------+---------------+
 * | 消息体长度                                                                      |
 * | 4字节                                                                          |
 * +-------+-------+---------------+---------------+---------------+---------------+
 * | 消息体                                                                         |
 * | N字节                                                                          |
 * +-------+-------+---------------+---------------+---------------+---------------+
 */
public class RpcEncoder extends MessageToByteEncoder<RpcMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage msg, ByteBuf out) throws Exception {
        MessageHeader header = msg.getHeader();
        // 写入魔数
        out.writeShort(header.getMagic());
        // 写入版本号
        out.writeByte(header.getVersion());
        // 写入序列化类型
        out.writeByte(header.getSerializerType());
        // 写入消息类型
        out.writeByte(header.getMessageType());
        // 写入状态
        out.writeByte(header.getStatus());
        // 写入请求ID
        out.writeLong(header.getRequestId());
        // 写入消息体长度
        out.writeInt(header.getBodyLength());
        // 写入消息体
        if (msg.getBody() != null) {
            out.writeBytes((byte[]) msg.getBody());
        }
    }
}