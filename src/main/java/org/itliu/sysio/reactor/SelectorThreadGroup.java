package org.itliu.sysio.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @desc
 * @auther itliu
 * @date 2020/7/8
 */
public class SelectorThreadGroup {

    SelectorThread[] sts;
    ServerSocketChannel server;

    AtomicInteger xid = new AtomicInteger(0);

    SelectorThreadGroup stg = this;

    public SelectorThreadGroup(int num) {
        sts = new SelectorThread[num];
        for (int i = 0; i < num; i++) {
            sts[i] = new SelectorThread(this);
            new Thread(sts[i]).start();
        }
    }

    public void setWorker(SelectorThreadGroup worker) {
        this.stg = worker;
    }

    public void bind(int port) {
        try {
            server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.bind(new InetSocketAddress(port));

            nextSelectorV2(server);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void nextSelectorV2(Channel channel) {
        if (channel instanceof ServerSocketChannel) {
            sts[0].lbq.add(channel);
            sts[0].selector.wakeup();
        } else {
            final SelectorThread st = nextV2(channel);

            st.lbq.add(channel);
            st.selector.wakeup();//唤醒对应的thread，由对应的thread处理注册
        }

    }

    public void nextSelectorV3(Channel channel) {
        if (channel instanceof ServerSocketChannel) {
            final SelectorThread st = next(channel);
            st.lbq.add(channel);
            st.selector.wakeup();
        } else {
            final SelectorThread st = nextV3(channel);
            st.lbq.add(channel);
            st.selector.wakeup();//唤醒对应的thread，由对应的thread处理注册
        }

    }

    public void nextSelector(Channel channel) {
        final SelectorThread st = next(channel);

        st.lbq.add(channel);
        st.selector.wakeup();//唤醒对应的thread，由对应的thread处理注册

        /*final ServerSocketChannel server = (ServerSocketChannel) channel;

        try {
            server.register(st.selector, SelectionKey.OP_ACCEPT);
            st.selector.wakeup();

        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }*/
    }


    private SelectorThread next(Channel channel) {
        final int index = xid.incrementAndGet() % sts.length; //轮询就会很尴尬，倾斜
        return sts[index];
    }


    private SelectorThread nextV2(Channel channel) {
        final int index = xid.incrementAndGet() % (sts.length - 1); //轮询就会很尴尬，倾斜
        return sts[index + 1];
    }

    private SelectorThread nextV3(Channel channel) {
        final int index = xid.incrementAndGet() % stg.sts.length; //动用worker的线程分配
        return stg.sts[index];
    }
}
