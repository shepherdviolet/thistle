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

import java.nio.ByteBuffer;

/**
 * [非线程安全]使用堆内存(HEAP)的Bitmap, 占用内存 8bit -> 1byte.
 *
 * 一致性: extract/inject操作有同步锁, put/get/bloomAdd/bloomContains无同步锁, 且不保证内存可见性(非CAS操作).
 *
 * @see Bitmap
 * @see BloomBitmap
 * @author S.Violet
 */
public class HeapBitmap extends AbstractBitmap {

    //Heap buffer
    private ByteBuffer buffer;

    public HeapBitmap(int size) {
        super(size);
    }

    public HeapBitmap(byte[] data) {
        super(data);
    }

    @Override
    protected void dataAccess_init(int slotSize) {
        buffer = ByteBuffer.allocate(slotSize);
    }

    @Override
    protected byte dataAccess_getSlot(int index) {
        return buffer.get(index);
    }

    @Override
    protected boolean dataAccess_putSlot(int index, byte newValue, byte oldValue) {
        buffer.put(index, newValue);
        return true;
    }

    @Override
    protected void dataAccess_extract(byte[] dst, int offset) {
        synchronized (this) {
            buffer.position(offset);
            buffer.get(dst, 0, dst.length);
        }
    }

    @Override
    protected void dataAccess_inject(byte[] src, int offset) {
        synchronized (this) {
            buffer.position(offset);
            buffer.put(src, 0, src.length);
        }
    }

}
