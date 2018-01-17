
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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>ReentrantLock工具</p>
 *
 * @author S.Violet
 */
public class ReentrantLockUtils {

    /**
     * 使用ReentrantLock+Condition阻塞线程等待指定时间, 防止直接用{@link Condition#await()}可能出现的
     * 假性唤醒, 除了抛出InterruptedException以外, 能够保证等待足够时间(不会提早结束)
     * @param lock ReentrantLock
     * @param condition condition
     * @param timeout 等待时间(ms)
     */
    public static void awaitMillis(ReentrantLock lock, Condition condition, long timeout) {
        final long startMillis = System.currentTimeMillis();
        try {
            lock.lock();
            while (true) {
                final long remainTimeout = timeout - (System.currentTimeMillis() - startMillis);
                if (remainTimeout <= 0) {
                    return;
                }
                try {
                    if (!condition.await(remainTimeout, TimeUnit.MILLISECONDS)) {
                        return;
                    }
                } catch (InterruptedException e) {
                    return;
                }
            }
        } finally {
            lock.unlock();
        }
    }

}
