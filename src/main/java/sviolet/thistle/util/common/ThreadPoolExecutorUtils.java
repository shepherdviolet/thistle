/*
 * Copyright (C) 2015-2017 S.Violet
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

package sviolet.thistle.util.common;

import sviolet.thistle.compat.CompatThreadFactoryBuilder;

import java.util.concurrent.*;

/**
 * 线程池工具
 */
public class ThreadPoolExecutorUtils {

    /**
     * 创建一个线程池
     * @param corePoolSize 核心线程数
     * @param maximumPoolSize 最大线程数
     * @param keepAliveSeconds 线程保活时间(秒)
     * @param threadNameFormat 线程名称格式(rpc-pool-%d)
     */
    public static ExecutorService newInstance(int corePoolSize,
                                       int maximumPoolSize,
                                       long keepAliveSeconds,
                                       String threadNameFormat){
        return newInstance(
                corePoolSize,
                maximumPoolSize,
                keepAliveSeconds,
                threadNameFormat,
                new LinkedBlockingQueue<Runnable>(),
                new ThreadPoolExecutor.AbortPolicy(),
                null);
    }

    /**
     * 创建一个线程池
     * @param corePoolSize 核心线程数
     * @param maximumPoolSize 最大线程数
     * @param keepAliveSeconds 线程保活时间(秒)
     * @param threadNameFormat 线程名称格式(rpc-pool-%d)
     * @param workQueue 等待队列, new LinkedBlockingQueue<Runnable>(1024), new SynchronousQueue<Runnable>()
     * @param rejectHandler 拒绝处理器, new ThreadPoolExecutor.AbortPolicy()
     * @param executeListener nullable, 监听执行前执行后的事件
     */
    public static ExecutorService newInstance(int corePoolSize,
                                              int maximumPoolSize,
                                              long keepAliveSeconds,
                                              String threadNameFormat,
                                              BlockingQueue<Runnable> workQueue,
                                              RejectedExecutionHandler rejectHandler,
                                              final ExecuteListener executeListener){

        if (executeListener == null) {
            return new ThreadPoolExecutor(
                    corePoolSize,
                    maximumPoolSize,
                    keepAliveSeconds,
                    TimeUnit.SECONDS,
                    workQueue,
                    new CompatThreadFactoryBuilder().setNameFormat(threadNameFormat).build(),
                    rejectHandler != null ? rejectHandler : new ThreadPoolExecutor.AbortPolicy());
        } else {
            return new ThreadPoolExecutor(
                    corePoolSize,
                    maximumPoolSize,
                    keepAliveSeconds,
                    TimeUnit.SECONDS,
                    workQueue,
                    new CompatThreadFactoryBuilder().setNameFormat(threadNameFormat).build(),
                    rejectHandler != null ? rejectHandler : new ThreadPoolExecutor.AbortPolicy()) {
                @Override
                protected void beforeExecute(Thread t, Runnable r) {
                    super.beforeExecute(t, r);
                    executeListener.beforeExecute(t, r);
                }

                @Override
                protected void afterExecute(Runnable r, Throwable t) {
                    super.afterExecute(r, t);
                    executeListener.afterExecute(r, t);
                }
            };
        }
    }

    public interface ExecuteListener {

        void beforeExecute(Thread t, Runnable r);

        void afterExecute(Runnable r, Throwable t);

    }

}
