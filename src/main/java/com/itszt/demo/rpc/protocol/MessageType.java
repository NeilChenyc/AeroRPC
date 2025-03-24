package com.itszt.demo.rpc.protocol;

/**
 * 消息类型枚举
 */
public enum MessageType {

    /**
     * 请求消息
     */
    REQUEST(1),

    /**
     * 响应消息
     */
    RESPONSE(2),

    /**
     * 心跳请求
     */
    HEARTBEAT_REQUEST(3),

    /**
     * 心跳响应
     */
    HEARTBEAT_RESPONSE(4);

    private final int code;

    MessageType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static MessageType valueOf(int code) {
        for (MessageType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown message type code: " + code);
    }
}