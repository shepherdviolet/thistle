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

import org.junit.Assert;
import org.junit.Test;
import sviolet.thistle.util.conversion.ByteUtils;

import java.util.Random;
import java.util.UUID;

public class DirectBitmapTest {

    @Test
    public void test(){
        DirectBitmap directBitmap = new DirectBitmap(1024);
        directBitmap.put(999, true);
        Assert.assertTrue(directBitmap.get(999));
        directBitmap.put(998, true);
        directBitmap.put(999, false);
        directBitmap.put(1000, true);
        Assert.assertFalse(directBitmap.get(999));
        directBitmap.put(0, true);
        Assert.assertTrue(directBitmap.get(0));
        directBitmap.put(1023, true);
        Assert.assertTrue(directBitmap.get(1023));
        System.out.println(directBitmap.destroy());
        System.out.println(ByteUtils.bytesToHex(directBitmap.extractAll()));
    }

    public static void main(String[] args) {
//        baseline1();//性能
        baseline2();//误判率
    }

    private static final int BITMAP_SIZE = 1000000000;
    private static final int TIMES = 10000000;
    private static final Random random = new Random();

    /**
     * 性能
     */
    private static void baseline1() {
        Bitmap bitmap = new HeapBitmap(BITMAP_SIZE);
//        Bitmap bitmap = new DirectBitmap(BITMAP_SIZE);

        long time = System.currentTimeMillis();

        for (int i = 0 ; i < TIMES ; i++) {
            bitmap.put(random.nextInt(BITMAP_SIZE), i % 2 == 0);
        }

        boolean result = false;
        for (int i = 0 ; i < TIMES ; i++) {
            result = bitmap.get(random.nextInt(BITMAP_SIZE));
        }

        System.out.println(System.currentTimeMillis() - time);
        System.out.println(result);
    }

    private static final int BITMAP_SIZE_2 = 1000000000;//10亿
//    private static final int BITMAP_SIZE_2 = 100000000;//1亿
//    private static final int BITMAP_SIZE_2 = 10000000;//1000万

    private static final int TIMES_2 = 10000000;//1000万
//    private static final int TIMES_2 = 1000000;//100万

    /**
     * 误判率
     *
     * 10亿 1000万 72次 86次 约0.00001
     * 1亿 1000万 48200次 48111次 约0.005
     * 1亿 100万 7次 2次 约0.00001
     * 1000万 100万 4673次 4862次 约0.005
     */
    private static void baseline2(){
        BloomBitmap bitmap = new HeapBitmap(BITMAP_SIZE_2);
        int collisions = 0;

        for (int i = 0 ; i < TIMES_2 ; i++) {
            byte[] uuid = UUID.randomUUID().toString().getBytes();
            if (bitmap.bloomContains(uuid)) {
                collisions++;
            }
            bitmap.bloomAdd(uuid);
        }

        System.out.println(collisions);
    }

}
