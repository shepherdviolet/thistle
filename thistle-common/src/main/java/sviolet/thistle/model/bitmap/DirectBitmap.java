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
 * 注意!!! 这个类请谨慎使用, 内存分配在堆外, 小心内存泄露!
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
    protected final ByteBuffer buildBuffer(int bufferSize) {
        return ByteBuffer.allocateDirect(bufferSize);
    }

    public boolean destroy(){
        return CloseableUtils.cleanMappedByteBuffer(buffer);
    }

}
