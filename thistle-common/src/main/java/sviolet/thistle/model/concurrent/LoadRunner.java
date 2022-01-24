/*
 * Copyright (C) 2015-2022 S.Violet
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
 * Project GitHub: https://github.com/shepherdviolet/thistle
 * Email: shepherdviolet@163.com
 */

package sviolet.thistle.model.concurrent;

import sviolet.thistle.util.concurrent.ThreadPoolExecutorUtils;

import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 负载执行器, 多线程执行同一个任务, 可以实时调整并发数/时间间隔
 *
 * @author shepherdviolet
 */
public class LoadRunner implements AutoCloseable, Closeable {

    private final Task task;

    private volatile int maxThreadNum = 0;
    private volatile int intervalMillis = 0;

    private final AtomicInteger currentThreadNum = new AtomicInteger(0);

    private final ExecutorService dispatcherThreadPool = ThreadPoolExecutorUtils.createLazy(10, "LoadRunner-dispatcher");
    private final ExecutorService workerThreadPool = ThreadPoolExecutorUtils.createCached(0, Integer.MAX_VALUE, 10, "LoadRunner-worker-%d");

    /**
     * @param task 执行的任务
     * @param maxThreadNum 最大线程数
     * @param intervalMillis 单线程执行间隔
     */
    public LoadRunner(Task task, int maxThreadNum, int intervalMillis) {
        if (task == null) {
            throw new IllegalArgumentException("task is null");
        }
        if (maxThreadNum < 0) {
            throw new IllegalArgumentException("maxThreadNum < 0");
        }
        if (intervalMillis < 0) {
            throw new IllegalArgumentException("intervalMillis < 0");
        }

        this.task = task;
        this.maxThreadNum = maxThreadNum;
        this.intervalMillis = intervalMillis;
    }

    public LoadRunner setMaxThreadNum(int maxThreadNum) {
        this.maxThreadNum = maxThreadNum;
        start();
        return this;
    }

    public LoadRunner setIntervalMillis(int intervalMillis) {
        this.intervalMillis = intervalMillis;
        return this;
    }

    public LoadRunner start() {
        dispatcherThreadPool.execute(DISPATCH_TASK);
        return this;
    }

    @Override
    public void close() {
        setMaxThreadNum(0);
        try {
            workerThreadPool.shutdownNow();
        } catch (Throwable ignore){
        }
        try {
            dispatcherThreadPool.shutdownNow();
        } catch (Throwable ignore){
        }
    }

    public int getMaxThreadNum() {
        return maxThreadNum;
    }

    public int getIntervalMillis() {
        return intervalMillis;
    }

    public int getCurrentThreadNum() {
        return currentThreadNum.get();
    }

    private final Runnable DISPATCH_TASK = new Runnable() {
        @Override
        public void run() {
            for (int i = 0 ; i < maxThreadNum && currentThreadNum.get() < maxThreadNum ; i++) {

                workerThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        int id = currentThreadNum.getAndIncrement();
                        while (id < maxThreadNum) {
                            try {
                                if (intervalMillis > 0L) {
                                    //noinspection BusyWait
                                    Thread.sleep(intervalMillis);
                                }
                                task.onExecute(id);
                            } catch (InterruptedException ignore) {
                            } catch (Throwable t) {
                                try {
                                    task.onException(id, t);
                                } catch (Throwable ignore) {
                                }
                            }
                        }
                        currentThreadNum.getAndDecrement();
                    }
                });

            }
        }
    };

    /**
     * 任务
     */
    public interface Task {

        /**
         * 任务执行代码
         */
        void onExecute(int id);

        /**
         * 异常处理
         */
        void onException(int id, Throwable t);

    }

}
