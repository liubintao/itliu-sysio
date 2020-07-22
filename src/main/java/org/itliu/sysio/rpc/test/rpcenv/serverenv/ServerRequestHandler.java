package org.itliu.sysio.rpc.test.rpcenv.serverenv;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.itliu.sysio.rpc.test.proxymode.rpcprotocol.RpcContent;
import org.itliu.sysio.rpc.test.rpcenv.PackageMsg;

/**
 * @desc
 * @auther itliu
 * @date 2020/7/21
 */
public class ServerRequestHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        PackageMsg pkg = (PackageMsg) msg;

        String IOReadThreadName = Thread.currentThread().getName();
        ctx.executor().parent().next().execute(new Runnable() {
            @Override
            public void run() {
                String executeThreadName = Thread.currentThread().getName();
                String s = "io in :" + IOReadThreadName + " execute in :" + executeThreadName + " content.methodName :" + pkg.getContent().getMethodName() + " args :" + pkg.getContent().getArgs()[0];
                PackageMsg rpkg = PackageMsg.responseHeader(pkg.getMessageId(), new RpcContent(s));
                ctx.writeAndFlush(rpkg.getByteBuf());
            }
        });
    }
}
