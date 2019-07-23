/*
 * Copyright (C) 2015-2019 S.Violet
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

package sviolet.thistle.model.bitmap;

import java.util.concurrent.locks.ReentrantLock;

/**
 * [线程安全:同步锁版]使用堆内存(HEAP)的Bitmap, 占用内存 = size / 8 .
 *
 * 特点: 这个Bitmap的put/get/bloomAdd/bloomContains操作性能较HeapBitmap/ConcurrentHeapBitmap有较大幅度的下降(因为同步锁开销),
 * 但内存占用情况比ConcurrentHeapBitmap小.
 *
 * 一致性: extract/inject操作有同步锁1, put/get/bloomAdd/bloomContains有同步锁2, 同步锁1和同步锁2不互斥.
 * 注意, extract/inject会强制赋值(一般用于初始数据导入导出), extract/inject与put/bloomAdd同时进行时不保证严格的一致性.
 *
 * @see Bitmap
 * @see BloomBitmap
 * @author S.Violet
 */
public class SyncHeapBitmap extends HeapBitmap {

    private ReentrantLock[] locks;
    private int lockNum;

    public SyncHeapBitmap(int size) {
        this(size, 64);
    }

    public SyncHeapBitmap(byte[] data) {
        this(data, 64);
    }

    public SyncHeapBitmap(int size, int lockNum) {
        super(size);
        initLocks(lockNum);
    }

    public SyncHeapBitmap(byte[] data, int lockNum) {
        super(data);
        initLocks(lockNum);
    }

    private void initLocks(int lockNum){
        if (lockNum < 4 || lockNum > 1024) {
            throw new IllegalArgumentException("lockNum must >= 4 and <= 1024");
        }
        this.lockNum = lockNum;
        this.locks = new ReentrantLock[lockNum];
        for (int i = 0 ; i < lockNum ; i++) {
            this.locks[i] = new ReentrantLock();
        }
    }

    @Override
    protected byte getSlot(int slotIndex) {
        ReentrantLock lock = locks[slotIndex % lockNum];
        try {
            lock.lock();
            return super.getSlot(slotIndex);
        } finally {
            lock.unlock();
        }
    }

    @Override
    protected boolean putValue(int slotIndex, int slotOffset, boolean value) {
        ReentrantLock lock = locks[slotIndex % lockNum];
        try {
            lock.lock();
            return super.putValue(slotIndex, slotOffset, value);
        } finally {
            lock.unlock();
        }
    }

}
