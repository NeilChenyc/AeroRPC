package com.itszt.demo.nio;

import java.io.IOException;
import java.util.Scanner;

import static com.itszt.demo.nio.Const.DEFAULT_SERVER_IP;

/**
 * nio通信客户端
 */
public class NioClient {

    private static NioClientHandle nioClientHandle;

    public static void start() {
        nioClientHandle = new NioClientHandle(DEFAULT_SERVER_IP, 8888);
        new Thread(nioClientHandle, "Server").start();
    }

    /***向服务器发送消息***/
    public static boolean sendMsg(String msg) throws IOException {
        nioClientHandle.sendMsg(msg);
        return true;

    }

    public static void main(String[] args) throws IOException {
        start();
        Scanner scanner = new Scanner(System.in);
        while (NioClient.sendMsg(scanner.next())) ;
    }
}
