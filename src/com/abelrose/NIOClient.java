package com.abelrose;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

// 网络客户端程序
public class NIOClient {
    public static void main(String[] args) throws Exception{
        //1.得到一个网络通道
        SocketChannel channel = SocketChannel.open();
        //2.设置非阻塞方式
        channel.configureBlocking(false); // 采用非阻塞的方式
        //3.提供服务器端的IP和端口号
        InetSocketAddress address = new InetSocketAddress("127.0.0.1", 9999);
        //4.连接服务器端
        if(!channel.connect(address)){
            while (!channel.finishConnect()){ // NIO作为非阻塞式的优势 -> 在连接的时候还可以做别的事情
                System.out.println("Client: 我还可以做别的事情...");
            }
        }
        //5.得到一个缓冲区
        String msg = "hello,Server";
        ByteBuffer wrap = ByteBuffer.wrap(msg.getBytes());// 字节数组
        //6.发送数据 到服务器端
        channel.write(wrap);

        // 注意暂时不能关闭通道 需要服务器的处理

        System.in.read();

    }
}
