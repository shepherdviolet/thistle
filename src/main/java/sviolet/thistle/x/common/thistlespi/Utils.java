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

package sviolet.thistle.x.common.thistlespi;

import static sviolet.thistle.x.common.thistlespi.ThistleSpi.LOG_PREFIX;

class Utils {

    private static final Object[] NULL_OBJ_ARRAY = new Object[]{null};

    /**
     * 类型实例化, 可包含一个String构造参数
     */
    static Object newInstance(Class<?> clazz, String arg) throws Exception {
        if (arg == null) {
            try {
                return clazz.getConstructor().newInstance();
            } catch (NoSuchMethodException e) {
                try {
                    return clazz.getConstructor(String.class).newInstance(NULL_OBJ_ARRAY);
                } catch (NoSuchMethodException e2) {
                    throw e;
                }
            }
        } else {
            try {
                return clazz.getConstructor(String.class).newInstance(arg);
            } catch (NoSuchMethodException e) {
                try {
                    clazz.getConstructor().newInstance();
                } catch (NoSuchMethodException e2) {
                    throw e;
                }
            }
        }
        return null;
    }

    /**
     * 将参数值解析为实现信息(实现类和构造参数)
     */
    static Implementation parseImplementation(String propValue, boolean fromConfig, SpiLogger logger, int loaderId, String propKey, String propUrl){
        int argStart = propValue.indexOf("(");
        //value第一个字符就是(, 非法
        if (argStart == 0) {
            if (fromConfig) {
                RuntimeException e = new RuntimeException("ThistleSpi: Illegal config, value of " + propKey + " starts with '(', config:" + propUrl);
                logger.print(loaderId + LOG_PREFIX + "ERROR: Illegal config, value of " + propKey + " starts with '(', config:" + propUrl, e);
                throw e;
            } else {
                RuntimeException e = new RuntimeException("ThistleSpi: Illegal jvm arg, value of -D" + propKey + " starts with '('");
                logger.print(loaderId + LOG_PREFIX + "ERROR: Illegal jvm arg, value of -D" + propKey + " starts with '('", e);
                throw e;
            }
        }
        //存在(字符, 尝试截取构造参数
        if (argStart > 0) {
            //value最后一个字符不是), 非法
            if (')' != propValue.charAt(propValue.length() - 1)) {
                if (fromConfig) {
                    RuntimeException e = new RuntimeException("ThistleSpi: Illegal config, value of " + propKey + " has '(' but no ')' at last, config:" + propUrl);
                    logger.print(loaderId + LOG_PREFIX + "ERROR: Illegal config, value of " + propKey + " has '(' but no ')' at last, config:" + propUrl, e);
                    throw e;
                } else {
                    RuntimeException e = new RuntimeException("ThistleSpi: Illegal jvm arg, value of -D" + propKey + " has '(' but no ')' at last");
                    logger.print(loaderId + LOG_PREFIX + "ERROR: Illegal jvm arg, value of -D" + propKey + " has '(' but no ')' at last", e);
                    throw e;
                }
            }
            return new Implementation(
                    propValue.substring(argStart + 1, propValue.length() - 1),
                    propValue.substring(0, argStart).trim()
            );
        }
        return new Implementation(propValue, null);
    }

    static class Implementation {
        String implement;
        String arg;
        Implementation(String implement, String arg) {
            this.implement = implement;
            this.arg = arg;
        }
    }

}
