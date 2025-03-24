package com.itszt.demo.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * nio通信服务端处理器
 */
public class NioServerHandle implements Runnable {


    private volatile boolean started;

    /***服务端需要选择器用来注册事件**/
    private Selector selector;

    /***ServerSocketChannel:应用服务器程序的监听通道,只有通过这个通道
     * ,应用程序才能向操作系统注册支持"多路复用IO"的端口监听,同时支持UDP协议
     和TCP协议**/
    private ServerSocketChannel serverSocketChannel;

    /**
     * 构造方法
     *
     * @param port 指定要监听的端口号
     */
    public NioServerHandle(int port) {
        try {
            /***创建选择器的实例***/
            selector = Selector.open();
            /**创建ServerSocketChannel的实例***/
            serverSocketChannel = ServerSocketChannel.open();

            /***设置通道为非阻塞模式**/
            serverSocketChannel.configureBlocking(false);

            /**绑定端口***/
            serverSocketChannel.socket().bind(new InetSocketAddress(port));

            /***注册事件,表示关心客户端连接**/
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            started = true;
            System.out.println("服务器已启动,端口号: " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /***
     * 构造方法
     */


    @Override
    public void run() {
        while (started) {
            try {
                /***获取当前有哪些事件****/
                selector.select(1000);
                /**获取事件的集合***/
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    /**我们必须首先将处理过的SelectionKey 从选定的键集合中删除
                     如果我们没有删除处理过的键,那么它仍然会在主集合中以以一个
                     激活的键出现,这会导致我们尝试再次处理它**/
                    iterator.remove();
                    handleInput(key);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /***处理事件的发生**/
    private void handleInput(SelectionKey key) throws IOException {
        if (key.isValid()) {
            if (key.isAcceptable()) {
                ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                SocketChannel accept = channel.accept();
                System.out.println("===============建立连接=================");
                accept.configureBlocking(false);
                /***关注读事件**/
                accept.register(selector, SelectionKey.OP_READ);
            }
            if (key.isReadable()) {
                SocketChannel channel = (SocketChannel) key.channel();
                ByteBuffer buffer = ByteBuffer.allocate(1000);
                int read = channel.read(buffer);
                if (read > 0) {
                    /***
                     * 将缓冲区当前limit设置为postion,postion为0
                     * 用于后续对缓冲区的读取操作
                     */
                    buffer.flip();
                    /***根据缓冲区可读字节数创建字节数据****/
                    byte[] bytes = new byte[buffer.remaining()];
                    /**将缓冲区可读字节数组复制到新建的数组中***/
                    buffer.get(bytes);
                    String message = new String(bytes, "UTF-8");
                    System.out.println("服务器收到消息: " + message);
                    /***处理数据**/
                    String result = Const.response(message);
                    /**发送应答消息**/
                    doWrite(channel, result);
                } else if (read < 0) {
                    /***取消特定的注册关系***/
                    key.cancel();
                    /**关闭通道***/
                    channel.close();
                }

            }
        }
    }

    /***发送应答消息****/
    private void doWrite(SocketChannel channel, String result) throws IOException {
        byte[] bytes = result.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        channel.write(buffer);
    }

    public void stop() {
        started = false;
    }
}
