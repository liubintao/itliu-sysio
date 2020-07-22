package org.itliu.sysio.rpc.test.rpcenv.connectpool;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.itliu.sysio.rpc.test.packmode.MsgCallbackMapping;
import org.itliu.sysio.rpc.test.rpcenv.PackageMsg;

/**
 * @desc
 * @auther itliu
 * @date 2020/7/21
 */
public class ClientResponsesHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        PackageMsg packageMsg = (PackageMsg) msg;
        MsgCallbackMapping.runCallback(packageMsg);
    }
}
