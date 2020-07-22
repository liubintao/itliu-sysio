package org.itliu.sysio.rpc.test.rpcenv.serverenv;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.itliu.sysio.rpc.test.api.Car;
import org.itliu.sysio.rpc.test.discover.MyDiscover;
import org.itliu.sysio.rpc.test.rpcenv.DecodeHandler;

import java.net.InetSocketAddress;

/**
 * @desc
 * @auther itliu
 * @date 2020/7/21
 */
public class ServerRPCEnv {
    public static void startServer() {
        MyDiscover.register(Car.class, new InetSocketAddress("localhost", 9090));

        NioEventLoopGroup bossGroup = new NioEventLoopGroup(10);
        NioEventLoopGroup workerGroup = bossGroup;
        ServerBootstrap bootstrap = new ServerBootstrap();
        ChannelFuture future = bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_RCVBUF, 1024 * 1024)
                .childOption(ChannelOption.SO_RCVBUF, 8192)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new DecodeHandler());
                        pipeline.addLast(new ServerRequestHandler());
                    }
                }).bind(new InetSocketAddress("localhost", 9090));

        try {
            future.sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
