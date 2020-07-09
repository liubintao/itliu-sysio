package org.itliu.sysio.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * @desc 非阻塞在单线程里的处理demo
 * @auther itliu
 * @date 2020/7/7
 */
public class SocketServerNIO {

    public static void main(String[] args) throws Exception {
        List<SocketChannel> clients = new ArrayList<>();

        ServerSocketChannel server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress(9090));
        server.configureBlocking(false); //重点  对应到kernel socket函数的SOCK_NONBLOCK

        for (; ; ) {
            Thread.sleep(1000); //每一秒循环一次，只是做演示用
            //因为NIO中不会阻塞，所以得放在loop里; 没有数据会返回null，对应内核中函数的-1，因为java是面向对象的
            SocketChannel client = server.accept();

            //accept  调用内核了：1，没有客户端连接进来，返回值？在BIO 的时候一直卡着，但是在NIO ，不卡着，返回-1，NULL
            //如果来客户端的连接，accept 返回的是这个客户端的fd  5，client  object
            //NONBLOCKING 就是代码能往下走了，只不过有不同的情况
            if (client == null) {
                System.out.println("null.................");
            } else {
                //重点  socket（服务端的listen socket<连接请求三次握手后，往我这里扔，我去通过accept 得到  连接的socket>，连接socket<连接后的数据读写使用的> ）
                client.configureBlocking(false);
                final int port = client.socket().getPort();
                System.out.println("client...port: " + port);
                clients.add(client);
            }

            ByteBuffer buffer = ByteBuffer.allocate(4096);//4k的大小，堆内分配
//            ByteBuffer buffer = ByteBuffer.allocateDirect(4096);//4k的大小，堆外分配，堆外是jvm堆外，java进程的堆内，减少了一次数据copy

            //遍历已经链接进来的客户端能不能读写数据
            for (SocketChannel channel : clients) { //串行化，要优化为多线程
                final int read = channel.read(buffer); // >0  -1  0   //不会阻塞
                if (read > 0) {
                    buffer.flip();
                    byte[] bytes = new byte[buffer.limit()];
                    buffer.get(bytes);

                    String res = new String(bytes);
                    System.out.println(client.socket().getPort() + " : " + res);
                    buffer.clear();
                }
            }

        }
    }
}
