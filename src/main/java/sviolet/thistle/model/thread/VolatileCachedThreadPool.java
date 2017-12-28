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

package sviolet.thistle.model.thread;

import sviolet.thistle.util.common.ThreadPoolExecutorUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * <p>易失性缓存线程池</p>
 *
 * <p>可指定等待队列长度, 当等待队列满时, 新加入的任务会被抛弃, 且{@link VolatileCachedThreadPool#execute(Runnable)}方法返回false</p>
 *
 * Created by S.Violet on 2016/4/12.
 */
public class VolatileCachedThreadPool {

    private ExecutorService executor;

    /**
     * @param coreThreads 核心线程数(即使闲置状态,也不会销毁的线程)
     * @param maximumThreads 最大并发线程数(最大线程数, 非核心线程在60s闲置后会销毁)
     * @param waitingQueueSize 等待队列长度, 等待队列满时, 新任务将被直接拒绝
     * @param threadNameFormat 线程名称格式, VolatileCachedThreadPool-%d
     */
    public VolatileCachedThreadPool(int coreThreads, int maximumThreads, int waitingQueueSize, String threadNameFormat) {
        executor = ThreadPoolExecutorUtils.newInstance(
                coreThreads,
                maximumThreads,
                60L,
                threadNameFormat != null ? threadNameFormat : "VolatileCachedThreadPool-%d",
                new CustomLinkedBlockingQueue(waitingQueueSize),
                null,
                null);
    }

    /**
     * 执行任务
     * @param command Runnable
     * @return true:任务加入队列成功(将被执行) false:任务加入队列失败(拒绝执行)
     */
    public boolean execute(Runnable command){
        try{
            executor.execute(command);
            return true;
        }catch(Exception e){
            //抛出异常, 视为入队失败
            return false;
        }
    }

    public void shutdown() {
        executor.shutdown();
    }

    public List<Runnable> shutdownNow() {
        return executor.shutdownNow();
    }

    public boolean isShutdown() {
        return executor.isShutdown();
    }

    public boolean isTerminated() {
        return executor.isTerminated();
    }

    private static class CustomLinkedBlockingQueue extends LinkedBlockingQueue<Runnable>{

        public CustomLinkedBlockingQueue(int capacity) {
            super(capacity);
        }

        @Override
        public boolean offer(Runnable runnable) {
            if (super.offer(runnable)){
                return true;
            }
            //入队失败抛出异常
            throw new RuntimeException("[VolatileCachedThreadPool.CustomLinkedBlockingQueue]queue is full");
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (executor != null){
            executor.shutdown();
        }
    }
}
