/*
 * Copyright (C) 2015-2018 S.Violet
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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 *
 * @author S.Violet
 */
public class SimpleKeyValueEncoder {

    private static final char SPLIT = ',';
    private static final char EQUAL = '=';
    private static final char ESCAPE = '\\';
    private static final char SPACE = ' ';
    private static final char TAB = '\t';

    private static final String ESCAPE_NULL = "\\0";
    private static final String ESCAPE_SPACE = "\\s";
    private static final String ESCAPE_TAB = "\\t";

    private static final char ESCAPE_VALUE_NULL = '0';
    private static final char ESCAPE_VALUE_SPACE = 's';
    private static final char ESCAPE_VALUE_TAB = 't';

    public static String encode(Map<String, String> keyValue){
        return encode(keyValue, 0);
    }

    public static String encode(Map<String, String> keyValue, int spaceInterval){
        if (keyValue == null || keyValue.size() <= 0) {
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        for (Map.Entry<String, String> entry : keyValue.entrySet()) {
            if (i++ > 0) {
                stringBuilder.append(SPLIT);
                for (int si = 0 ; si < spaceInterval ; si++) {
                    stringBuilder.append(SPACE);
                }
            }
            append(stringBuilder, entry.getKey());
            stringBuilder.append(EQUAL);
            append(stringBuilder, entry.getValue());
        }

        return stringBuilder.toString();
    }

    private static void append(StringBuilder stringBuilder, String str){
        if (str == null) {
            stringBuilder.append(ESCAPE_NULL);
            return;
        }

        char[] chars = str.toCharArray();
        int start = 0;
        for (int i = 0 ; i < chars.length ; i++) {
            char c = chars[i];
            if (c == SPLIT ||
                    c == EQUAL ||
                    c == ESCAPE){
                stringBuilder.append(chars, start, i - start);
                stringBuilder.append(ESCAPE);
                stringBuilder.append(c);
                start = i + 1;
            } else if (c == SPACE) {
                stringBuilder.append(chars, start, i - start);
                stringBuilder.append(ESCAPE_SPACE);
                start = i + 1;
            } else if (c == TAB) {
                stringBuilder.append(chars, start, i - start);
                stringBuilder.append(ESCAPE_TAB);
                start = i + 1;
            }
        }

        if (start < chars.length) {
            stringBuilder.append(chars, start, chars.length - start);
        }
    }

    public static Map<String, String> decode(String encoded) throws DecodeException {
        if (encoded == null || encoded.length() <= 0) {
            return new LinkedHashMap<>(0);
        }

        Map<String, String> resultMap = new LinkedHashMap<>();
        char[] chars = encoded.toCharArray();

        Visitor visitor = new Visitor();
        boolean escaping = false;
        int start = 0;

        for (int i = 0 ; i < chars.length ; i++) {
            char c = chars[i];
            if (escaping) {
                //handle escape
                visitor.onEscape(resultMap, chars, start, i, c);
                start = i + 1;
                escaping = false;
            } else {
                if (c == ESCAPE) {
                    //find escape
                    escaping = true;
                }else if (c == SPLIT) {
                    //element finish
                    visitor.onElementFinish(resultMap, chars, start, i);
                    start = i + 1;
                } else if (c == EQUAL) {
                    //find equal
                    visitor.onEqual(resultMap, chars, start, i);
                    start = i + 1;
                }
            }
        }

        //finish all
        visitor.onElementFinish(resultMap, chars, start, chars.length);

        return resultMap;
    }

    private static class Visitor {

        //true: decoding key, false: decoding value
        private boolean keyDecoding = true;

        private StringBuilder keyBuilder = new StringBuilder();
        private boolean keyNull = false;
        private int keyStart = Integer.MAX_VALUE;
        private int keyEnd = Integer.MIN_VALUE;

        private StringBuilder valueBuilder = new StringBuilder();
        private boolean valueNull = false;
        private int valueStart = Integer.MAX_VALUE;
        private int valueEnd = Integer.MIN_VALUE;

        /**
         * when we find a char after escape \
         */
        private void onEscape(Map<String, String> map, char[] chars, int startIndex, int currentIndex, char c) throws DecodeException {
            //append previous chars (skip previous escape char \)
            appendPrevious(chars, startIndex, currentIndex - 1);
            //append escape char
            if (c == SPLIT ||
                    c == EQUAL ||
                    c == ESCAPE){
                //normal escape
                appendChar(chars, c);
            } else if (c == ESCAPE_VALUE_SPACE) {
                //space escape
                appendChar(chars, SPACE);
                //record position of valid space or tab, avoid to trimmed
                recordStartEnd();
            } else if (c == ESCAPE_VALUE_TAB) {
                //tab escape
                appendChar(chars, TAB);
                //record position of valid space or tab, avoid to trimmed
                recordStartEnd();
            } else if (c == ESCAPE_VALUE_NULL) {
                //null escape
                if (keyDecoding) {
                    //space escape can only be used alone
                    if (keyBuilder.length() > 0 || keyNull) {
                        throw new DecodeException("Invalid data, '\\0' can only be used alone, example \\0=abc or abc=\\0, data:" + new String(chars));
                    }
                    //set key null
                    keyNull = true;
                } else {
                    //space escape can only be used alone
                    if (valueBuilder.length() > 0 || valueNull) {
                        throw new DecodeException("Invalid data, '\\0' can only be used alone, example \\0=abc or abc=\\0, data:" + new String(chars));
                    }
                    //set value null
                    valueNull = true;
                }
            } else {
                throw new DecodeException("Invalid data, undefined escape \\" + c + ", data:" + new String(chars));
            }
        }

        /**
         * when we find equal =
         */
        private void onEqual(Map<String, String> map, char[] chars, int startIndex, int currentIndex) throws DecodeException {
            //check state
            if (!keyDecoding) {
                throw new DecodeException("Invalid data, element has two '=', use escape char '\\=' instead, problem key:" + keyBuilder.toString() + ", data:" + new String(chars));
            }
            //append previous chars
            appendPrevious(chars, startIndex, currentIndex);
            //start to decoding value
            keyDecoding = false;
        }

        /**
         * when element finish
         */
        private void onElementFinish(Map<String, String> map, char[] chars, int startIndex, int currentIndex) throws DecodeException {
            //check state
            if (keyDecoding) {
                throw new DecodeException("Invalid data, element has no value, problem key:" + keyBuilder.toString() + ", data:" + new String(chars));
            }
            //append previous chars
            appendPrevious(chars, startIndex, currentIndex);
            //get and trim key/value
            String key = keyNull ? null : trim(keyBuilder.toString(), keyStart, keyEnd);
            String value = valueNull ? null : trim(valueBuilder.toString(), valueStart, valueEnd);
            //put map
            map.put(key, value);
            //reset
            reset();
        }

        /**
         * remove the leading and trailing spaces, and it will not remove valid spaces and tabs (record by keyStart/keyEnd/valueStart/valueEnd)
         */
        private String trim(String value, int start, int end){
            char[] chars = value.toCharArray();
            int from = 0;
            int to = chars.length;
            while (from < to && chars[from] <= SPACE && from < start) {
                from++;
            }
            while (to > from && chars[to - 1] <= SPACE) {
                to--;
            }
            return from <= 0 && to >= chars.length ? value : value.substring(from, to);
        }

        /**
         * reset context
         */
        private void reset() {
            keyDecoding = true;
            keyBuilder.setLength(0);
            keyNull = false;
            keyStart = Integer.MAX_VALUE;
            keyEnd = Integer.MIN_VALUE;
            valueBuilder.setLength(0);
            valueNull = false;
            valueStart = Integer.MAX_VALUE;
            valueEnd = Integer.MIN_VALUE;
        }

        /**
         * append from startIndex to currentIndex
         */
        private void appendPrevious(char[] chars, int startIndex, int currentIndex) throws DecodeException {
            if (startIndex < currentIndex) {
                if (keyDecoding) {
                    //space escape can only be used alone
                    if (keyNull) {
                        throw new DecodeException("Invalid data, '\\0' can only be used alone, example \\0=abc or abc=\\0, data:" + new String(chars));
                    }
                    keyBuilder.append(chars, startIndex, currentIndex - startIndex);
                } else {
                    //space escape can only be used alone
                    if (valueNull) {
                        throw new DecodeException("Invalid data, '\\0' can only be used alone, example \\0=abc or abc=\\0, data:" + new String(chars));
                    }
                    valueBuilder.append(chars, startIndex, currentIndex - startIndex);
                }
            }
        }

        /**
         * append char
         */
        private void appendChar(char[] chars, char c) throws DecodeException {
            if (keyDecoding) {
                //space escape can only be used alone
                if (keyNull) {
                    throw new DecodeException("Invalid data, '\\0' can only be used alone, example \\0=abc or abc=\\0, data:" + new String(chars));
                }
                keyBuilder.append(c);
            } else {
                //space escape can only be used alone
                if (valueNull) {
                    throw new DecodeException("Invalid data, '\\0' can only be used alone, example \\0=abc or abc=\\0, data:" + new String(chars));
                }
                valueBuilder.append(c);
            }
        }

        /**
         * record position of valid space or tab, avoid to trimmed
         */
        private void recordStartEnd(){
            if (keyDecoding) {
                int currPosition = keyBuilder.length();
                if (currPosition < keyStart) {
                    keyStart = currPosition;
                }
                if (currPosition > keyEnd) {
                    keyEnd = currPosition;
                }
            } else {
                int currPosition = valueBuilder.length();
                if (currPosition < valueStart) {
                    valueStart = currPosition;
                }
                if (currPosition > valueEnd) {
                    valueEnd = currPosition;
                }
            }
        }

    }

    public static class DecodeException extends Exception {

        public DecodeException(String message) {
            super(message);
        }

        public DecodeException(String message, Throwable cause) {
            super(message, cause);
        }

    }

}
