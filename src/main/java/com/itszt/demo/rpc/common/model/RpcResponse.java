package com.itszt.demo.rpc.common.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * RPC响应实体类
 */
public class RpcResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 成功响应码
     */
    public static final int SUCCESS_CODE = 200;

    /**
     * 失败响应码
     */
    public static final int FAIL_CODE = 500;

    /**
     * 创建成功响应
     */
    public static <T> RpcResponse<T> success(T data, String requestId) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(SUCCESS_CODE);
        response.setMessage("Success");
        response.setRequestId(requestId);
        if (data != null) {
            response.setData(data);
        }
        return response;
    }
    
    /**
     * 创建成功响应（带服务名称）
     */
    public static <T> RpcResponse<T> success(T data, String requestId, String serviceName) {
        RpcResponse<T> response = success(data, requestId);
        response.setServiceName(serviceName);
        return response;
    }

    /**
     * 创建失败响应
     */
    public static <T> RpcResponse<T> fail(String message, String requestId) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(FAIL_CODE);
        response.setMessage(message);
        response.setRequestId(requestId);
        return response;
    }
    
    /**
     * 创建失败响应（带服务名称）
     */
    public static <T> RpcResponse<T> fail(String message, String requestId, String serviceName) {
        RpcResponse<T> response = fail(message, requestId);
        response.setServiceName(serviceName);
        return response;
    }

    public RpcResponse() {
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RpcResponse<?> that = (RpcResponse<?>) o;
        return Objects.equals(requestId, that.requestId) &&
                Objects.equals(code, that.code) &&
                Objects.equals(message, that.message) &&
                Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, code, message, data);
    }

    @Override
    public String toString() {
        return "RpcResponse{" +
                "requestId='" + requestId + '\'' +
                ", code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}