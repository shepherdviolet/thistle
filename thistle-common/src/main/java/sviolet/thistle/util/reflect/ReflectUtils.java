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

package sviolet.thistle.util.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 反射工具
 *
 * @author S.Violet
 */
public class ReflectUtils {

    /**
     * 获得一个类的泛型类型
     * @param clazz 类
     * @return 泛型类型
     */
    public static List<Class> getActualTypes(Class clazz){
        List<Class> result = new ArrayList<>();
        Type type = clazz.getGenericSuperclass();
        if (ParameterizedType.class.isAssignableFrom(type.getClass())) {
            for (Type actualType : ((ParameterizedType) type).getActualTypeArguments()) {
                result.add((Class)actualType);
            }
        }
        return result;
    }

}
