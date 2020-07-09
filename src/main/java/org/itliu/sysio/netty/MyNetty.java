package org.itliu.sysio.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

/**
 * @desc
 * @auther itliu
 * @date 2020/7/8
 */
public class MyNetty {

    @Test
    public void myByteBuffer() {
//        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(8, 20);
//        ByteBuf buf = UnpooledByteBufAllocator.DEFAULT.heapBuffer(8, 20);
        ByteBuf buf = PooledByteBufAllocator.DEFAULT.heapBuffer(8, 20);
        print(buf);
        buf.writeBytes(new byte[]{1, 2, 3, 4});
        print(buf);
        buf.writeBytes(new byte[]{1, 2, 3, 4});
        print(buf);
        buf.writeBytes(new byte[]{1, 2, 3, 4});
        print(buf);
        buf.writeBytes(new byte[]{1, 2, 3, 4});
        print(buf);
        buf.writeBytes(new byte[]{1, 2, 3, 4});
        print(buf);
        /*buf.writeBytes(new byte[]{1,2,3,4});
        print(buf);*/
    }

    @Test
    public void loopExecutor() throws IOException {
        NioEventLoopGroup selector = new NioEventLoopGroup(1);
        selector.execute(() -> {
            try {
                for (; ; ) {
                    System.out.println("hello world001");
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        selector.execute(() -> {
            try {
                for (; ; ) {
                    System.out.println("hello world002");
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });
        System.in.read();
    }

    @Test
    public void clientMode() throws Exception {
        NioEventLoopGroup thread = new NioEventLoopGroup(1);
        NioSocketChannel client = new NioSocketChannel();

        thread.register(client);  //epoll_ctl(5,ADD,3)
        final ChannelPipeline pipeline = client.pipeline();
        pipeline.addLast(new MyInHandler());

        final ChannelFuture connect = client.connect(new InetSocketAddress("10.0.0.5", 9090));
        final ChannelFuture sync = connect.sync();

        ByteBuf buf = Unpooled.copiedBuffer("hello server".getBytes());
        final ChannelFuture channelFuture = client.writeAndFlush(buf);
        channelFuture.sync();

        sync.channel().closeFuture().sync();
        System.out.println("client closed...");
    }

    @Test
    public void nettyClient() throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        Bootstrap bootstrap = new Bootstrap();
        final ChannelFuture connect = bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new MyInHandler())
                .connect(new InetSocketAddress("127.0.0.1", 9090));
        final Channel client = connect.sync().channel();

        ByteBuf buf = Unpooled.copiedBuffer("hello server".getBytes());
        final ChannelFuture channelFuture = client.writeAndFlush(buf);
        channelFuture.sync();

        connect.channel().closeFuture().sync();
        System.out.println("client closed...");
    }

    @Test
    public void serverMode() throws Exception {
        NioEventLoopGroup thread = new NioEventLoopGroup(1);
        NioServerSocketChannel server = new NioServerSocketChannel();

        thread.register(server);  //epoll_ctl(5,ADD,3)
        final ChannelPipeline pipeline = server.pipeline();
        pipeline.addLast(new MyAcceptHandler(thread, new ChannelInit()));

        final ChannelFuture bind = server.bind(new InetSocketAddress(9090));
        bind.sync().channel().closeFuture().sync();
        System.out.println("server close....");
    }

    @Test
    public void nettyMode() throws Exception {
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        ServerBootstrap bootstrap = new ServerBootstrap();
        final ChannelFuture bind = bootstrap.group(group, group)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInit())
                .bind(9090);
        bind.sync().channel().closeFuture().sync();
        System.out.println("server close....");
    }

    public static void print(ByteBuf buf) {
        System.out.println("buf.isReadable()    :" + buf.isReadable());
        System.out.println("buf.readerIndex()   :" + buf.readerIndex());
        System.out.println("buf.readableBytes() " + buf.readableBytes());
        System.out.println("buf.isWritable()    :" + buf.isWritable());
        System.out.println("buf.writerIndex()   :" + buf.writerIndex());
        System.out.println("buf.writableBytes() :" + buf.writableBytes());
        System.out.println("buf.capacity()  :" + buf.capacity());
        System.out.println("buf.maxCapacity()   :" + buf.maxCapacity());
        System.out.println("buf.isDirect()  :" + buf.isDirect());
        System.out.println("--------------");

    }
}

class MyAcceptHandler extends ChannelInboundHandlerAdapter {
    private EventLoopGroup selector;
    private ChannelHandler handler;

    public MyAcceptHandler(EventLoopGroup selector, ChannelHandler handler) {
        this.selector = selector;
        this.handler = handler;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("server registered...");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        final NioSocketChannel client = (NioSocketChannel) msg;
        final ChannelPipeline pipeline = client.pipeline();
        pipeline.addLast(handler);

        selector.register(client);
    }
}

class MyInHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("client registered...");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("client active...");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        final ByteBuf buf = (ByteBuf) msg;
        final CharSequence str = buf.readCharSequence(buf.readableBytes(), CharsetUtil.UTF_8);
        System.out.println(str);
        ctx.channel().write(buf);
    }
}

//为啥要有一个inithandler，可以没有，但是MyInHandler就得设计成单例
@ChannelHandler.Sharable
class ChannelInit extends ChannelInboundHandlerAdapter{

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        Channel client = ctx.channel();
        ChannelPipeline p = client.pipeline();
        p.addLast(new MyInHandler());//2,client::pipeline[ChannelInit,MyInHandler]
        ctx.pipeline().remove(this);
        //3,client::pipeline[MyInHandler]
    }

//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        System.out.println("haha");
//        super.channelRead(ctx, msg);
//    }
}
