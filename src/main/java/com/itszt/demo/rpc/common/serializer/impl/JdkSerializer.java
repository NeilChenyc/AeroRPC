package com.itszt.demo.rpc.common.serializer.impl;

import com.itszt.demo.rpc.common.serializer.Serializer;
import com.itszt.demo.rpc.common.serializer.SerializerType;

import java.io.*;

/**
 * JDK原生序列化实现
 */
public class JdkSerializer implements Serializer {

    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("JDK序列化失败", e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return clazz.cast(ois.readObject());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("JDK反序列化失败", e);
        }
    }

    @Override
    public SerializerType getType() {
        return SerializerType.JDK;
    }
}