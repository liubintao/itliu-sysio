package org.itliu.sysio.bio;

import java.io.*;
import java.net.Socket;

/**
 * @desc
 * @auther itliu
 * @date 2020/7/6
 */
public class SocketBIOClient {
    public static void main(String[] args) {
        try {
            Socket client = new Socket("10.0.0.5", 9090);

            //缓冲区20 byte
            client.setSendBufferSize(20);
            client.setTcpNoDelay(true);
            client.setOOBInline(false);
            OutputStream out = client.getOutputStream();

            InputStream in = System.in;
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            for (;;) {
                String line = reader.readLine();
                if (line != null) {
                    final byte[] bytes = line.getBytes();
                    for (byte b : bytes) {
                        //读到数据就给服务端发送，注意：没有flush，依靠kernel
                        out.write(b);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
