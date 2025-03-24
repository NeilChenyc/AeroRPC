package com.itszt.demo.bio;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerPool {

    ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    /**
     * 服务端代码
     *
     * @param args
     */
    public static void main(String[] args) throws IOException {
        /**声明服务端socket对象**/
        ServerSocket serverSocket = new ServerSocket();
        /**服务端监听端口**/
        serverSocket.bind(new InetSocketAddress(10001));
        System.out.println("port 10001,服务已启动！！！");
        while (true){
            new Thread(new ServerTask(serverSocket.accept())).start();
        }

    }

    private static class ServerTask implements  Runnable{

        private Socket socket= null;


        public ServerTask(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (ObjectInputStream inputStream= new ObjectInputStream(socket.getInputStream());
                    ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream())) {
                String intput = inputStream.readUTF();
                System.out.println("my name is"+intput);
                outputStream.writeUTF("去吃屎吧,"+intput);
                outputStream.flush();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}