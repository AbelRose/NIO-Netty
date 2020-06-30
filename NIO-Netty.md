# NIO-Netty

### BIO 编程

Block-IO

文件IO或者网络IO

![BIO](NIO-Netty.assets/BIO.png)

##### BIO-Client (基于TCP)

```java
package com.abelrose;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

//BIO 客户端程序
public class TCPClient {
	public static void main(String[] args) throws Exception {
		while (true) {
			//1.创建Socket对象
			Socket s = new Socket("127.0.0.1", 9999);
			//2.从连接中取出输出流并发消息
			OutputStream os = s.getOutputStream();
			System.out.println("请输入:");
			Scanner sc = new Scanner(System.in);
			String msg = sc.nextLine();
			os.write(msg.getBytes());
			//3.从连接中取出输入流并接收回话
			InputStream is = s.getInputStream(); //阻塞 等待服务器返回数据 如果没有返回就一直等待
			byte[] b = new byte[20];
			is.read(b);
			System.out.println("老板说:" + new String(b).trim());
			//4.关闭
			s.close();
		}
	}
}

```

##### BIO-Server

```java
package com.abelrose;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

//BIO 服务器端程序
public class TCPServer {
	public static void main(String[] args) throws Exception {
		//1.创建ServerSocket对象
		ServerSocket ss=new ServerSocket(9999); //端口号

		while (true) {
			//2.监听客户端
			System.out.println("来呀");
			Socket s = ss.accept(); //阻塞 正在监听客户端连接 如果没有客户端连接就一直等待
			System.out.println("来呀");
			//3.从连接中取出输入流来接收消息
			InputStream is = s.getInputStream(); //阻塞
			byte[] b = new byte[10];
			is.read(b);
			String clientIP = s.getInetAddress().getHostAddress();
			System.out.println(clientIP + "说:" + new String(b).trim());
			//4.从连接中取出输出流并回话
			//OutputStream os = s.getOutputStream();
			//os.write("没钱".getBytes());
			//5.关闭
			//s.close();
		}
	}
}


```

### NIO 编程

Non-blocking IO (After JDK1.4)

BIO是以流的方式处理数据 NIO是以块的方式(Channel Buffer)处理数据(效率高很多)

三大核心*:**Channel Buffer Selector***

##### 文件IO



Buffer 实际上是一个容器(特殊的数组)  能够***记录*** 和***跟踪* **缓冲区的状态变化情况,读写必须经过Buffer

![image-20200630160949057](NIO-Netty.assets/image-20200630160949057.png)

在NIO中,Buffer是一个顶层父类,他是一个抽象类,常用子类有:

- ByteBuffer:存储字节数据到缓存(最常用)

  ​	主要方法:

   - public abstract ByteBuffer put(byte[] b); 存储字节数据到缓冲区
   - public abstract byte[] get(); 从缓冲区获得字节数据
   - public final byte[] array(); 把缓冲区数据转换成字节数组
   - public static ByteBuffer allocate(int capacity); 设置缓冲区的初始容量
   - public static ByteBuffer wrap(byte[] array); 把一个现成的数组放到缓冲区中使用
   - public final Buffer flip(); 翻转缓冲区,重置位置到初始位置

- ShortBuffer:存储字符串数据到缓存

- CharBuffer:存储字符数据到缓冲区
- IntBuffer:存储整数数据到缓冲区
- LongBuffer:存储长整型数据到缓冲区
- DoubleBuffer:存储小数到缓冲区
- FloatBuffer:存储小数到缓冲区



Channel 类似于 BIO 中的Stream(单向的input/output) NIO中的Channel是双向的(可读可写)

- FileChannel:文件读写
  - public int read(ByteBuffer dst) ,从通道读取数据并放到缓冲区中
  - public int write(ByteBuffer src) ,把缓冲区的数据写到通道中
  - public long transferFrom(ReadableByteChannel src, long position, long count),从目标通道中复制数据到当前通道
  - public long transferTo(long position, long count,WritableByteChannel target),把数据从当前通道复制给目标通道
- DatagramChannel:UDP读写
- ServerSocketChannel:TCP读写
- SocketChannel:TCP读写

![image-20200630163612171](NIO-Netty.assets/image-20200630163612171.png)

文件操作

```java
package com.abelrose;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

// 通过NIO实现文件IO
public class TestNIO {
    @Test  // 在本地文件中写数据
    public void test1() throws Exception{
        //1.创建输出流
        FileOutputStream fos = new FileOutputStream("demo.txt"); // 往这个文件中写数据、
        //2.从流中得到通道(通道是从流中创建得到的)
        FileChannel fc = fos.getChannel();
        //3.提供一个缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024); // 注意这个不是new出来的 是静态方法
        //4.往缓冲区中存数据
        String str = "hello,NIO";
        buffer.put(str.getBytes()); // str.getBytes() 把一个字符串转换成字节数组
        //5.翻转缓冲区
        buffer.flip(); // 要不然不从头开始读字符串 从最后开始读 是空的
        //6.把缓冲区写到通道中
        fc.write(buffer);
        //7.关闭
        fos.close(); // 关闭流的时候就已经把通道关闭了
    }

    @Test // 从本地文件中读取数据
    public void test2() throws Exception{
        //0.将文件封装 为了在缓冲区中能够调用文件的大小
        File file = new File("demo.txt");
        //1.创建输入流
        FileInputStream fis = new FileInputStream(file);
        //2.得到一个通道
        FileChannel fc = fis.getChannel();
        //3.提供一个缓冲区
        ByteBuffer buff = ByteBuffer.allocate((int) file.length()); // 以文件的大小作为缓冲区的大小
        //4.从通道里读取数据 并存到缓冲区中
        fc.read(buff); // 最终数据是在缓冲区里面
        //5.打印到控制台上
        System.out.println(new String(buff.array()));
        //6.关闭流即可
        fis.close();
    }

    /** 注意FileChannel fc = fos.getChannel();
    *   fc.write() 和 fc.read() 的区别
    */
    
    @Test // 使用NIO实现文件复制
    public void test3() throws Exception{
        //1.创建两个流
        FileInputStream fis = new FileInputStream("demo.txt");
        FileOutputStream fos = new FileOutputStream("demo-copy.txt");
        //2.得到两个通道
        FileChannel sour = fis.getChannel();
        FileChannel dest = fos.getChannel();
        //3.复制
        dest.transferFrom(sour,0,sour.size());  // 适合复制大文件
//        sour.transferTo(dest,0,sour.size());
        //4.关闭
        fis.close();
        fos.close();
    }
}
```



##### 网络IO

学习 NIO 主要就是进行网络 IO,Java NIO 中的网络通道是非阻塞 IO 的实现

基于事件驱动,非常适用于服务器需要***维持大量连接***,但是数据交换量不大的情况,例如一些***即时通信的服务***

Selector(选择器)-俗称通道管理大师,能够检测多个注册的通道上是否有事件发生,如果有事件发生,便获取事件然后针对每个事件进行相应的处理。

![image-20200630165750718](NIO-Netty.assets/image-20200630165750718.png)

优点:

- 大大地减少了系统开销,并且不必为每个连接都创建一个线程,不用去维护多个线程,
- 避免了多线程之间的上下文切换导致的开销

常用方法:

- public static Selector ***open()***,得到一个选择器对象
- public int select(***long timeout***),监控所有注册的通道,当其中有 IO 操作可以进行时,将对应的 SelectionKey 加入到内部集合中并返回,参数用来设置超时时间
- public Set<SelectionKey> ***selectedKeys()***,从内部集合中得到所有的SelectionKey 代表了Selector和通道之间的注册关系
  - int OP_ACCEPT有新的网络连接可以 accept,值为 16
  - int OP_CONNECT:代表连接已经建立,值为 8
  - int OP_READ 和 int OP_WRITE:代表了读、写操作,值为 1 和 4



ServerSocketChannel

在服务器端监听新的客户端 Socket 连接

- public static ServerSocketChannel ***open()***,得到一个 ServerSocketChannel 通道
- public final ServerSocketChannel ***bind***(SocketAddress local),设置服务器端端口号
- public final SelectableChannel ***configureBlocking***(boolean block),设置阻塞或非阻塞模式,取值 false 表示采用非阻塞模式
- public SocketChannel ***accept()***,接受一个连接,返回代表这个连接的通道对象
- public final SelectionKey ***register***(Selector sel, int ops),注册一个选择器并设置监听事件

SocketChannel

网络 IO 通道,具体负责进行读写操作(缓冲区-通道或者通道-缓冲区)

- public static SocketChannel ***open()***,得到一个 SocketChannel 通道

- public final SelectableChannel ***configureBlocking***(boolean block),设置阻塞或非阻塞模式,取值 false 表示采用非阻塞模式

- public boolean ***connect***(SocketAddress remote),连接服务器

- public boolean ***finishConnect()***,如果上面的方法***连接失败***,接下来就要通过该方法完成连接操作

- public int ***write***(ByteBuffer src),往通道里写数据

- public int ***read***(ByteBuffer dst),从通道里读数据

- public final SelectionKey ***register***(Selector sel, int ops, Object att),注册一个选择器并设置监听事件,最后一个参数可以设置共享数据

- public final void close(),关闭通道

  ![image-20200630171226660](NIO-Netty.assets/image-20200630171226660.png)

  

![image-20200630171312459](NIO-Netty.assets/image-20200630171312459.png)



##### NIO-Clinet

```java
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
```



##### NIO-Server

```java
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
                }
                if(key.isReadable()){ // 读取客户端事件 能不能读
                }
            }
        }
    }
}
```





















































































































