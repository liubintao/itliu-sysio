package org.itliu.sysio.rpc.test.client;

import org.itliu.sysio.rpc.test.api.Car;
import org.itliu.sysio.rpc.test.proxymode.rpcprotocol.RpcProxyUtil;
import org.itliu.sysio.rpc.test.rpcenv.serverenv.ServerRPCEnv;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @desc
 * @auther itliu
 * @date 2020/7/21
 */
public class MyRPCTest {

    @Test
    public void get() {
        final AtomicInteger num = new AtomicInteger(0);
        //server start
        new Thread(() -> {
            ServerRPCEnv.startServer();
        }).start();

        System.out.println("server started......");

        //模拟客户端并发
        //串行
        /*for (int i = 0; i < 20; i++) {
            System.out.println("=========client start");
            Car car = RpcProxyUtil.proxyGet(Car.class);
            String msg = "hello server, " + num.incrementAndGet();
            String res = car.run(msg);
            System.out.println(res + " from : " + msg);
        }*/

        //并发
        int size = 50;
        Thread[] threads = new Thread[size];
        for (int i = 0; i < size; i++) {
            threads[i] = new Thread(() -> {
                Car car = RpcProxyUtil.proxyGet(Car.class);//动态代理实现
                String msg = "helloa";
                String ooxx = car.run(msg);
                System.out.println(ooxx + " from ..." + msg);
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
