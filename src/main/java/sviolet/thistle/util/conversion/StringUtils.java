/*
 * Copyright (C) 2015-2017 S.Violet
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串工具
 * @author S.Violet
 */
public class StringUtils {

    private static final String DECODE_DEC_UNICODE_REGEXP = "&#\\d*;";

    /**
     * 将字符串指定位置变为大写(字母)
     * @param src 源字符串
     * @param positions 变为大写的位置[0, length)
     * @return 变换后的字符串
     */
    public static String toUpperCase(String src, int... positions){
        if (src == null) {
            return null;
        }
        char[] chars = src.toCharArray();
        for (int position : positions){
            if(position < chars.length && position > -1){
                chars[position] -= (chars[position] > 96 && chars[position] < 123) ? 32 : 0;
            }
        }
        return String.valueOf(chars);
    }

    /**
     * 将字符串指定位置变为小写(字母)
     * @param src 源字符串
     * @param positions 变为小写的位置[0, length)
     * @return 变换后的字符串
     */
    public static String toLowerCase(String src, int... positions){
        if (src == null) {
            return null;
        }
        char[] chars = src.toCharArray();
        for (int position : positions){
            if(position < chars.length && position > -1){
                chars[position] += (chars[position] > 64 && chars[position] < 91) ? 32 : 0;
            }
        }
        return String.valueOf(chars);
    }

    /**
     * 将字符串中的数字字母标点转为全角
     * @param src 原字符串
     * @return 全角字符串
     */
    public static String toSBCCase(String src) {
        if (src == null) {
            return null;
        }
        char[] charArray = src.toCharArray();
        for (int i = 0; i< charArray.length; i++) {
            if (charArray[i] == 12288) {
                charArray[i] = (char) 32;
            }else if (charArray[i] > 65280 && charArray[i] < 65375) {
                charArray[i] = (char) (charArray[i] - 65248);
            }
        }
        return new String(charArray);
    }

    /**
     * 把异常转为String信息
     */
    public static String throwableToString(Throwable throwable) {
        if (throwable == null){
            return null;
        }
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        throwable.printStackTrace(printWriter);
        printWriter.close();
        return writer.toString();
    }

    /**
     * <p>将包含十进制Unicode编码的String, 转为普通编码的String</p>
     *
     * <p>例如:"马特&#8226;达蒙"转为"马特•达蒙"</p>
     */
    public static String decodeDecUnicode(String string){
        if (string == null){
            return null;
        }
        Matcher matcher = Pattern.compile(DECODE_DEC_UNICODE_REGEXP).matcher(string);
        StringBuffer stringBuffer = new StringBuffer();
        while (matcher.find()) {
            String s = matcher.group(0);
            s = s.replaceAll("(&#)|;", "");
            char c = (char) Integer.parseInt(s);
            matcher.appendReplacement(stringBuffer, Character.toString(c));
        }
        matcher.appendTail(stringBuffer);
        return stringBuffer.toString();
    }

    /**
     * 检查string中是否包含keywords
     * @param string string
     * @param keywords keywords
     * @return true:包含
     */
    public static boolean contains(String string, String keywords){
        if (string == null){
            return false;
        }
        return string.contains(keywords);
    }

}
