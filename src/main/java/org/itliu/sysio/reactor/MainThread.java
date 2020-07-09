package org.itliu.sysio.reactor;

/**
 * @desc
 * @auther itliu
 * @date 2020/7/8
 */
public class MainThread {

    public static void main(String[] args) {
        //不做IO和业务，只做启动类

        //1.创建IO Thread（一个或多个）
        SelectorThreadGroup boss = new SelectorThreadGroup(3);//boss有自己的线程组
        SelectorThreadGroup worker = new SelectorThreadGroup(3);//worker有自己的线程组
        //2.应该把监听注册到哪个selector上

        boss.setWorker(worker);
        //但是，boss得多持有worker的引用：
        /**
         * boss里选一个线程注册listen ， 触发bind，从而，这个被选中的线程得持有 workerGroup的引用
         * 因为未来 listen 一旦accept得到client后得去worker中 next出一个线程分配
         */
        boss.bind(9090);
    }
}
