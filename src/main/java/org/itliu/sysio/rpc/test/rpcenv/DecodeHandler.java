package org.itliu.sysio.rpc.test.rpcenv;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.itliu.sysio.rpc.test.packmode.MyHeader;
import org.itliu.sysio.rpc.test.proxymode.rpcprotocol.RpcContent;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.List;

/**
 * @desc
 * @auther itliu
 * @date 2020/7/21
 */
public class DecodeHandler extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        while (buf.readableBytes() >= 108) {
            byte[] bytes = new byte[108];
            buf.getBytes(buf.readerIndex(), bytes);

            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            ObjectInputStream oin = new ObjectInputStream(in);
            MyHeader header = (MyHeader) oin.readObject();

            if ((buf.readableBytes() - 108) >= header.getDataLen()) {
                buf.readBytes(108);
                byte[] data = new byte[header.getDataLen()];
                buf.readBytes(data);

                ByteArrayInputStream din = new ByteArrayInputStream(data);
                ObjectInputStream doin = new ObjectInputStream(din);
                if (header.getFlag() == 0x14141414) {
                    RpcContent content = (RpcContent) doin.readObject();
                    out.add(new PackageMsg(header.getRequestId(), header, content));
                } else {
                    RpcContent content = (RpcContent) doin.readObject();

                    out.add(new PackageMsg(header.getRequestId(), header, content));
                }

            } else {
                break;
            }
        }
    }
}
