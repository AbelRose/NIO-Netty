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
        fc.read(buff); // 最终数据是咋缓冲区里面
        //5.打印到控制台上
        System.out.println(new String(buff.array()));
        //6.关闭流即可
        fis.close();
    }

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
