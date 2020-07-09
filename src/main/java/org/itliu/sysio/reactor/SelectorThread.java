package org.itliu.sysio.reactor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @desc
 * @auther itliu
 * @date 2020/7/8
 */
public class SelectorThread extends Thread {

    Selector selector;
    LinkedBlockingQueue<Channel> lbq = new LinkedBlockingQueue<>();
    SelectorThreadGroup stg;

    public SelectorThread(SelectorThreadGroup stg) {
        try {
            this.stg = stg;
            this.selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        for (; ; ) {
            try {
                //1.select
                System.out.println(Thread.currentThread().getName() + "   :  before select...." + selector.keys().size());
                final int nums = selector.select();
//                Thread.sleep(1000);
                System.out.println(Thread.currentThread().getName() + "   :  after select...." + selector.keys().size());
                //2.处理selectionKeys
                if (nums > 0) {
                    final Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    final Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    while (iterator.hasNext()) {
                        final SelectionKey key = iterator.next();
                        iterator.remove();

                        if (key.isAcceptable()) {
                            acceptHandler(key);
                        } else if (key.isReadable()) {
                            readHandler(key);
                        } else if (key.isWritable()) {

                        }
                    }
                }
                //3.处理一些task
                if (!lbq.isEmpty()) {
                    final Channel channel = lbq.take();
                    if (channel instanceof ServerSocketChannel) {
                        final ServerSocketChannel server = (ServerSocketChannel) channel;
                        server.register(selector, SelectionKey.OP_ACCEPT);
                    } else if (channel instanceof SocketChannel) {
                        final SocketChannel client = (SocketChannel) channel;
                        ByteBuffer buffer = ByteBuffer.allocate(4096);
                        client.register(selector, SelectionKey.OP_READ, buffer);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void readHandler(SelectionKey key) {
        final SocketChannel client = (SocketChannel) key.channel();
        final ByteBuffer buffer = (ByteBuffer) key.attachment();
        buffer.clear();
        while (true) {
            try {
                final int read = client.read(buffer);
                if (read > 0) {
                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        client.write(buffer);
                    }
                    buffer.clear();
                }
                if (read == 0) {
                    break;
                } else if (read < 0) {
                    //client disconnected
                    System.out.println("client " + client.getRemoteAddress() + " closed...");
                    key.cancel();
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void acceptHandler(SelectionKey key) {
        System.out.println("acceptHandler..........");
        final ServerSocketChannel server = (ServerSocketChannel) key.channel();
        try {
            final SocketChannel client = server.accept();
            client.configureBlocking(false);

            //选择一个selector去注册
            stg.nextSelectorV3(client);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
