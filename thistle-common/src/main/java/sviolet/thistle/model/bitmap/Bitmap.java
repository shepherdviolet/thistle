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

/**
 * <p>Bitmap</p>
 *
 * <p>Bitmap能够精确的判断一个元素是否在集合中存在, 但元素的主键必须为不重复的数字, 且数字有界. </p>
 *
 * @author S.Violet
 */
public interface Bitmap {

    /**
     * 取值
     * @param bitIndex 索引位置(比特位置)
     * @return true / false
     */
    boolean get(int bitIndex);

    /**
     * 存值
     * @param bitIndex 索引位置(比特位置)
     * @param value 值
     */
    void put(int bitIndex, boolean value);

    /**
     * 抽取(复制)全部数据
     */
    byte[] extractAll();

    /**
     * 抽取(复制)部分数据
     * @param dst 抽取的数据存放到这里
     * @param offset 起始位置(注意, 这个不是比特位置, 是字节位置; 不是dst的位置)
     * @param length 抽取长度(注意, 这个不是比特长度, 是字节长度)
     */
    void extract(byte[] dst, int offset, int length);

    /**
     * 导入数据
     * @param src 需要被导入的数据
     * @param offset 起始位置(注意, 这个不是比特位置, 是字节位置; 不是src的位置)
     * @param length 导入长度(注意, 这个不是比特长度, 是字节长度)
     */
    void inject(byte[] src, int offset, int length);

    /**
     * Bitmap的总比特长度
     */
    int size();

}
