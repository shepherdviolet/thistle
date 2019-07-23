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

import sviolet.thistle.util.conversion.HashUtils;

import java.nio.ByteBuffer;

/**
 * [非线程安全]使用堆内存(HEAP)的Bitmap, 占用内存 = size / 8.
 *
 * @see Bitmap
 * @see BloomBitmap
 * @author S.Violet
 */
public class HeapBitmap implements BloomBitmap {

    //00000001 00000010 00000100 00001000 ...
    private static final byte[] F = new byte[8];
    //11111110 11111101 11111011 11110111 ...
    private static final byte[] R = new byte[8];

    static {
        for (int i = 0 ; i < 8 ; i++) {
            R[i] = (byte) ((F[i] = (byte) (0x01 << i)) ^ 0xFF);
        }
    }

    protected final ByteBuffer buffer;
    protected final int size;

    public HeapBitmap(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("The size must >= 0, but it's " + size);
        }
        if ((toSlotOffset(size)) > 0) {
            throw new IllegalArgumentException("The size must be a multiple of 8, but it's " + size);
        }
        this.size = size;
        this.buffer = buildBuffer(toSlotIndex(size));
    }

    public HeapBitmap(byte[] data) {
        this(data != null ? data.length : 0);
        if (data == null || data.length <= 0) {
            return;
        }
        //导入数据
        this.buffer.put(data, 0, data.length);
    }

    /**
     * 创建ByteBuffer
     */
    protected ByteBuffer buildBuffer(int bufferSize){
        return ByteBuffer.allocate(bufferSize);
    }

    public boolean get(int bitIndex) {
        if (bitIndex < 0 || bitIndex >= size) {
            throw new IllegalArgumentException("Out of bound, The bitIndex must >= 0 and < " + size + ", but it's " + bitIndex);
        }
        byte slot = buffer.get(toSlotIndex(bitIndex));
        return (slot & F[toSlotOffset(bitIndex)]) != 0;
    }

    public void put(int bitIndex, boolean value) {
        if (bitIndex < 0 || bitIndex >= size) {
            throw new IllegalArgumentException("Out of bound, The bitIndex must >= 0 and < " + size + ", but it's " + bitIndex);
        }
        int slotIndex = toSlotIndex(bitIndex);
        byte slot = buffer.get(slotIndex);
        buffer.put(slotIndex, value ? (byte) (slot | F[toSlotOffset(bitIndex)]) :
                (byte) (slot & R[toSlotOffset(bitIndex)]));
    }

    @Override
    public byte[] extractAll(){
        byte[] result = new byte[toSlotIndex(size)];
        this.buffer.get(result, 0, result.length);
        return result;
    }

    @Override
    public void extract(byte[] dst, int offset, int length) {
        this.buffer.get(dst, offset, length);
    }

    @Override
    public void inject(byte[] src, int offset, int length) {
        this.buffer.put(src, offset, length);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void bloomAdd(byte[] data){
        if (data == null) {
            data = new byte[0];
        }
        int[] hashes = bloomHash(data);
        for (int hash : hashes) {
            put(hash % size, true);
        }
    }

    @Override
    public boolean bloomContains(byte[] data) {
        if (data == null) {
            data = new byte[0];
        }
        int[] hashes = bloomHash(data);
        for (int hash : hashes) {
            if (!get(hash % size)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 布隆hash算法, 可自定义
     */
    protected int[] bloomHash(byte[] data){
        int[] hashes = new int[3];
        hashes[0] = HashUtils.djb2(data);
        hashes[1] = HashUtils.sdbm(data);
        hashes[2] = HashUtils.fnv1(data);
        return hashes;
    }

    private int toSlotIndex(int num) {
        return num >> 3;
    }

    private int toSlotOffset(int bitIndex) {
        return bitIndex & 0x07;
    }

}
