package com.itszt.demo.nio;

import static com.itszt.demo.nio.Const.DEFAULT_PORT;

/***
 * nio通信服务端
 */
public class NioServer {

private static NioServerHandle nioServerHandle;

    public static void main(String[] args) {
        nioServerHandle = new NioServerHandle(DEFAULT_PORT);
        new Thread(nioServerHandle,"Server").start();
    }

}
