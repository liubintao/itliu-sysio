package org.itliu.sysio.rpc.test.rpcenv.connectpool;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.itliu.sysio.rpc.test.discover.MyDiscover;
import org.itliu.sysio.rpc.test.packmode.MsgCallbackMapping;
import org.itliu.sysio.rpc.test.rpcenv.DecodeHandler;
import org.itliu.sysio.rpc.test.rpcenv.PackageMsg;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @desc 获取连接的工厂
 * @auther itliu
 * @date 2020/7/21
 */
public class ClientFactory {
    int poolSize = 100;
    private static final ClientFactory factory;
    NioEventLoopGroup clientWorker;
    private static final Map<InetSocketAddress, ClientPool> outBox = new ConcurrentHashMap<>();
    private static final Random random = new Random();

    static {
        factory = new ClientFactory();
    }

    private ClientFactory() {
    }

    //单例获取的方法
    public static ClientFactory getInstance() {
        return factory;
    }

    /**
     * 客户端应用的功能：发送信息给服务端
     * 1.连接到服务端
     * 2.发送信息
     *
     * @param packageMsg     发送的信息
     * @param clazzInterface 发送到服务端后，由哪个接口来处理(实际处理的是实现类)
     * @return
     */
    public static CompletableFuture send(PackageMsg packageMsg, Class<?> clazzInterface) {
        ClientFactory factory = ClientFactory.getInstance();
        NioSocketChannel clientChannel = factory.getClient(MyDiscover.discover(clazzInterface));
        CompletableFuture cf = new CompletableFuture();
        MsgCallbackMapping.addCallback(packageMsg.getMessageId(), cf);
        clientChannel.writeAndFlush(packageMsg.getByteBuf());
        return cf;
    }

    private NioSocketChannel getClient(InetSocketAddress address) {
        ClientPool clientPool = outBox.get(address);
        if (clientPool == null) {
            outBox.putIfAbsent(address, new ClientPool(poolSize));
            clientPool = outBox.get(address);
        }

        int i = random.nextInt(poolSize);

        if (clientPool.clients[i] != null && clientPool.clients[i].isActive()) {
            return clientPool.clients[i];
        }

        synchronized (clientPool.locks[i]) {
            return clientPool.clients[i] = create(address);
        }
    }

    private NioSocketChannel create(InetSocketAddress address) {
        clientWorker = new NioEventLoopGroup(1);
        Bootstrap bootstrap = new Bootstrap();
        ChannelFuture connect = bootstrap.group(clientWorker)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new DecodeHandler());
                        pipeline.addLast(new ClientResponsesHandler());
                    }
                }).connect(address);
        try {
            return (NioSocketChannel) connect.sync().channel();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
