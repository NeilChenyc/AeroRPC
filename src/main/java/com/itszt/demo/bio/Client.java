package com.itszt.demo.bio;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * bio通信客户端
 */
public class Client {

    public static void main(String[] args) throws IOException {
        /**客户端启动必备**/
        Socket socket = null;
        /**实例化与服务端通信的输入输出流**/
        ObjectOutputStream output = null;
        ObjectInputStream input = null;
        //服务器的通信地址
        InetSocketAddress address = new InetSocketAddress("127.0.0.1", 10001);
        try {
            socket= new Socket();
            socket.connect(address);//连接服务器
            output=new ObjectOutputStream(socket.getOutputStream());
            input=new ObjectInputStream(socket.getInputStream());
            /**向服务器输出请求**/
output.writeUTF("lijinjian");
output.flush();
/**接收服务器的输出**/
            System.out.println(input.readUTF());
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (socket!=null)
                socket.close();
            if (output!=null)
                output.close();
            if (input!=null)
                input.close();
        }
    }
}
