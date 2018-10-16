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

package sviolet.thistle.model.common;

import java.util.Map;

/**
 * 按顺序从一系列Map中取值, 取到为止
 *
 * @author S.Violet
 */
public class PollGettingMap {

    private Map[] maps;

    /**
     * 内置数个Map, 按指定顺序轮询获取参数, 第一个优先级最高
     * @param leadingMap 第一个Map, 优先级最高
     * @param followingMaps 其他的Map
     */
    public PollGettingMap(Map leadingMap, Map... followingMaps) {
        if (leadingMap == null) {
            throw new NullPointerException("leadingMap is null");
        }
        if (followingMaps != null) {
            for (Map map : followingMaps) {
                if (map == null) {
                    throw new NullPointerException("one of followingMaps is null");
                }
            }
            this.maps = new Map[followingMaps.length + 1];
            this.maps[0] = leadingMap;
            System.arraycopy(followingMaps, 0, this.maps, 1, followingMaps.length);
        } else {
            this.maps = new Map[]{leadingMap};
        }
    }

    /**
     * 按顺序从一系列Map中取值, 取到为止
     * @param key key
     * @param def 默认值
     * @return value
     */
    public Object get(Object key, Object def) {
        Object value;
        for (Map map : maps) {
            if ((value = map.get(key)) != null) {
                return value;
            }
        }
        return def;
    }

    /**
     * 按顺序从一系列Map中取String, 取到为止, 若类型不为String会转换成String返回
     * @param key key
     * @param def 默认值
     * @return value
     */
    public String getString(Object key, String def) {
        Object value = get(key, def);
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }

    /**
     * 按顺序从一系列Map中取int, 取到为止, 若类型不为int会转换成int返回, 转换失败抛出异常
     * @param key key
     * @param def 默认值
     * @return value
     */
    public int getInt(Object key, int def) throws ParseException {
        Object value = get(key, def);
        if (value instanceof Integer) {
            return (int) value;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            throw new ParseException("Error while parsing " + value + " to int", e);
        }
    }

    /**
     * 按顺序从一系列Map中取int, 取到为止, 若类型不为int会转换成int返回, 转换失败返回默认值
     * @param key key
     * @param def 默认值
     * @return value
     */
    public int safeGetInt(Object key, int def) {
        Object value = get(key, def);
        if (value instanceof Integer) {
            return (int) value;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * 按顺序从一系列Map中取long, 取到为止, 若类型不为long会转换成long返回, 转换失败抛出异常
     * @param key key
     * @param def 默认值
     * @return value
     */
    public long getLong(Object key, long def) throws ParseException {
        Object value = get(key, def);
        if (value instanceof Long) {
            return (long) value;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            throw new ParseException("Error while parsing " + value + " to long", e);
        }
    }

    /**
     * 按顺序从一系列Map中取long, 取到为止, 若类型不为long会转换成long返回, 转换失败返回默认值
     * @param key key
     * @param def 默认值
     * @return value
     */
    public long safeGetLong(Object key, long def) {
        Object value = get(key, def);
        if (value instanceof Long) {
            return (long) value;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * 按顺序从一系列Map中取float, 取到为止, 若类型不为float会转换成float返回, 转换失败抛出异常
     * @param key key
     * @param def 默认值
     * @return value
     */
    public float getFloat(Object key, float def) throws ParseException {
        Object value = get(key, def);
        if (value instanceof Float) {
            return (float) value;
        }
        try {
            return Float.parseFloat(String.valueOf(value));
        } catch (Exception e) {
            throw new ParseException("Error while parsing " + value + " to float", e);
        }
    }

    /**
     * 按顺序从一系列Map中取float, 取到为止, 若类型不为float会转换成float返回, 转换失败返回默认值
     * @param key key
     * @param def 默认值
     * @return value
     */
    public float safeGetFloat(Object key, float def) {
        Object value = get(key, def);
        if (value instanceof Float) {
            return (float) value;
        }
        try {
            return Float.parseFloat(String.valueOf(value));
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * 按顺序从一系列Map中取double, 取到为止, 若类型不为double会转换成double返回, 转换失败抛出异常
     * @param key key
     * @param def 默认值
     * @return value
     */
    public double getDouble(Object key, double def) throws ParseException {
        Object value = get(key, def);
        if (value instanceof Double) {
            return (double) value;
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            throw new ParseException("Error while parsing " + value + " to double", e);
        }
    }

    /**
     * 按顺序从一系列Map中取double, 取到为止, 若类型不为double会转换成double返回, 转换失败返回默认值
     * @param key key
     * @param def 默认值
     * @return value
     */
    public double safeGetDouble(Object key, double def) {
        Object value = get(key, def);
        if (value instanceof Double) {
            return (double) value;
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * 类型转换失败
     */
    public static class ParseException extends Exception {

        public ParseException(String message) {
            super(message);
        }

        public ParseException(String message, Throwable cause) {
            super(message, cause);
        }

    }

}
