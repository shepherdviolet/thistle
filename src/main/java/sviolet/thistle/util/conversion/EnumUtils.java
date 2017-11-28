/*
 * Copyright (C) 2015-2016 S.Violet
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

/**
 * <p>ENUM工具</p>
 *
 * Created by S.Violet on 2016/7/13.
 */

public class EnumUtils {

    /**
     * String转enum
     * @param type enum类型
     * @param value string值
     * @return enum
     */
    public static <T extends Enum<T>> T toEnum(Class<T> type, String value){
        if (type == null){
            throw new RuntimeException("[EnumUtils]type is null");
        }
        if (value == null || value.length() <= 0){
            return null;
        }
        return Enum.valueOf(type, value);
    }

}
