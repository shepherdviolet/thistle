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
 * 一致性: extract/inject操作有同步锁, put/get/bloomAdd/bloomContains无同步锁, 且不保证内存可见性(非CAS操作).
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

    protected final BitmapOperator operator;
    protected final int size;

    public HeapBitmap(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("The size must >= 0, but it's " + size);
        }
        if ((toSlotOffset(size)) > 0) {
            throw new IllegalArgumentException("The size must be a multiple of 8, but it's " + size);
        }
        this.size = size;
        this.operator = buildBuffer(toSlotIndex(size));
    }

    public HeapBitmap(byte[] data) {
        this(data != null ? data.length << 3 : 0);
        if (data == null || data.length <= 0) {
            return;
        }
        //导入数据
        this.operator.inject(data, 0);
    }

    /**
     * 创建BitmapOperator
     */
    protected BitmapOperator buildBuffer(final int bufferSize){
        return new BitmapOperator() {

            //Heap buffer
            private final ByteBuffer buffer = ByteBuffer.allocate(bufferSize);

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

    public boolean get(int bitIndex) {
        if (bitIndex < 0 || bitIndex >= size) {
            throw new IllegalArgumentException("Out of bound, The bitIndex must >= 0 and < " + size + ", but it's " + bitIndex);
        }
        byte slot = getSlot(toSlotIndex(bitIndex));
        return (slot & F[toSlotOffset(bitIndex)]) != 0;
    }

    protected byte getSlot(int slotIndex) {
        return operator.get(slotIndex);
    }

    public void put(int bitIndex, boolean value) {
        if (bitIndex < 0 || bitIndex >= size) {
            throw new IllegalArgumentException("Out of bound, The bitIndex must >= 0 and < " + size + ", but it's " + bitIndex);
        }
        putValue(toSlotIndex(bitIndex), toSlotOffset(bitIndex), value);
    }

    /**
     * 将比特值放入比特位置
     */
    protected boolean putValue(int slotIndex, int slotOffset, boolean value) {
        //get old value
        byte oldValue = operator.get(slotIndex);
        //calculate new value
        byte newValue = value ? (byte) (oldValue | F[slotOffset]) : (byte) (oldValue & R[slotOffset]);
        //try to put
        return operator.put(slotIndex, newValue, oldValue);
    }

    @Override
    public byte[] extractAll(){
        byte[] result = new byte[toSlotIndex(size)];
        this.operator.extract(result, 0);
        return result;
    }

    @Override
    public void extract(byte[] dst, int offset) {
        this.operator.extract(dst, offset);
    }

    @Override
    public void inject(byte[] src, int offset) {
        this.operator.inject(src, offset);
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
