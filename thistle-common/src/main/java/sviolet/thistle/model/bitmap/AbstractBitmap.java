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

import java.io.IOException;

/**
 * <p>Bitmap抽象实现, 数据访问层未实现</p>
 *
 * <p>Bitmap能够精确的判断一个元素是否在集合中存在, 但元素的主键必须为不重复的数字, 且数字有界. </p>
 *
 * @see BloomBitmap
 * @author S.Violet
 */
public abstract class AbstractBitmap implements BloomBitmap {

    // 00000001 00000010 00000100 00001000 ...
    private static final byte[] F = new byte[8];

    // 11111110 11111101 11111011 11110111 ...
    private static final byte[] R = new byte[8];

    // Max length of data (byte array)
    private static final int MAX_BYTE_ARRAY_LENGTH = Integer.MAX_VALUE >> 3;

    static {
        for (int i = 0 ; i < 8 ; i++) {
            R[i] = (byte) ((F[i] = (byte) (0x01 << i)) ^ 0xFF);
        }
    }

    // bit size
    protected final int size;

    public AbstractBitmap(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("The size must be >= 0, but it's " + size);
        }
        if ((bitIndexToSlotOffset(size)) > 0) {
            throw new IllegalArgumentException("The size must be a multiple of 8, but it's " + size);
        }
        this.size = size;
        dataAccess_init(bitIndexToSlotIndex(size));
    }

    public AbstractBitmap(byte[] data) {
        if (data == null) {
            data = new byte[0];
        }
        if (data.length > MAX_BYTE_ARRAY_LENGTH) {
            throw new IllegalArgumentException("The length of data must be <= " + MAX_BYTE_ARRAY_LENGTH + ", but it's " + data.length);
        }
        this.size = data.length << 3;
        dataAccess_init(bitIndexToSlotIndex(size));

        if (data.length > 0) {
            dataAccess_inject(data, 0);
        }
    }

    /**
     * @inheritDoc
     */
    public boolean get(int bitIndex) {
        if (bitIndex < 0 || bitIndex >= size) {
            throw new IllegalArgumentException("Out of bound, The bitIndex must be >= 0 and < " + size + ", but it's " + bitIndex);
        }
        byte slot = dataAccess_getSlot(bitIndexToSlotIndex(bitIndex));
        return (slot & F[bitIndexToSlotOffset(bitIndex)]) != 0;
    }

    /**
     * @inheritDoc
     */
    public boolean put(int bitIndex, boolean value) {
        if (bitIndex < 0 || bitIndex >= size) {
            throw new IllegalArgumentException("Out of bound, The bitIndex must be >= 0 and < " + size + ", but it's " + bitIndex);
        }
        return putBitToSlot(bitIndexToSlotIndex(bitIndex), bitIndexToSlotOffset(bitIndex), value);
    }

    /**
     * 取出指定slot, 计算新值, 最后存回去
     */
    protected boolean putBitToSlot(int slotIndex, int slotOffset, boolean value) {
        //get old value
        byte oldValue = dataAccess_getSlot(slotIndex);
        //calculate new value
        byte newValue = value ? (byte) (oldValue | F[slotOffset]) : (byte) (oldValue & R[slotOffset]);
        //try to put
        return dataAccess_putSlot(slotIndex, newValue, oldValue);
    }

    /**
     * @inheritDoc
     */
    @Override
    public byte[] extractAll(){
        byte[] result = new byte[bitIndexToSlotIndex(size)];
        dataAccess_extract(result, 0);
        return result;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void extract(byte[] dst, int byteOffset) {
        dataAccess_extract(dst, byteOffset);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void inject(byte[] src, int byteOffset) {
        dataAccess_inject(src, byteOffset);
    }

    /**
     * @inheritDoc
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * 回收内存
     */
    @Override
    public void close() throws IOException {
        // Do nothing
    }

    /**
     * @inheritDoc
     */
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

    /**
     * @inheritDoc
     */
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

    protected int bitIndexToSlotIndex(int bitIndex) {
        return bitIndex >> 3;
    }

    protected int bitIndexToSlotOffset(int bitIndex) {
        return bitIndex & 0x07;
    }

    protected abstract void dataAccess_init(int slotSize);

    protected abstract byte dataAccess_getSlot(int slotIndex);

    protected abstract boolean dataAccess_putSlot(int index, byte newValue, byte oldValue);

    protected abstract void dataAccess_extract(byte[] dst, int offset);

    protected abstract void dataAccess_inject(byte[] src, int offset);

}
