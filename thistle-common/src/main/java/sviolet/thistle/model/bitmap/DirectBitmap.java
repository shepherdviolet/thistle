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

import sviolet.thistle.util.common.CloseableUtils;

import java.nio.ByteBuffer;

/**
 * [非线程安全]使用直接内存的Bitmap, 占用内存 = size / 8 .
 *
 * 注意!!! 这个类请谨慎使用, 内存分配在堆外, 小心内存泄露!!!
 *
 * 一致性: extract/inject操作有同步锁, put/get/bloomAdd/bloomContains无同步锁, 且不保证内存可见性(非CAS操作).
 *
 * @see Bitmap
 * @see BloomBitmap
 * @author S.Violet
 */
public class DirectBitmap extends HeapBitmap {

    public DirectBitmap(int size) {
        super(size);
    }

    public DirectBitmap(byte[] data) {
        super(data);
    }

    @Override
    protected final BitmapOperator buildBuffer(final int bufferSize) {
        return new BitmapOperator() {

            //Direct buffer
            private final ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);

            @Override
            public byte get(int index) {
                return buffer.get(index);
            }

            @Override
            public synchronized void extract(byte[] dst, int offset) {
                buffer.position(offset);
                buffer.get(dst, 0, dst.length);
            }

            @Override
            public boolean put(int index, byte newValue, byte oldValue) {
                buffer.put(index, newValue);
                return true;
            }

            @Override
            public synchronized void inject(byte[] src, int offset) {
                buffer.position(offset);
                buffer.put(src, 0, src.length);
            }

            @Override
            public Object getProvider() {
                return buffer;
            }
        };
    }

    public boolean destroy(){
        return CloseableUtils.cleanMappedByteBuffer((ByteBuffer) operator.getProvider());
    }

}
