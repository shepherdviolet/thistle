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

import sviolet.thistle.model.common.SysPropFirstProperties;

import java.util.Properties;

/**
 * ThistleSpi 工具
 *
 * @author S.Violet
 */
public class ThistleSpiUtils {

    private static final String LOG_PREFIX = " ThistleSpi Utils | ";

    private static final SpiLogger logger = ThistleSpi.getLoader().getLogger();

    /**
     * 将Service/Plugin实现类构造参数的Properties对象包装成强化版, 先从系统参数(启动参数)中获取, 若获取失败, 则从构造参数
     * 引用的配置文件中获取, 还是获取失败则返回默认值. 提供数字类型的安全获取(解析失败会打印日志并返回默认值).
     * @param properties Service/Plugin实现类构造参数的Properties对象
     * @return 强化版
     */
    public static SysPropFirstProperties wrapPropertiesBySysProp(Properties properties){
        return new SysPropFirstProperties(properties, SYS_PROP_FIRST_PROPERTIES_LOG_HANDLER);
    }

    private static SysPropFirstProperties.LogHandler SYS_PROP_FIRST_PROPERTIES_LOG_HANDLER = new SysPropFirstProperties.LogHandler() {

        /**
         * 当系统参数(启动参数)存在, 并覆盖了内置参数时
         * @param sysPropKey 系统参数key
         * @param sysPropValue 系统参数的值
         * @param propKey 内置参数的key
         * @param propValue 内置参数的值
         * @param defValue 预设的默认值
         * @param properties 内置的Properties对象(不是系统参数)
         */
        @Override
        public void onOverwrittenBySysProp(String sysPropKey, String sysPropValue, String propKey, String propValue, Object defValue, Properties properties) {
            logger.print("?" + LOG_PREFIX + "Constructor parameter '" + propKey + "' is overwritten by system property '-D" + sysPropKey + "=" + sysPropValue + "', value " + propValue + " -> " + sysPropValue);
        }

        /**
         * 当使用默认值时
         * @param sysPropKey 系统参数key
         * @param sysPropValue 系统参数的值
         * @param propKey 内置参数的key
         * @param propValue 内置参数的值
         * @param defValue 预设的默认值
         * @param properties 内置的Properties对象(不是系统参数)
         */
        @Override
        public void onUsingDefault(String sysPropKey, String sysPropValue, String propKey, String propValue, Object defValue, Properties properties) {
            logger.print("?" + LOG_PREFIX + "Constructor parameter '" + propKey + "' using default value '" + defValue + "'");
        }

        /**
         * 当value从String转为指定类型时发生异常. 你可以在这里打印日志(get方法会返回默认值)
         * @param parsingSysProp true:解析系统参数(启动参数)发生错误 false:解析内置的Properties参数发生错误
         * @param key 发生错误的key
         * @param value 发生错误的value
         * @param toType 尝试将value转换为该类型时出错
         * @param defValue 尝试使用的默认值
         * @param properties 内置的Properties对象(不是系统参数)
         * @param e 异常
         */
        @Override
        public void onParseException(boolean parsingSysProp, String key, String value, Class<?> toType, Object defValue, Properties properties, Exception e) {
            if (parsingSysProp) {
                logger.print("?" + LOG_PREFIX + "WARNING: Error while parsing system property '-D" + key + "=" + value + "' to " + toType.getName() + ", try using '" + defValue + "'", e);
            } else {
                logger.print("?" + LOG_PREFIX + "WARNING: Error while parsing constructor parameter '" + value + "' to " + toType.getName() + ", using default '" + defValue + "'" +
                        ", parameter key:" + key + ", definite in " + properties.get(ThistleSpi.PROPERTIES_URL), e);
            }
        }
    };

}
