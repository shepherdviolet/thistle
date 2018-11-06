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

import sviolet.thistle.util.conversion.DateTimeUtils;
import sviolet.thistle.util.judge.CheckUtils;

import java.util.HashSet;
import java.util.Set;

class Constants {

    // ************************************************************************************************
    // 启动参数

    //日志打印级别(error/debug/verbose, 默认debug)
    static final String STARTUP_PROP_LOG_LV = "thistle.spi.loglv";
    //缓存开关(默认true)
    static final String STARTUP_PROP_CACHE_ENABLED = "thistle.spi.cache";
    //强制禁用配置文件(根据文件hash值)
    static final String STARTUP_PROP_FILE_EXCLUSION = "thistle.spi.file.exclusion";

    //启动参数指定服务
    static final String STARTUP_PROP_SERVICE_APPLY_PREFIX = "thistle.spi.apply.";

    //启动参数忽略插件
    static final String STARTUP_PROP_PLUGIN_IGNORE_PREFIX = "thistle.spi.ignore.";

    // ************************************************************************************************
    // 路径

    //默认配置路径
    static final String CONFIG_PATH_DEFAULT = "META-INF/thistle-spi/";
    //[固定]自定义日志打印器配置路径
    static final String CONFIG_PATH_LOGGER = "META-INF/thistle-spi-logger/";
    //[固定]构造参数引用配置文件路径(相对路径)
    static final String CONFIG_PATH_PARAMETER = "parameter/";

    //服务配置文件名
    static final String CONFIG_FILE_SERVICE = "service.properties";
    //服务指定文件名
    static final String CONFIG_FILE_SERVICE_APPLY = "service-apply.properties";

    //插件配置文件名
    static final String CONFIG_FILE_PLUGIN = "plugin.properties";
    //插件忽略文件名
    static final String CONFIG_FILE_PLUGIN_IGNORE = "plugin-ignore.properties";

    // ************************************************************************************************
    // 日志

    //日志前缀
    static final String LOG_PREFIX = " ThistleSpi | ";
    static final String LOG_PREFIX_LOADER = " ThistleSpi Loader | ";

    //INFO级别的日志最大输出行数
    static final int MAX_INFO_LOG_LINES = 10;

    //日志级别
    static final int ERROR = 0;
    static final int INFO = 1;
    static final int DEBUG = 2;

    //日志级别(error/info/debug), 默认info
    static final int LOG_LV;

    // ************************************************************************************************
    // 其他

    //是否启用缓存(默认true)
    static final boolean CACHE_ENABLED;

    //被强制禁用的配置文件的hash
    static final Set<String> FILE_EXCLUSION_SET;

    // ************************************************************************************************
    // 初始化

    static {
        LOG_LV = initLogLevel();
        CACHE_ENABLED = initCacheEnabled();
        FILE_EXCLUSION_SET = initFileExclusion();
    }

    private static boolean initCacheEnabled() {
        boolean enabled = "true".equals(System.getProperty(STARTUP_PROP_CACHE_ENABLED, "true"));
        if (LOG_LV >= INFO && !enabled) {
            System.out.println(DateTimeUtils.getDateTime() + " ?" + LOG_PREFIX + "Loader cache force disabled by -D" + STARTUP_PROP_CACHE_ENABLED + "=false");
        }
        return enabled;
    }

    private static int initLogLevel() {
        switch (System.getProperty(STARTUP_PROP_LOG_LV, "info").toLowerCase()) {
            case "error":
                return ERROR;
            case "debug":
                return DEBUG;
            case "info":
            default:
                return INFO;
        }
    }

    private static Set<String> initFileExclusion() {
        Set<String> fileExclusionSet = new HashSet<>();
        String fileExclusionStr = System.getProperty(STARTUP_PROP_FILE_EXCLUSION, null);
        if (!CheckUtils.isEmptyOrBlank(fileExclusionStr)) {
            String[] array = fileExclusionStr.split(",");
            for (String item : array) {
                if (!CheckUtils.isEmptyOrBlank(item)) {
                    fileExclusionSet.add(item.trim());
                    if (LOG_LV >= INFO) {
                        System.out.println(DateTimeUtils.getDateTime() + " ?" + LOG_PREFIX + "Config file with hash '" + item + "' will be excluded, by -D" + STARTUP_PROP_FILE_EXCLUSION + "=" + fileExclusionStr);
                    }
                }
            }
        }
        return fileExclusionSet;
    }

}
