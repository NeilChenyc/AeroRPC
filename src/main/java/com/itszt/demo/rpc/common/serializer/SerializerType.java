package com.itszt.demo.rpc.common.serializer;

/**
 * 序列化类型枚举
 */
public enum SerializerType {
    
    /**
     * JDK原生序列化
     */
    JDK(1),
    
    /**
     * JSON序列化
     */
    JSON(2),
    
    /**
     * Protobuf序列化
     */
    PROTOBUF(3),
    
    /**
     * Hessian序列化
     */
    HESSIAN(4);
    
    private final int code;
    
    SerializerType(int code) {
        this.code = code;
    }
    
    public int getCode() {
        return code;
    }
    
    public static SerializerType valueOf(int code) {
        for (SerializerType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown serializer type code: " + code);
    }
}