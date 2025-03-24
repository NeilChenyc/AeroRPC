package com.itszt.demo.rpc.common.serializer;

/**
 * 序列化接口
 * 所有序列化类都需要实现这个接口
 */
public interface Serializer {
    
    /**
     * 序列化
     *
     * @param obj 要序列化的对象
     * @return 序列化后的字节数组
     */
    byte[] serialize(Object obj);

    /**
     * 反序列化
     *
     * @param bytes 序列化后的字节数组
     * @param clazz 目标类
     * @param <T>   类的类型
     * @return 反序列化后的对象
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);

    /**
     * 获取序列化器类型
     * @return 序列化器类型
     */
    SerializerType getType();
}