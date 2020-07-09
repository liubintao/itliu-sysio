package org.itliu.sysio.c10k;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * @desc 在不同的机器上启动此client，连接相同的server
 *       一个client会持有55000个socket四元组，server端四元组的数量=client数量*55000
 *       过程中会涉及调整server的内核参数
 * @auther itliu
 * @date 2020/7/7
 */
public class C10KClient {

    public static final InetSocketAddress REMOTE = new InetSocketAddress("10.0.0.5", 9090);

    public static void main(String[] args) {
        List<SocketChannel> objects = new ArrayList<>();

        for (int i = 10000; i < 65000; i++) {
            try {
                SocketChannel client1 = SocketChannel.open();

                client1.bind(new InetSocketAddress("10.0.0.1", i));
                client1.connect(REMOTE);
                boolean c1 = client1.isOpen();
                objects.add(client1);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        System.out.println("clients " + objects.size());

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
