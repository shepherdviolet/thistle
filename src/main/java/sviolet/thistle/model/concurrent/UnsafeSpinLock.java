/*
 * Copyright (C) 2015-2018 S.Violet
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

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 慎用!!! 不规范的使用会导致严重的问题!!!<br>
 *
 * 简易自旋锁, 未提供任何保护措施, 需要仔细阅读源码并谨慎使用.<br>
 *
 * <code>
 *     private UnsafeSpinLock spinLock = new UnsafeSpinLock();
 *
 *     public void setXXX(int xxx) {
 *         try {
 *             spinLock.lock();
 *             //do something ...
 *         } finally {
 *             spinLock.unlock();
 *         }
 *     }
 * </code>
 *
 * @deprecated IT'S UNSAFE !!! Irregular use can cause serious problems !!!
 * @author S.Violet
 */
@Deprecated
public class UnsafeSpinLock {

    private AtomicBoolean lock = new AtomicBoolean(false);

    /**
     * 上锁<br>
     *
     * <code>
     *     private UnsafeSpinLock spinLock = new UnsafeSpinLock();
     *
     *     public void setXXX(int xxx) {
     *         try {
     *             spinLock.lock();
     *             //do something ...
     *         } finally {
     *             spinLock.unlock();
     *         }
     *     }
     * </code>
     *
     * @deprecated IT'S UNSAFE !!! Irregular use can cause serious problems !!!
     */
    @Deprecated
    public void lock(){
        while (true) {
            if (!lock.get() && lock.compareAndSet(false, true)) {
                break;
            } else {
                Thread.yield();
            }
        }
    }

    /**
     * 解锁<br>
     *
     * <code>
     *     private UnsafeSpinLock spinLock = new UnsafeSpinLock();
     *
     *     public void setXXX(int xxx) {
     *         try {
     *             spinLock.lock();
     *             //do something ...
     *         } finally {
     *             spinLock.unlock();
     *         }
     *     }
     * </code>
     *
     * @deprecated IT'S UNSAFE !!! Irregular use can cause serious problems !!!
     */
    @Deprecated
    public void unlock(){
        lock.set(false);
    }

}
