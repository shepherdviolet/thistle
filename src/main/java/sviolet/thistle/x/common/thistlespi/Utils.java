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

import sviolet.thistle.util.judge.CheckUtils;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import static sviolet.thistle.x.common.thistlespi.ThistleSpi.*;

class Utils {

    /**
     * 类型实例化, 可包含一个String构造参数
     * @param clazz 需要实例化的类
     * @param arg 构造参数, null / String参数 / Properties文件名
     * @param classLoader (用于加载构造参数应用的配置文件)类加载器
     * @param configPath (用于加载构造参数应用的配置文件)配置文件根路径
     * @param configUrl (用于加载构造参数应用的配置文件)服务/插件的配置文件URL
     * @param logger (日志相关)日志打印器
     * @param loaderId (日志相关)加载器ID
     */
    static Object newInstance(Class<?> clazz, String arg, ClassLoader classLoader, String configPath, URL configUrl, SpiLogger logger, int loaderId) throws Exception {
        Constructor[] constructors = clazz.getConstructors();
        if (constructors.length != 1) {
            throw new RuntimeException("ThistleSpi: Illegal Service/Plugin implementation " + clazz.getName() +
                    ", the implementation must have one and only one public constructor, now it has " + constructors.length +
                    ", definitions:" + configUrl);
        }
        Constructor constructor = constructors[0];
        Class[] paramTypes = constructor.getParameterTypes();
        if (paramTypes.length > 1) {
            throw new RuntimeException("ThistleSpi: Illegal Service/Plugin implementation " + clazz.getName() +
                    ", the constructor can only have 0 or 1 parameter, now it has " + paramTypes.length +
                    ", definitions:" + configUrl);
        } else if (paramTypes.length == 0) {
            return constructor.newInstance();
        } else if (String.class.isAssignableFrom(paramTypes[0])){
            //paramType length == 1 and is instance of String
            return constructor.newInstance(arg);
        } else if (Properties.class.isAssignableFrom(paramTypes[0])) {
            //paramType length == 1 and is instance of Properties
            if (CheckUtils.isEmptyOrBlank(arg)) {
                //input empty Properties
                if (LOG_LV >= DEBUG) {
                    logger.print(loaderId + LOG_PREFIX_LOADER + "The parameter type of constructor is java.util.Properties in " +
                            clazz.getName() + ", But no constructor arg defined in definitions:" + configPath);
                }
                return constructor.newInstance(new Properties());
            }
            return newInstanceForPropConstructor(clazz, arg, classLoader, configPath, configUrl, logger, loaderId, constructor);
        } else {
            throw new RuntimeException("ThistleSpi: Illegal Service/Plugin implementation " + clazz.getName() +
                    ", the parameter type of constructor must be java.lang.String or java.util.Properties, now it it " +
                    paramTypes[0].getName() + ", definitions:" + configUrl);
        }
    }

    /**
     * @param clazz 需要实例化的类
     * @param arg 构造参数, null / String参数 / Properties文件名
     * @param classLoader (用于加载构造参数应用的配置文件)类加载器
     * @param configPath (用于加载构造参数应用的配置文件)配置文件根路径
     * @param configUrl (用于加载构造参数应用的配置文件)服务/插件的配置文件URL
     * @param logger (日志相关)日志打印器
     * @param loaderId (日志相关)加载器ID
     * @param constructor 构造器
     */
    private static Object newInstanceForPropConstructor(Class<?> clazz, String arg, ClassLoader classLoader, String configPath, URL configUrl, SpiLogger logger, int loaderId, Constructor constructor) throws Exception {
        //find properties in the same package
        //properties file path, e.g. parameter/hello.properties
        String propertiesPath = CONFIG_PATH_PARAMETER + arg;
        //config file url prefix
        String urlPrefix = String.valueOf(configUrl);
        urlPrefix = urlPrefix.substring(0, urlPrefix.lastIndexOf('/') + 1);
        //find in classpath
        Enumeration<URL> urls;
        try {
            urls = classLoader.getResources(configPath + propertiesPath);
        } catch (Exception e) {
            throw new RuntimeException("ThistleSpi: Error while finding properties for constructor of " + clazz.getName() +
                    ", seeking " + propertiesPath + " in " + urlPrefix + ", definitions:" + configUrl, e);
        }
        if (urls == null || !urls.hasMoreElements()) {
            throw new RuntimeException("ThistleSpi: Illegal Service/Plugin definition, the parameter type of constructor is java.util.Properties in " +
                    clazz.getName() + ", so you have to define a properties file at " + urlPrefix + propertiesPath + ", definitions:" + configUrl);
        }
        //find url starts with specified prefix
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            if (String.valueOf(url).startsWith(urlPrefix)) {
                //found
                Properties properties;
                try {
                    properties = new Properties();
                    properties.load(url.openStream());
                } catch (Exception e) {
                    throw new RuntimeException("ThistleSpi: Error while loading properties for constructor of " + clazz.getName() +
                            ", properties path:" + url + ", definitions:" + configUrl, e);
                }
                //set url to properties
                properties.setProperty(PROPERTIES_URL, String.valueOf(url));
                if (LOG_LV >= INFO) {
                    logger.print(loaderId + LOG_PREFIX_LOADER + "Parameters load successfully: " + clazz.getName() + "(" + arg + ") params:" + properties + (LOG_LV >= DEBUG ? " definitions:" + url : ""));
                }
                return constructor.newInstance(properties);
            }
        }
        //not found
        throw new RuntimeException("ThistleSpi: Illegal definition, the param type of constructor is Properties in " + clazz.getName() +
                ", you have to define a properties file at " + urlPrefix + propertiesPath +
                ", NOTICE!!! MUST BE in the SAME project as the definition file (See https://github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/guide.md)" +
                ", definition file:" + configUrl);
    }

    /**
     * 将参数值解析为实现信息(实现类和构造参数)
     * @param propValue 参数值
     * @param fromConfig (日志相关)true:来自配置文件 false:来自启动参数
     * @param logger (日志相关)日志打印器
     * @param loaderId (日志相关)加载器ID
     * @param propKey (日志相关)参数键, 或启动参数名
     * @param configUrl (日志相关)配置文件URL, 可为空
     */
    static Implementation parseImplementation(String propValue, boolean fromConfig, SpiLogger logger, int loaderId, String propKey, URL configUrl){
        int argStart = propValue.indexOf("(");
        //value第一个字符就是(, 非法
        if (argStart == 0) {
            if (fromConfig) {
                RuntimeException e = new RuntimeException("ThistleSpi: Illegal config, value of " + propKey + " starts with '(', definitions:" + configUrl);
                logger.print(loaderId + LOG_PREFIX + "ERROR: Illegal config, value of " + propKey + " starts with '(', definitions:" + configUrl, e);
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
                    RuntimeException e = new RuntimeException("ThistleSpi: Illegal config, value of " + propKey + " has '(' but no ')' at last, definitions:" + configUrl);
                    logger.print(loaderId + LOG_PREFIX + "ERROR: Illegal config, value of " + propKey + " has '(' but no ')' at last, definitions:" + configUrl, e);
                    throw e;
                } else {
                    RuntimeException e = new RuntimeException("ThistleSpi: Illegal jvm arg, value of -D" + propKey + " has '(' but no ')' at last");
                    logger.print(loaderId + LOG_PREFIX + "ERROR: Illegal jvm arg, value of -D" + propKey + " has '(' but no ')' at last", e);
                    throw e;
                }
            }
            return new Implementation(
                    propValue.substring(0, argStart).trim(),
                    propValue.substring(argStart + 1, propValue.length() - 1)
            );
        }
        return new Implementation(propValue, null);
    }

    static class Implementation {
        //class name
        String implement;
        //constructor arg ( string arg or properties file name )
        String arg;
        Implementation(String implement, String arg) {
            this.implement = implement;
            this.arg = arg;
        }
    }

    /**
     * 检查配置文件是否被强制排除
     * @param propHash 配置文件的hash
     * @param logger (日志相关)日志打印器
     * @param loaderId (日志相关)加载器ID
     * @param configUrl (日志相关)被排除的配置文件的URL
     */
    static boolean checkFileExclusion(String propHash, SpiLogger logger, int loaderId, URL configUrl) {
        if (FILE_EXCLUSION.contains(propHash)) {
            if (LOG_LV >= INFO) {
                logger.print(loaderId + LOG_PREFIX + "!!! Exclude config " + configUrl + " by -D" + PROPERTY_FILE_EXCLUSION);
            }
            return true;
        } else {
            if (LOG_LV >= DEBUG) {
                logger.print(loaderId + LOG_PREFIX + "Loading config " + configUrl + " <hash> " + propHash);
            }
        }
        return false;
    }

}
