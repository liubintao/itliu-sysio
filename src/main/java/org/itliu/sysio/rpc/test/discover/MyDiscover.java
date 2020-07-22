package org.itliu.sysio.rpc.test.discover;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @desc 缓存类
 * @auther itliu
 * @date 2020/7/21
 */
public class MyDiscover {
    private static final Map<Class<?>, InetSocketAddress> rpcMap = new ConcurrentHashMap<>();

    public static void register(Class<?> clazz, InetSocketAddress address) {
        rpcMap.putIfAbsent(clazz, address);
    }

    public static InetSocketAddress discover(Class<?> clazz) {
        return rpcMap.get(clazz);
    }
}
