package org.itliu.sysio.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @desc BIO服务端
 * @auther itliu
 * @date 2020/7/6
 */
public class SocketBIOServerProperties {

    //server socket listen property:
    private static final int RECEIVE_BUFFER = 10;
    private static final int SO_TIMEOUT = 0;
    private static final boolean REUSE_ADDR = false;
    //连接太多的时候，操作系统层面有多少个可以排队的，超过的全拒绝
    private static final int BACK_LOG = 2;
    //client socket listen property on server endpoint:
    //是否代表长连接
    private static final boolean CLI_KEEPALIVE = false;
    //是否优先一个字符先发过去试探一下
    private static final boolean CLI_OOB = false;
    //使用netstat -antp时可以看到Recv-Q
    private static final int CLI_REC_BUF = 20;
    //是否重用地址
    private static final boolean CLI_REUSE_ADDR = false;
    private static final int CLI_SEND_BUF = 20;
    //断开连接的速度
    private static final boolean CLI_LINGER = true;
    private static final int CLI_LINGER_N = 0;
    //client读取的时候是阻塞的，可以加一个超时
    private static final int CLI_TIMEOUT = 0;
    //TCP的一个优化算法，在发送数据比较少的时候可以利用缓冲
    private static final boolean CLI_NO_DELAY = false;

    /*

    StandardSocketOptions.TCP_NODELAY
    StandardSocketOptions.SO_KEEPALIVE
    StandardSocketOptions.SO_LINGER
    StandardSocketOptions.SO_RCVBUF
    StandardSocketOptions.SO_SNDBUF
    StandardSocketOptions.SO_REUSEADDR

 */
    public static void main(String[] args) {
        ServerSocket server = null;
        try {
            server = new ServerSocket();
            //监听9090端口
            server.bind(new InetSocketAddress(9090));
            //设置参数
            server.setReceiveBufferSize(RECEIVE_BUFFER);
            server.setReuseAddress(REUSE_ADDR);
            server.setSoTimeout(SO_TIMEOUT);



        System.out.println("server up use 9090!");

        for (; ; ) {
            try {
                //阻塞，用于查看中间连接状态，此时三次握手只是在内核层面分配完了资源，socket四元组还没有分配给进程
                System.in.read();

                Socket client = server.accept();
                System.out.println("client's remotes addr is :" + client.getRemoteSocketAddress() +
                        ", port is:" + client.getPort());

                //给client设置参数
                client.setKeepAlive(CLI_KEEPALIVE);
                client.setOOBInline(CLI_OOB);
                client.setReceiveBufferSize(CLI_REC_BUF);
                client.setReuseAddress(CLI_REUSE_ADDR);
                client.setSendBufferSize(CLI_SEND_BUF);
                client.setSoLinger(CLI_LINGER, CLI_LINGER_N);
                client.setSoTimeout(CLI_TIMEOUT);
                client.setTcpNoDelay(CLI_NO_DELAY);

                //最原始的方式，接收到一个client分配一个新的线程执行
                new Thread(() -> {
                    for (; ; ) {
                        try {
                            InputStream in = client.getInputStream();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                            char[] data = new char[1024];
                            final int num = reader.read(data);

                            if (num > 0) {//从client读到了数据
                                System.out.println("client read some data is :" + num + " val :" + new String(data, 0, num));
                            } else if (num == 0) {//没有读到数据继续读
                                continue;
                            } else { //client连接断开了
                                client.close();
                                break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
