package org.itliu.sysio.rpc.test.rpcenv.connectpool;

import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @desc 客户端连接池
 * @auther itliu
 * @date 2020/7/21
 */
public class ClientPool {
    NioSocketChannel[] clients;
    Object[] locks;

    public ClientPool(int poolSize) {
        clients = new NioSocketChannel[poolSize];//连接init都是空的，什么时候new什么时候放进来
        locks = new Object[poolSize];

        for (int i = 0; i < locks.length; i++) {
            locks[i] = new Object();
        }
    }
}
