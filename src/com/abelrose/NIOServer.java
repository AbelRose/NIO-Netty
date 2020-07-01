package com.abelrose;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

// 服务器端程序
public class NIOServer {
    public static void main(String[] args) throws Exception{
        //1.得到一个ServerSockectChannel对象 服务器端老大
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //2.得到一个Selector对象 间谍
        Selector selector = Selector.open();
        //3.绑定一个端口号
        serverSocketChannel.bind(new InetSocketAddress(9999));
        //4.设置非阻塞方式
        serverSocketChannel.configureBlocking(false);
        //5.把得到一个ServerSocketChannel对象 注册给 selector对象
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        //6. 淦 !!!
        while (true){
            //6.1 监控客户端
            if(selector.select(2000) == 0){ // 监控是否有客户端  NIO非阻塞式的优势
                System.out.println("Server: 如果没有客户端我就可以做些别的事情");
                continue;
            }

            //6.2 得到Selectionkey 判断通道的事件
            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
            while (keyIterator.hasNext()){
                SelectionKey key = keyIterator.next();
                if(key.isAcceptable()){ // 客户端连接事件 能不能连
                    System.out.println("OP_ACCEPT");
                    SocketChannel socketChannel=serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector,SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                }
                if(key.isReadable()){ // 读取客户端事件 能不能读
                    SocketChannel channel=(SocketChannel) key.channel();
                    ByteBuffer buffer=(ByteBuffer) key.attachment();
                    channel.read(buffer);
                    System.out.println("客户端发来数据:"+new String(buffer.array()));
                }
            }


        }
    }
}
