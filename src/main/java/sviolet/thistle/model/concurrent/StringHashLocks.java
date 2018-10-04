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

import sviolet.thistle.util.math.MathUtils;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 产生指定数量的同步锁, 根据字符串的哈希获取锁对象, 这样可以把同步代码块分散, 提高并发性能
 *
 * @author S.Violet
 */
public class StringHashLocks {

    private ReentrantLock[] reentrantLocks;
    private int barrier;

    public StringHashLocks() {
        this(32);
    }

    /**
     * @param hashLockNum 锁数量, 16 32 64 128 256 512 1024, 数量越多发生碰撞的可能性更低, 但是消耗更多的内存
     */
    public StringHashLocks(int hashLockNum) {
        //limit
        if (hashLockNum < 16 || hashLockNum > 1024 || !MathUtils.isPowerOfTwo(hashLockNum)) {
            throw new IllegalArgumentException("hashLockNum must be 16 32 64 128 256 512 1024");
        }
        //create lock
        reentrantLocks = new ReentrantLock[hashLockNum];
        for (int i = 0 ; i < hashLockNum ; i++) {
            reentrantLocks[i] = new ReentrantLock();
        }
        //build barrier
        barrier = hashLockNum - 1;
    }

    /**
     * 根据字符串的哈希获取锁对象
     * @param str 字符串, 尽量不要送null
     * @return ReentrantLock
     */
    public ReentrantLock getLock(String str){
        int slot = hash(str) & barrier;
        return reentrantLocks[slot];
    }

    /**
     * hash
     */
    private int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

}
