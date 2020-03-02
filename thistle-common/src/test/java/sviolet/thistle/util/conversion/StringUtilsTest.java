/*
 * Copyright (C) 2015-2020 S.Violet
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

package sviolet.thistle.util.conversion;

import org.junit.Assert;
import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void truncateByUtf8ByteLength() {

        String s = "11喵aa汪";
//        System.out.println(ByteUtils.bytesToHex(s.getBytes(StandardCharsets.UTF_8)));

        truncateByUtf8ByteLength0(s, 11, "11喵aa汪");
        truncateByUtf8ByteLength0(s, 10, "11喵aa汪");
        truncateByUtf8ByteLength0(s, 9, "11喵aa");
        truncateByUtf8ByteLength0(s, 8, "11喵aa");
        truncateByUtf8ByteLength0(s, 7, "11喵aa");
        truncateByUtf8ByteLength0(s, 6, "11喵a");
        truncateByUtf8ByteLength0(s, 5, "11喵");
        truncateByUtf8ByteLength0(s, 4, "11");
        truncateByUtf8ByteLength0(s, 3, "11");
        truncateByUtf8ByteLength0(s, 2, "11");
        truncateByUtf8ByteLength0(s, 1, "1");
        truncateByUtf8ByteLength0(s, 0, "");
        truncateByUtf8ByteLength0(s, -1, "");
        truncateByUtf8ByteLength0(null, 1, null);

    }

    private void truncateByUtf8ByteLength0(String string, int toLength, String expected) {
        Assert.assertEquals(expected, StringUtils.truncateByUtf8ByteLength(string, toLength));
    }

}
