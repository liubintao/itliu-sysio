package org.itliu.sysio.rpc.test.rpcenv;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import org.itliu.sysio.rpc.test.packmode.MyHeader;
import org.itliu.sysio.rpc.test.proxymode.rpcprotocol.RpcContent;
import org.itliu.sysio.rpc.test.util.SerDerTool;

import java.util.UUID;

/**
 * @desc 消息体
 * @auther itliu
 * @date 2020/7/21
 */
public class PackageMsg {
    private long messageId;
    byte[] headerBytes;
    byte[] bodyBytes;
    MyHeader header;
    RpcContent content;

    public PackageMsg(long msgId, MyHeader header, RpcContent content) {
        this.messageId = msgId;
        this.header = header;
        this.content = content;
    }

    public static PackageMsg responseHeader(long messageId, RpcContent content) {
        int f = 0x14141424;
        return createPackage(f, messageId, content);
    }

    public MyHeader getHeader() {
        return header;
    }

    public void setHeader(MyHeader header) {
        this.header = header;
    }

    public RpcContent getContent() {
        return content;
    }

    public void setContent(RpcContent content) {
        this.content = content;
    }

    public static PackageMsg requestPackage(RpcContent content) {
        int f = 0x14141414;
        long messageID = Math.abs(UUID.randomUUID().getLeastSignificantBits());
        return createPackage(f, messageID, content);
    }

    private static PackageMsg createPackage(int flag, long msgId, RpcContent content) {
        MyHeader header = new MyHeader();
        header.setFlag(flag);
        header.setRequestId(msgId);
        PackageMsg packageMsg = new PackageMsg(msgId, header, content);
        packageMsg.doByte();
        return packageMsg;
    }

    private void doByte() {
        this.bodyBytes = SerDerTool.obj2byteArray(content);
        header.setDataLen(bodyBytes.length);
        this.headerBytes = SerDerTool.obj2byteArray(header);
    }

    public ByteBuf getByteBuf() {
        ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer(headerBytes.length + bodyBytes.length);
        buf.writeBytes(headerBytes);
        buf.writeBytes(bodyBytes);
        return buf;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public byte[] getHeaderBytes() {
        return headerBytes;
    }

    public void setHeaderBytes(byte[] headerBytes) {
        this.headerBytes = headerBytes;
    }

    public byte[] getBodyBytes() {
        return bodyBytes;
    }

    public void setBodyBytes(byte[] bodyBytes) {
        this.bodyBytes = bodyBytes;
    }
}
