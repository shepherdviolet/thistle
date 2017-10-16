/*
 * Copyright (C) 2015-2016 S.Violet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Project GitHub: https://github.com/shepherdviolet/turquoise
 * Email: shepherdviolet@163.com
 */

package sviolet.thistle.model.thread;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 惰性单线程池<p/>
 *
 * 1.内部维护一个单线程池, 同时只能执行一个任务(Runnable).<br/>
 * 2.设定一个最大队列长度(maxQueueLength, 默认2), 同时塞入多个任务时, 超过设定值的任务会被放弃执行.<p/>
 *
 * 可用于实现守护进程/调度进程/清扫进程.<br/>
 *
 * Created by S.Violet on 2016/1/7.
 */
public class LazySingleThreadPool {

    //默认最大队列长度
    private static final int DEFAULT_MAX_QUEUE_LENGTH = 2;

    //单线程池
    private ExecutorService singleThreadPool;

    //最大队列长度
    private int maxQueueLength = DEFAULT_MAX_QUEUE_LENGTH;
    //队列长度
    private AtomicInteger queueLength = new AtomicInteger(0);
    //锁
    private final ReentrantLock locker = new ReentrantLock();

    /**
     * 惰性单线程池<p/>
     *
     * 1.内部维护一个单线程池, 同时只能执行一个任务(Runnable).<br/>
     * 2.设定一个最大队列长度(maxQueueLength, 默认2), 同时塞入多个任务时, 超过设定值的任务会被放弃执行.<p/>
     *
     * 可用于实现守护进程/调度进程/清扫进程.<br/>
     *
     * Created by S.Violet on 2016/1/7.
     */
    public LazySingleThreadPool(){
        this(DEFAULT_MAX_QUEUE_LENGTH);
    }

    /**
     * 惰性单线程池<p/>
     *
     * 1.内部维护一个单线程池, 同时只能执行一个任务(Runnable).<br/>
     * 2.设定一个最大队列长度(maxQueueLength, 默认2), 同时塞入多个任务时, 超过设定值的任务会被放弃执行.<p/>
     *
     * 可用于实现守护进程/调度进程/清扫进程.<br/>
     *
     * @param maxQueueLength 最大队列长度 [1, MAX_VALUE)
     */
    public LazySingleThreadPool(int maxQueueLength){
        if (maxQueueLength < 1)
            throw new RuntimeException("[LazySingleThreadPool]maxQueueLength must >= 1");
        this.maxQueueLength = maxQueueLength;
    }

    /**
     * 请求执行任务
     * @param runnable 任务
     * @return true:确认执行 false:放弃执行
     */
    public boolean execute(final Runnable runnable){

        int length = queueLength.incrementAndGet();
        if (length > maxQueueLength){
            queueLength.decrementAndGet();
            return false;
        }

        getPool().execute(runnable);

        return true;
    }

    public void shutdown(){
        try{
            locker.lock();
            if (singleThreadPool != null) {
                singleThreadPool.shutdown();
                singleThreadPool = null;
            }
        }finally {
            locker.unlock();
        }
    }

    public void shutdownNow(){
        try{
            locker.lock();
            if (singleThreadPool != null) {
                singleThreadPool.shutdownNow();
                singleThreadPool = null;
            }
        }finally {
            locker.unlock();
        }
    }

    private ExecutorService getPool(){
        if (singleThreadPool == null){
            try{
                locker.lock();
                if (singleThreadPool == null) {
                    singleThreadPool =  new ThreadPoolExecutor(
                            0,
                            1,
                            60L,
                            TimeUnit.SECONDS,
                            new LinkedBlockingQueue<Runnable>()){
                        @Override
                        protected void afterExecute(Runnable runnable, Throwable t) {
                            super.afterExecute(runnable, t);
                            queueLength.decrementAndGet();
                        }
                    };
                }
            }finally {
                locker.unlock();
            }
        }
        return singleThreadPool;
    }

}
