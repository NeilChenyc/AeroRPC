package com.itszt.demo.rpc.common.serializer;

import com.itszt.demo.rpc.common.serializer.impl.JdkSerializer;
import com.itszt.demo.rpc.common.serializer.impl.JsonSerializer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 序列化工厂，用于获取序列化器
 */
public class SerializerFactory {

    private static final Map<SerializerType, Serializer> SERIALIZER_MAP = new ConcurrentHashMap<>();

    static {
        // 注册序列化器
        SERIALIZER_MAP.put(SerializerType.JDK, new JdkSerializer());
        SERIALIZER_MAP.put(SerializerType.JSON, new JsonSerializer());
        // 可以在这里注册更多的序列化器
    }

    /**
     * 获取序列化器
     *
     * @param type 序列化类型
     * @return 序列化器
     */
    public static Serializer getSerializer(SerializerType type) {
        Serializer serializer = SERIALIZER_MAP.get(type);
        if (serializer == null) {
            throw new IllegalArgumentException("Serializer not found for type: " + type);
        }
        return serializer;
    }

    /**
     * 注册序列化器
     *
     * @param type 序列化类型
     * @param serializer 序列化器
     */
    public static void registerSerializer(SerializerType type, Serializer serializer) {
        SERIALIZER_MAP.put(type, serializer);
    }
}