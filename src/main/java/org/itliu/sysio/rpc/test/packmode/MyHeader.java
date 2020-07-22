package org.itliu.sysio.rpc.test.packmode;

import java.io.Serializable;

/**
 * @desc
 * @auther itliu
 * @date 2020/7/21
 */
public class MyHeader implements Serializable {
    private int flag;
    private long requestId;
    private int dataLen;

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public int getDataLen() {
        return dataLen;
    }

    public void setDataLen(int dataLen) {
        this.dataLen = dataLen;
    }
}
