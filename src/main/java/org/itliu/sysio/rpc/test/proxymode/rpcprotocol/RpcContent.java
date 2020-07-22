package org.itliu.sysio.rpc.test.proxymode.rpcprotocol;

import java.io.Serializable;

/**
 * @desc
 * @auther itliu
 * @date 2020/7/21
 */
public class RpcContent implements Serializable {
    private String name;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] args;
    private String msg;

    public RpcContent(String s) {
        msg = s;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public static RpcContent createContent(String serviceName, String methodName, Object[] args, Class<?>[] parameterTypes) {
        RpcContent content = new RpcContent("send..msg");
        content.setArgs(args);
        content.setName(serviceName);
        content.setMethodName(methodName);
        content.setParameterTypes(parameterTypes);
        return content;

    }
}
