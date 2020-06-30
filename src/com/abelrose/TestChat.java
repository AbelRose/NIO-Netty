package cn.itcast.nio.chat;

import java.util.Scanner;

//启动聊天程序客户端
public class TestChat {
    public static void main(String[] args) throws Exception {
        ChatClient chatClient = new ChatClient();

        // 接收数据
        new Thread(){  // 匿名内部类 创建一个单独的线程 用于接收官博过来的消息
            @Override
            public void run() {
                while (true){
                    try {
                        chatClient.receiveMsg();
                        Thread.sleep(2000);
                    } catch (Exception e) {
                        e.printStackTrace();
                     }
                }
            }
        }.start();

        // 给服务器端发送消息
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()){
            String msg = scanner.nextLine();
            chatClient.sendMsg(msg);
        }
    }
}
