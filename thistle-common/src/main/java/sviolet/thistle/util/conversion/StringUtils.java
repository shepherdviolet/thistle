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
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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

    /**
     * Excel文件数值进度丢失特征: 小数第三位第四位第五位为000或999
     */
    private static Pattern resolveExcelPrecisionProblemPattern = Pattern.compile("^(-?\\d+\\.\\d{2})(000|999)(\\d)*$");

    /**
     * [特殊]通常用于处理Excel文件数据,
     * 因为Excel的数值有可能存在进度丢失的问题, 例如1.67变成1.669999999...3, 本方法专门识别这种情况, 并纠正精度丢失.
     * @param string excel中读取的数值, 例如1.669999999...3
     * @return 纠正后的数值, 例如1.67
     */
    public static String resolveExcelPrecisionProblem(String string){
        if (string == null || !resolveExcelPrecisionProblemPattern.matcher(string).matches()){
            return string;
        }
        return new BigDecimal(string).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     * <p>使用指定字符分割字符串, 忽略空白项, 去除头尾空白, 返回List</p>
     *
     * <p>
     * 例如:<br>
     * splitAndTrim(" abc, def, ,ghj,,klm ", ",")<br>
     * 结果为:<br>
     * 'abc' 'def' 'ghj', 'klm'<br>
     * </p>
     *
     * @param string 被切割的字符串
     * @param splitRegex 切割的字符
     * @return Not Null
     */
    public static List<String> splitAndTrim(String string, String splitRegex) {
        if (string == null) {
            return new ArrayList<>(0);
        }
        String[] array = string.split(splitRegex);
        List<String> result = new ArrayList<>(array.length);
        for (String item : array) {
            if (item == null || item.length() <= 0) {
                continue;
            }
            String trimmed = item.trim();
            if (trimmed.length() <= 0) {
                continue;
            }
            result.add(trimmed);
        }
        return result;
    }

    /**
     * 裁切字符串, 使得它的UTF-8编码字节长度小于指定值 (尾部裁切)
     *
     * @param string 字符串
     * @param toLength 指定字节长度
     * @return UTF-8编码字节长度不大于toLength的字符串 (尾部裁切)
     */
    public static String truncateByUtf8ByteLength(String string, int toLength){
        if (string == null) {
            return null;
        }
        if (toLength <= 0) {
            return "";
        }
        // Assume 4 bytes per char
        if ((string.length() << 2) <= toLength) {
            return string;
        }
        // To UTF-8 byte array
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= toLength) {
            return string;
        }
        // The char after last one
        int i = toLength;
        int flag = bytes[i] & 0b11000000;
        if (flag != 0b10000000) {
            // The char after last one is [0xxxxxxx : One byte char] or [11xxxxxx : Head of multiple byte char]
            return new String(bytes, 0, toLength, StandardCharsets.UTF_8);
        }
        // The char after last one is [10xxxxxx : Body of multiple byte char] looking for the head
        while (--i > 0) {
            if ((bytes[i] & 0b11000000) == 0b11000000) {
                // Meet [11xxxxxx : Head of multiple byte char] (0xxxxxxx is impossible here)
                return new String(bytes, 0, i, StandardCharsets.UTF_8);
            }
        }
        return "";
    }

}
