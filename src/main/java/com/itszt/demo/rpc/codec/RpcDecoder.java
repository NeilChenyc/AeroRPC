package com.itszt.demo.rpc.codec;

import com.itszt.demo.rpc.protocol.MessageHeader;
import com.itszt.demo.rpc.protocol.RpcMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * RPC解码器
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
public class RpcDecoder extends ByteToMessageDecoder {

    /**
     * 消息头长度
     */
    private static final int HEADER_LENGTH = 2 + 1 + 1 + 1 + 1 + 8 + 4;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 如果可读字节数小于消息头长度，则不处理
        if (in.readableBytes() < HEADER_LENGTH) {
            return;
        }

        // 标记当前读取位置
        in.markReaderIndex();

        // 读取魔数
        short magic = in.readShort();
        // 读取版本号
        byte version = in.readByte();
        // 读取序列化类型
        byte serializerType = in.readByte();
        // 读取消息类型
        byte messageType = in.readByte();
        // 读取状态
        byte status = in.readByte();
        // 读取请求ID
        long requestId = in.readLong();
        // 读取消息体长度
        int bodyLength = in.readInt();

        // 如果可读字节数小于消息体长度，则重置读取位置，等待更多数据
        if (in.readableBytes() < bodyLength) {
            in.resetReaderIndex();
            return;
        }

        // 读取消息体
        byte[] body = new byte[bodyLength];
        in.readBytes(body);

        // 创建消息头
        MessageHeader header = new MessageHeader();
        header.setMagic(magic);
        header.setVersion(version);
        header.setSerializerType(serializerType);
        header.setMessageType(messageType);
        header.setStatus(status);
        header.setRequestId(requestId);
        header.setBodyLength(bodyLength);

        // 创建消息
        RpcMessage message = new RpcMessage();
        message.setHeader(header);
        message.setBody(body);

        // 添加到输出列表
        out.add(message);
    }
}