package org.itliu.sysio.rpc.test.proxymode.rpcprotocol;


import org.itliu.sysio.rpc.test.rpcenv.PackageMsg;
import org.itliu.sysio.rpc.test.rpcenv.connectpool.ClientFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;

import static org.itliu.sysio.rpc.test.proxymode.rpcprotocol.RpcContent.createContent;

/**
 * @desc
 * @auther itliu
 * @date 2020/7/21
 */
public class RpcProxyUtil {
    public static <T> T proxyGet(Class<T> interfaceInfo) {


        ClassLoader loader = interfaceInfo.getClassLoader();
        Class<?>[] methodInfo = {interfaceInfo};


        return (T) Proxy.newProxyInstance(loader, methodInfo, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String name = interfaceInfo.getName();
                String methodName = method.getName();
                Class<?>[] parameterTypes = method.getParameterTypes();
                //业务协议体body
                RpcContent content = createContent(name, methodName, args, parameterTypes);
                //通信协议头header及requestID及body体
                PackageMsg packageMsg = PackageMsg.requestPackage(content);
                CompletableFuture<Object> cf = ClientFactory.send(packageMsg, interfaceInfo);
                return cf.get();
            }
        });
    }
}
