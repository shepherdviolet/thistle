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

import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * [线程安全:CAS版]使用堆内存(HEAP)的Bitmap, 占用内存 = size / 2 .
 *
 * 特点: 这个Bitmap占用内存比HeapBitmap大四倍, put/get/bloomAdd/bloomContains性能有轻微下降, extract/inject性能有较大下降,
 * extract/inject操作过程更占内存, 因为会进行数据类型转换(byte - int).
 *
 * 一致性: extract/inject操作有同步锁, put/get/bloomAdd/bloomContains采用CAS操作, 保证了内存可见性, put/bloomAdd使用了乐观锁, 防止更新操作前的脏读.
 * 注意, extract/inject会强制赋值(一般用于初始数据导入导出), extract/inject与put/bloomAdd同时进行时不保证严格的一致性.
 *
 * @see Bitmap
 * @see BloomBitmap
 * @author S.Violet
 */
public class ConcurrentHeapBitmap extends HeapBitmap {

    private static final int RETRY_TIMES = 100;

    public ConcurrentHeapBitmap(int size) {
        super(size);
    }

    public ConcurrentHeapBitmap(byte[] data) {
        super(data);
    }

    @Override
    protected boolean putValue(int slotIndex, int slotOffset, boolean value) {
        //乐观锁
        for (int i = 0 ; i < RETRY_TIMES ; i++) {
            if (super.putValue(slotIndex, slotOffset, value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected BitmapOperator buildBuffer(final int bufferSize) {
        return new BitmapOperator() {

            private final AtomicIntegerArray buffer = new AtomicIntegerArray(bufferSize);

            @Override
            public byte get(int index) {
                return (byte) buffer.get(index);
            }

            @Override
            public synchronized void extract(byte[] dst, int offset) {
                if (dst == null) {
                    return;
                }
                for (int i = 0 ; i < dst.length ; i++) {
                    dst[i] = (byte) buffer.get(offset + i);
                }
            }

            @Override
            public boolean put(int index, byte newValue, byte oldValue) {
                return buffer.compareAndSet(index, oldValue, newValue);
            }

            @Override
            public synchronized void inject(byte[] src, int offset) {
                if (src == null) {
                    return;
                }
                for (int i = 0 ; i < src.length ; i++) {
                    buffer.set(offset + i, src[i]);
                }
            }

            @Override
            public Object getProvider() {
                return buffer;
            }
        };
    }
}
