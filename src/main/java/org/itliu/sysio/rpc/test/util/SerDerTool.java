package org.itliu.sysio.rpc.test.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @desc
 * @auther itliu
 * @date 2020/7/21
 */
public class SerDerTool {

    static ObjectOutputStream oot;
    static ByteArrayOutputStream bos;

    static {
        bos = new ByteArrayOutputStream();
    }

    public static synchronized byte[] obj2byteArray(Object obj) {
        byte[] bytes = null;
        try {
            bos.reset();
            oot = new ObjectOutputStream(bos);
            oot.writeObject(obj);
            bytes = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }
}
