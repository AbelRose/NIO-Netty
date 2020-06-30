package cn.itcast.nio.chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.text.SimpleDateFormat;
import java.util.*;

//聊天程序服务器端
public class ChatServer {
    private ServerSocketChannel listenerChannel; //监听通道  老大
    private Selector selector;//选择器对象  间谍
    private static final int PORT = 9999; //服务器端口

    public ChatServer() {
        // 最基本的额五个步骤
        try {
            // 1. 得到监听通道  老大
            listenerChannel = ServerSocketChannel.open();
            // 2. 得到选择器  间谍
            selector = Selector.open();
            // 3. 绑定端口
            listenerChannel.bind(new InetSocketAddress(PORT));
            // 4. 设置为非阻塞模式
            listenerChannel.configureBlocking(false);
            // 5. 将选择器绑定到监听通道并监听accept事件
            listenerChannel.register(selector, SelectionKey.OP_ACCEPT);
            printInfo("Chat Server is ready.......");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //6. 干活儿 淦 ! ! !
    public void start() throws Exception {
        try {
            while (true) { //不停监控
                if (selector.select(2000) == 0) {
                    System.out.println("Server:没有客户端找我， 我就干别的事情");
                    continue;
                }
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isAcceptable()) { //连接请求事件
                        SocketChannel sc = listenerChannel.accept();
                        sc.configureBlocking(false);
                        sc.register(selector, SelectionKey.OP_READ);
                        System.out.println(sc.getRemoteAddress().toString().substring(1) + "上线了...");
                    }
                    if (key.isReadable()) { //读取数据事件
                        readMsg(key); // 从指定的key中读取数据
                    }
                    //一定要把当前key删掉，防止重复处理
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //读取客户端发来的消息并广播出去
    public void readMsg(SelectionKey key) throws Exception {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int count = channel.read(buffer);
        if (count > 0) {
            String msg = new String(buffer.array());
            printInfo(msg);
            // 发广播 需要两个参数 第一个是当前的通道(需要排除掉) 另一个是广播的信息
            broadCast(channel, msg);
        }
    }

    //给所有的客户端发广播(除了自己) 说实话就i是向通道中写数据
    public void broadCast(SocketChannel except, String msg) throws Exception {
        System.out.println("服务器发送了广播...");
        // 目前有多少个通道 通过selector
        for (SelectionKey key : selector.keys()) { // 返回所有就绪的通道 就是已经连上的通道
            Channel targetChannel =  key.channel(); // 找到所有已经就绪的通道 注意这个地方不能直接强转(有可能是各种各样的通道) 需要用父类接收一下
            // 排除自身
            if(targetChannel!=except && targetChannel instanceof SocketChannel){
                // 发广播 就是写数据
                SocketChannel destChannel = (SocketChannel) targetChannel; // 如果是SocketChannel 那么进行强转;
                ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes()); // 传到缓冲区
                destChannel.write(buffer);
            }
        }
    }

    private void printInfo(String str) { //往控制台打印消息
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("[" + sdf.format(new Date()) + "] -> " + str);
    }

    public static void main(String[] args) throws Exception {
        new ChatServer().start();
    }
}
