package org.itliu.sysio.fileio;

import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;


/**
 * @desc
 * @auther itliu
 * @date 2020/7/2
 */
public class OSFileIO {

    static byte[] data = "123456789\n".getBytes();
    static String path = "/opt/testfileio/out.txt";

    public static void main(String[] args) throws Exception {
        switch (args[0]) {
            case "0":
                testBasicFileIO();
                break;
            case "1":
                testBufferedFileIO();
                break;
            case "2":
                testRandomAccessFileWrite();
                break;
            case "3":
                break;
            default:

        }
    }

    private static void testBasicFileIO() throws Exception {
        File file = new File(path);
        FileOutputStream out = new FileOutputStream(file);

        //不断的往文件里写数据
        for (; ; ) {
            Thread.sleep(10);
            out.write(data);
            //注意：此处为了测试，不进行out.flush()；
        }
    }

    //测试buffer文件IO
    //  jvm  8kB   syscall  write(8KBbyte[])

    /**
     * @see BufferedOutputStream#BufferedOutputStream(OutputStream) 构造方法中第二个参数为8192
     * 也就是8192 byte = 8Kb
     * SO 每次 syscall 会写出8Kb的数据
     */
    private static void testBufferedFileIO() throws Exception {
        File file = new File(path);
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));

        for (; ; ) {
            Thread.sleep(10);
            out.write(data);
        }
    }

    private static void testRandomAccessFileWrite() throws Exception {
        RandomAccessFile file = new RandomAccessFile(path, "rw");
        //先写两行数据
        file.write("hello world\n".getBytes());
        file.write("hello computer\n".getBytes());

        System.out.println("write---------------------");
        System.in.read();

        //测试随机读写能力
        file.seek(4);
        file.write("xxoo".getBytes());
        System.out.println("seek---------------------");
        System.in.read();

        FileChannel channel = file.getChannel();
        /**
         * 使用mmap这个syscall开辟一块堆外内存，这块内存是和文件映射的
         */
        MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, 4096);
        map.put("@@@".getBytes());//不是系统调用  但是数据会到达 内核的pagecache

        /**
         * 曾经我们是需要out.write()  这样的系统调用，才能让程序的data 进入内核的pagecache
         * 曾经必须有用户态内核态切换
         * mmap的内存映射，依然是内核的pagecache体系所约束的！！！
         * 换言之，丢数据
         * 你可以去github上找一些 其他C程序员写的jni扩展库，使用linux内核的Direct IO
         * 直接IO是忽略linux的pagecache
         * 是把pagecache  交给了程序自己开辟一个字节数组当作pagecache，动用代码逻辑来维护一致性/dirty。。。一系列复杂问题
         */
        System.out.println("map--put--------");
        System.in.read();

//        map.force(); //  flush

        file.seek(0);

        ByteBuffer buffer = ByteBuffer.allocate(8192);//分配堆内内存
//        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);//分配对外内存

        final int read = channel.read(buffer);
        System.out.println(buffer);
        buffer.flip();
        System.out.println(buffer);

        for (int i = 0; i < buffer.limit(); i++) {
            Thread.sleep(200);
            System.out.println((char) buffer.get(i));
        }
    }

    @Test
    public  void whatByteBuffer(){

//        ByteBuffer buffer = ByteBuffer.allocate(1024);
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);


        System.out.println("postition: " + buffer.position());
        System.out.println("limit: " +  buffer.limit());
        System.out.println("capacity: " + buffer.capacity());
        System.out.println("mark: " + buffer);

        buffer.put("123".getBytes());

        System.out.println("-------------put:123......");
        System.out.println("mark: " + buffer);

        buffer.flip();   //读写交替

        System.out.println("-------------flip......");
        System.out.println("mark: " + buffer);

        buffer.get();

        System.out.println("-------------get......");
        System.out.println("mark: " + buffer);

        buffer.compact();

        System.out.println("-------------compact......");
        System.out.println("mark: " + buffer);

        buffer.clear();

        System.out.println("-------------clear......");
        System.out.println("mark: " + buffer);

    }

}
