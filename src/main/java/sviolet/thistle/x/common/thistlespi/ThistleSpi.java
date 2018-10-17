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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * [非线程安全] <p>
 *
 * ThistleSpi<br>
 * Java Service Provider Interfaces (SPI) 变种实现<br>
 * 支持`服务装载`和`插件装载`两种方式<br>
 * `服务装载`:根据启动参数/优先级, 从Classpath下声明的多个服务实现中选择唯一的一个进行装载<br>
 * `插件装载`:装载Classpath下声明的全部插件实现, 并根据优先级排序(数字越小优先级越高), 允许通过启动参数和配置排除部分实现<br>
 *
 * @author S.Violet
 */
public class ThistleSpi {

    public static final String PROPERTIES_URL = "_PROPERTIES_URL_";

    //日志打印级别(error/debug/verbose, 默认debug)
    static final String PROPERTY_LOGLV = "thistle.spi.loglv";
    //缓存开关(默认true)
    static final String PROPERTY_CACHE = "thistle.spi.cache";
    //强制禁用配置文件(根据文件hash值)
    static final String PROPERTY_FILE_EXCLUSION = "thistle.spi.file.exclusion";

    //默认配置路径
    private static final String CONFIG_PATH = "META-INF/thistle-spi/";
    //[固定]自定义日志打印器配置路径
    private static final String CONFIG_PATH_LOGGER = "META-INF/thistle-spi-logger/";
    //[固定]构造参数引用配置文件路径(相对路径)
    static final String CONFIG_PATH_PARAMETER = "parameter/";

    //日志前缀
    public static final String LOG_PREFIX = " ThistleSpi | ";
    public static final String LOG_PREFIX_LOADER = " ThistleSpi Loader | ";

    //日志级别
    static final int ERROR = 0;
    static final int INFO = 1;
    static final int DEBUG = 2;

    //日志级别(error/info/debug), 默认info
    static final int LOG_LV;

    //是否启用缓存(默认true)
    private static final boolean CACHE;

    //被强制禁用的配置文件的hash
    static final Set<String> FILE_EXCLUSION = new HashSet<>();

    private static final AtomicInteger LOADER_ID_COUNT = new AtomicInteger(0);

    private static final Map<String, ServiceLoader> LOADER_CACHE = new ConcurrentHashMap<>(16);

    static {
        //Log level
        switch (System.getProperty(PROPERTY_LOGLV, "info").toLowerCase()) {
            case "error":
                LOG_LV = ERROR;
                break;
            case "debug":
                LOG_LV = DEBUG;
                break;
            case "info":
            default:
                LOG_LV = INFO;
                break;
        }
        //Cache enabled
        CACHE = "true".equals(System.getProperty(PROPERTY_CACHE, "true"));
        if (LOG_LV >= INFO && !CACHE) {
            System.out.println(DateTimeUtils.getDateTime() + " ?" + LOG_PREFIX + "Loader cache force disabled by -D" + PROPERTY_CACHE + "=false");
        }
        //Config file exclude by hash
        String fileExclusionStr = System.getProperty(PROPERTY_FILE_EXCLUSION, null);
        if (!CheckUtils.isEmptyOrBlank(fileExclusionStr)) {
            String[] array = fileExclusionStr.split(",");
            for (String item : array) {
                if (!CheckUtils.isEmptyOrBlank(item)) {
                    FILE_EXCLUSION.add(item.trim());
                    if (LOG_LV >= INFO) {
                        System.out.println(DateTimeUtils.getDateTime() + " ?" + LOG_PREFIX + "Config file with hash '" + item + "' will be excluded, by -D" + PROPERTY_FILE_EXCLUSION + "=" + fileExclusionStr);
                    }
                }
            }
        }
    }

    /**
     * [非线程安全]<br>
     * 创建一个新的服务加载器(无缓存).<br>
     * 1.尽量用同一个加载器加载服务和插件, 不要反复创建加载器.<br>
     * 2.创建过程会加载所有jar包中的相关配置文件, 根据策略决定每个服务的实现类, 决定每个插件的实现列表.<br>
     * 3.如果有动态类加载的需要, 可以重新创建一个新的服务加载器, 新的类加载器会重新加载配置.<br>
     * 4.配置文件解析出错时会抛出RuntimeException异常.<br>
     * @param classLoader ClassLoader 类加载器
     * @param configPath 自定义配置文件路径, 默认META-INF/thistle-spi/
     * @return 服务加载器
     */
    public static ServiceLoader newLoader(ClassLoader classLoader, String configPath) {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        if (CheckUtils.isEmptyOrBlank(configPath)) {
            configPath = CONFIG_PATH;
        }
        if (!configPath.endsWith("/")) {
            configPath = configPath + "/";
        }
        return new ServiceLoader(classLoader, configPath);
    }

    /**
     * [非线程安全]<br>
     * 创建一个新的服务加载器(无缓存).<br>
     * 1.尽量用同一个加载器加载服务和插件, 不要反复创建加载器.<br>
     * 2.创建过程会加载所有jar包中的相关配置文件, 根据策略决定每个服务的实现类, 决定每个插件的实现列表.<br>
     * 3.如果有动态类加载的需要, 可以重新创建一个新的服务加载器, 新的类加载器会重新加载配置.<br>
     * 4.配置文件解析出错时会抛出RuntimeException异常.<br>
     * @param classLoader ClassLoader 类加载器
     * @return 服务加载器
     */
    public static ServiceLoader newLoader(ClassLoader classLoader) {
        return newLoader(classLoader, null);
    }

    /**
     * [非线程安全]<br>
     * 创建一个新的服务加载器(无缓存).<br>
     * 1.尽量用同一个加载器加载服务和插件, 不要反复创建加载器.<br>
     * 2.创建过程会加载所有jar包中的相关配置文件, 根据策略决定每个服务的实现类, 决定每个插件的实现列表.<br>
     * 3.如果有动态类加载的需要, 可以重新创建一个新的服务加载器, 新的类加载器会重新加载配置.<br>
     * 4.配置文件解析出错时会抛出RuntimeException异常.<br>
     * @param configPath 自定义配置文件路径, 默认META-INF/thistle-spi/
     * @return 服务加载器(使用上下文类加载器)
     */
    public static ServiceLoader newLoader(String configPath) {
        return newLoader(null, configPath);
    }

    /**
     * [非线程安全]<br>
     * 创建一个新的服务加载器(无缓存).<br>
     * 1.尽量用同一个加载器加载服务和插件, 不要反复创建加载器.<br>
     * 2.创建过程会加载所有jar包中的相关配置文件, 根据策略决定每个服务的实现类, 决定每个插件的实现列表.<br>
     * 3.如果有动态类加载的需要, 可以重新创建一个新的服务加载器, 新的类加载器会重新加载配置.<br>
     * 4.配置文件解析出错时会抛出RuntimeException异常.<br>
     * @return 服务加载器(使用上下文类加载器)
     */
    public static ServiceLoader newLoader() {
        return newLoader(null, null);
    }

    /**
     * 获取服务加载器(不能自定义ClassLoader), 第一次获取会有创建过程, 后续从缓存中获得.<br>
     * 1.尽量用同一个加载器加载服务和插件, 不要反复创建加载器.<br>
     * 2.创建过程会加载所有jar包中的相关配置文件, 根据策略决定每个服务的实现类, 决定每个插件的实现列表.<br>
     * 3.配置文件解析出错时会抛出RuntimeException异常.<br>
     * 4.若设置启动参数-Dthistle.spi.cache=false, 则每次都会重新创建加载器.<br>
     * 5.如果有需要(动态类加载/Jar包热插拔/多ClassLoader/自定义ClassLoader), 请使用newLoader方法创建并自行维护加载器.<br>
     * @param configPath 自定义配置文件路径, 默认META-INF/thistle-spi/
     * @return 服务加载器(使用上下文类加载器)
     */
    public static ServiceLoader getLoader(String configPath) {
        if (CheckUtils.isEmptyOrBlank(configPath)) {
            configPath = CONFIG_PATH;
        }
        if (!configPath.endsWith("/")) {
            configPath = configPath + "/";
        }
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        if (!CACHE) {
            return newLoader(classLoader, configPath);
        }

        String cacheKey = classLoader.hashCode() + "#" + configPath;
        ServiceLoader serviceLoader = LOADER_CACHE.get(cacheKey);
        boolean fromCache = true;
        if (serviceLoader == null) {
            synchronized (LOADER_CACHE) {
                serviceLoader = LOADER_CACHE.get(cacheKey);
                if (serviceLoader == null) {
                    serviceLoader = newLoader(classLoader, configPath);
                    LOADER_CACHE.put(cacheKey, serviceLoader);
                    fromCache = false;
                }
            }
        }
        if (fromCache) {
            serviceLoader.printCallerInfo();
        }
        return serviceLoader;
    }

    /**
     * 获取服务加载器(不能自定义ClassLoader), 第一次获取会有创建过程, 后续从缓存中获得.<br>
     * 1.尽量用同一个加载器加载服务和插件, 不要反复创建加载器.<br>
     * 2.创建过程会加载所有jar包中的相关配置文件, 根据策略决定每个服务的实现类, 决定每个插件的实现列表.<br>
     * 3.配置文件解析出错时会抛出RuntimeException异常.<br>
     * 4.若设置启动参数-Dthistle.spi.cache=false, 则每次都会重新创建加载器.<br>
     * 5.如果有需要(动态类加载/Jar包热插拔/多ClassLoader/自定义ClassLoader), 请使用newLoader方法创建并自行维护加载器.<br>
     * @return 服务加载器(使用上下文类加载器)
     */
    public static ServiceLoader getLoader(){
        return getLoader(null);
    }



    /* ***********************************************************************************************************
     * ServiceLoader
     * ***********************************************************************************************************/



    public static class ServiceLoader {

        /**
         * [非线程安全]<p>
         * 加载服务, 每次都会重新实例化, 请自行持有服务对象<p>
         * 若服务未定义会返回空, 实例化失败会抛出RuntimeException异常<p>
         * @param type 服务类型(接口全限定名)
         * @return 服务(若找不到定义会返回空)
         */
        public <T> T loadService(Class<T> type) {
            return serviceConfigLoader.loadService(type);
        }

        /**
         * [非线程安全]<p>
         * 加载插件, 每次都会重新实例化, 请自行持有插件对象<p>
         * 若插件未定义会返回空列表, 实例化失败会抛出RuntimeException异常<p>
         * @param type 插件类型(接口全限定名)
         * @return 插件(若找不到定义会返回空)
         */
        public <T> List<T> loadPlugins(Class<T> type) {
            return pluginConfigLoader.loadPlugins(type);
        }



        /* ***********************************************************************************************************
         * Logic
         * ***********************************************************************************************************/



        private SpiLogger logger = new DefaultSpiLogger();
        private int loaderId;

        private ClassLoader classLoader;
        private ServiceConfigLoader serviceConfigLoader;
        private PluginConfigLoader pluginConfigLoader;

        private ServiceLoader(ClassLoader classLoader, String configPath) {

            //加载器编号
            loaderId = LOADER_ID_COUNT.getAndIncrement();

            //类加载器
            this.classLoader = classLoader;

            //创建服务配置加载器
            serviceConfigLoader = new ServiceConfigLoader(classLoader, logger, loaderId);

            //加载日志打印器配置文件
            serviceConfigLoader.loadConfig(CONFIG_PATH_LOGGER, true);

            if (LOG_LV >= DEBUG) {
                logger.print(loaderId + LOG_PREFIX + "-------------------------------------------------------------");
            }

            //加载自定义日志打印器
            SpiLogger customLogger = serviceConfigLoader.loadService(SpiLogger.class);
            if (customLogger != null) {
                //替换为自定义的日志打印器
                logger = customLogger;
                serviceConfigLoader.setLogger(logger);
            }

            //清空服务配置加载器
            serviceConfigLoader.invalidConfig();

            //打印调用者和ClassLoader
            printCallerInfo();

            //加载服务配置文件
            serviceConfigLoader.loadConfig(configPath, false);

            //创建插件配置加载器
            pluginConfigLoader = new PluginConfigLoader(classLoader, logger, loaderId);

            //加载插件配置文件
            pluginConfigLoader.loadConfig(configPath);

            if (LOG_LV >= INFO) {
                logger.print(loaderId + LOG_PREFIX + "-------------------------------------------------------------");
            }
        }

        /**
         * 打印调用者
         */
        private void printCallerInfo() {
            if (LOG_LV >= INFO) {
                StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
                boolean foundThistleSpi = false;
                for (StackTraceElement element : stackTraceElements) {
                    if (ThistleSpi.class.getName().equals(element.getClassName())) {
                        foundThistleSpi = true;
                    } else if (foundThistleSpi){
                        logger.print(loaderId + LOG_PREFIX +
                                element.getClassName() + "#" + element.getMethodName() + " is trying to load services or plugins. With classloader " +
                                classLoader.getClass().getName());
                        return ;
                    }
                }
            }
        }

    }

}
