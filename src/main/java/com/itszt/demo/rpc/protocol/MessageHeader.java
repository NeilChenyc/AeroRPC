package com.itszt.demo.rpc.protocol;

import com.itszt.demo.rpc.common.serializer.SerializerType;

import java.io.Serializable;

/**
 * 消息头
 */
public class MessageHeader implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 魔数，用于快速判断是否是有效数据包
     */
    private short magic;

    /**
     * 版本号
     */
    private byte version;

    /**
     * 序列化类型
     */
    private byte serializerType;

    /**
     * 消息类型
     */
    private byte messageType;

    /**
     * 状态
     */
    private byte status;

    /**
     * 请求ID
     */
    private long requestId;

    /**
     * 消息体长度
     */
    private int bodyLength;

    /**
     * 默认魔数
     */
    public static final short DEFAULT_MAGIC = 0x10;

    /**
     * 默认版本号
     */
    public static final byte DEFAULT_VERSION = 0x1;

    public MessageHeader() {
        this.magic = DEFAULT_MAGIC;
        this.version = DEFAULT_VERSION;
    }

    public short getMagic() {
        return magic;
    }

    public void setMagic(short magic) {
        this.magic = magic;
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public byte getSerializerType() {
        return serializerType;
    }

    public void setSerializerType(byte serializerType) {
        this.serializerType = serializerType;
    }

    public void setSerializerType(SerializerType serializerType) {
        this.serializerType = (byte) serializerType.getCode();
    }

    public byte getMessageType() {
        return messageType;
    }

    public void setMessageType(byte messageType) {
        this.messageType = messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = (byte) messageType.getCode();
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public int getBodyLength() {
        return bodyLength;
    }

    public void setBodyLength(int bodyLength) {
        this.bodyLength = bodyLength;
    }
}