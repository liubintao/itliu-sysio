package org.itliu.sysio.rpc.test.packmode;

import org.itliu.sysio.rpc.test.rpcenv.PackageMsg;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @desc
 * @auther itliu
 * @date 2020/7/21
 */
public class MsgCallbackMapping {

    private static final Map<Long, CompletableFuture> mapping = new ConcurrentHashMap<>();

    public static final void addCallback(long requestId, CompletableFuture future) {
        mapping.putIfAbsent(requestId, future);
    }

    public static void runCallback(PackageMsg packageMsg) {
        CompletableFuture cf = mapping.get(packageMsg.getMessageId());
        cf.complete(packageMsg.getContent().getMsg());
        removeCB(packageMsg.getMessageId());
    }

    public static void removeCB(long requestId) {
        mapping.remove(requestId);
    }
}
