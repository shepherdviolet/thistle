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

import java.util.HashMap;
import java.util.Map;

/**
 * 基本类型转换工具
 *
 * @author S.Violet
 */
public class PrimitiveUtils {

    private static final Map<Class<?>, Class<?>> primitiveWrappers = new HashMap<>();

    static {
        primitiveWrappers.put(boolean.class, Boolean.class);
        primitiveWrappers.put(char.class, Character.class);
        primitiveWrappers.put(byte.class, Byte.class);
        primitiveWrappers.put(short.class, Short.class);
        primitiveWrappers.put(int.class, Integer.class);
        primitiveWrappers.put(long.class, Long.class);
        primitiveWrappers.put(float.class, Float.class);
        primitiveWrappers.put(double.class, Double.class);
        primitiveWrappers.put(void.class, Void.class);
    }

    public static Class<?> toWrapperType(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        if (!clazz.isPrimitive()){
            return clazz;
        }
        Class<?> result = primitiveWrappers.get(clazz);
        return result != null ? result : clazz;
    }

}
