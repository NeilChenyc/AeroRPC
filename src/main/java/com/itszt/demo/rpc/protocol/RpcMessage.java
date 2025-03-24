package com.itszt.demo.rpc.protocol;

import java.io.Serializable;

/**
 * RPC消息，包含消息头和消息体
 */
public class RpcMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息头
     */
    private MessageHeader header;

    /**
     * 消息体
     */
    private Object body;

    public RpcMessage() {
    }

    public MessageHeader getHeader() {
        return header;
    }

    public void setHeader(MessageHeader header) {
        this.header = header;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }
}