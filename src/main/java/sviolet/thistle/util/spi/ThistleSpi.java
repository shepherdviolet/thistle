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

package sviolet.thistle.util.spi;

import sviolet.thistle.util.judge.CheckUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * [非线程安全] <p>
 *
 * ThistleSpi<p>
 *
 * @author S.Violet
 */
public class ThistleSpi {

    private static final String PROPERTY_DEBUG = "thistle.spi.debug";
    private static final String PROPERTY_CACHE = "thistle.spi.cache";

    private static final String CONFIG_PATH = "META-INF/thistle-spi/";
    private static final String CONFIG_PATH_LOGGER = "META-INF/thistle-spi-logger/";

    static final String LOG_PREFIX = " ThistleSpi | ";
    static final String LOG_PREFIX_LOADER = " ThistleSpi ServiceLoader | ";

    static final boolean debug;
    static final boolean cache;

    private static final AtomicInteger loaderIdCount = new AtomicInteger(0);

    private static final Map<String, ServiceLoader> loaderCache = new ConcurrentHashMap<>(16);

    static {
        debug = "true".equals(System.getProperty(PROPERTY_DEBUG, "true"));
        cache = "true".equals(System.getProperty(PROPERTY_CACHE, "true"));
        if (!cache) {
            System.out.print("?" + LOG_PREFIX + "Cache force disabled by -D" + PROPERTY_CACHE + "=false");
        }
    }

    /**
     * [非线程安全]<br>
     * 创建一个新的服务加载器.<br>
     * 1.创建过程会加载所有jar包中的相关配置文件, 根据策略决定每个服务的实现类, 决定每个插件的实现列表.<br>
     * 2.加载多个服务/插件时, 请使用同一个加载器, 以避免重复加载相关配置.<br>
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
        return new ServiceLoader(classLoader, configPath);
    }

    /**
     * [非线程安全]<br>
     * 创建一个新的服务加载器.<br>
     * 1.创建过程会加载所有jar包中的相关配置文件, 根据策略决定每个服务的实现类, 决定每个插件的实现列表.<br>
     * 2.加载多个服务/插件时, 请使用同一个加载器, 以避免重复加载相关配置.<br>
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
     * 创建一个新的服务加载器.<br>
     * 1.创建过程会加载所有jar包中的相关配置文件, 根据策略决定每个服务的实现类, 决定每个插件的实现列表.<br>
     * 2.加载多个服务/插件时, 请使用同一个加载器, 以避免重复加载相关配置.<br>
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
     * 创建一个新的服务加载器.<br>
     * 1.创建过程会加载所有jar包中的相关配置文件, 根据策略决定每个服务的实现类, 决定每个插件的实现列表.<br>
     * 2.加载多个服务/插件时, 请使用同一个加载器, 以避免重复加载相关配置.<br>
     * 3.如果有动态类加载的需要, 可以重新创建一个新的服务加载器, 新的类加载器会重新加载配置.<br>
     * 4.配置文件解析出错时会抛出RuntimeException异常.<br>
     * @return 服务加载器(使用上下文类加载器)
     */
    public static ServiceLoader newLoader() {
        return newLoader(null, null);
    }

    /**
     * 获取服务加载器(不能自定义ClassLoader), 第一次获取会有创建过程, 后续从缓存中获得.<br>
     * 1.创建过程会加载所有jar包中的相关配置文件, 根据策略决定每个服务的实现类, 决定每个插件的实现列表.<br>
     * 2.配置文件解析出错时会抛出RuntimeException异常.<br>
     * 3.若设置启动参数-Dthistle.spi.cache=false, 则每次都会重新创建加载器.<br>
     * 4.如果有动态类加载的需要, 或使用自定义ClassLoader, 请使用newLoader方法创建并自行维护加载器.<br>
     * 5.getLoader适合用于用户级项目, 开源库建议使用newLoader方法创建并自行维护加载器.<br>
     * @param configPath 自定义配置文件路径, 默认META-INF/thistle-spi/
     * @return 服务加载器(使用上下文类加载器)
     */
    public static ServiceLoader getLoader(String configPath) {
        if (CheckUtils.isEmptyOrBlank(configPath)) {
            configPath = CONFIG_PATH;
        }
        if (!cache) {
            return newLoader(null, configPath);
        }
        ServiceLoader serviceLoader = loaderCache.get(configPath);
        if (serviceLoader == null) {
            synchronized (loaderCache) {
                serviceLoader = loaderCache.get(configPath);
                if (serviceLoader == null) {
                    serviceLoader = newLoader(null, configPath);
                    loaderCache.put(configPath, serviceLoader);
                }
            }
        }
        return serviceLoader;
    }

    /**
     * 获取服务加载器(不能自定义ClassLoader), 第一次获取会有创建过程, 后续从缓存中获得.<br>
     * 1.创建过程会加载所有jar包中的相关配置文件, 根据策略决定每个服务的实现类, 决定每个插件的实现列表.<br>
     * 2.配置文件解析出错时会抛出RuntimeException异常.<br>
     * 3.若设置启动参数-Dthistle.spi.cache=false, 则每次都会重新创建加载器.<br>
     * 4.如果有动态类加载的需要, 或使用自定义ClassLoader, 请使用newLoader方法创建, 并自行维护加载器.<br>
     * 5.getLoader适合用于用户级项目, 开源库建议使用newLoader方法创建并自行维护加载器.<br>
     * @return 服务加载器(使用上下文类加载器)
     */
    public static ServiceLoader getLoader(){
        return getLoader(null);
    }

    public static class ServiceLoader {

        private SpiLogger logger = new DefaultSpiLogger();
        private ServiceConfigLoader serviceConfigLoader;
        private PluginConfigLoader pluginConfigLoader;

        private ServiceLoader(ClassLoader classLoader, String configPath) {
            int loaderId = loaderIdCount.getAndIncrement();
            serviceConfigLoader = new ServiceConfigLoader(classLoader, logger, loaderId);

            //加载日志打印器配置文件
            serviceConfigLoader.loadConfig(CONFIG_PATH_LOGGER);

            if (debug) {
                logger.print(loaderId + LOG_PREFIX + "-------------------------------------------------------------");
            }

            //加载自定义日志打印器
            SpiLogger customLogger = serviceConfigLoader.loadService(SpiLogger.class);
            if (customLogger != null) {
                logger = customLogger;
                serviceConfigLoader.setLogger(logger);
            }

            //清除日志打印器的配置
            serviceConfigLoader.invalidConfig();

            //打印调用者
            printCaller(loaderId);

            //打印ClassLoader
            if (debug) {
                logger.print(loaderId + LOG_PREFIX + "Classloader " + classLoader.getClass().getName());
            }

            //加载服务配置文件
            serviceConfigLoader.loadConfig(configPath);

            pluginConfigLoader = new PluginConfigLoader(classLoader, logger, loaderId);
            pluginConfigLoader.loadConfig(configPath);

            if (debug) {
                logger.print(loaderId + LOG_PREFIX + "-------------------------------------------------------------");
            }
        }

        /**
         * [非线程安全]<p>
         * 加载服务(每次都会重新实例化), 请自行持有服务对象<p>
         * 加载失败会抛出RuntimeException, 服务不存在则会返回空<p>
         * @param type 服务类型(接口全限定名)
         * @return 服务(若找不到定义会返回空)
         */
        public <T> T loadService(Class<T> type) {
            return serviceConfigLoader.loadService(type);
        }

        /**
         * [非线程安全]<p>
         * 加载插件(每次都会重新实例化), 请自行持有插件对象<p>
         * 加载失败会抛出RuntimeException, 服务不存在则会返回空<p>
         * @param type 插件类型(接口全限定名)
         * @return 插件(若找不到定义会返回空)
         */
        public <T> List<T> loadPlugins(Class<T> type) {
            return pluginConfigLoader.loadPlugins(type);
        }

        private void printCaller(int loaderId) {
            if (debug) {
                StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
                boolean foundThistleSpi = false;
                for (StackTraceElement element : stackTraceElements) {
                    if (ThistleSpi.class.getName().equals(element.getClassName())) {
                        foundThistleSpi = true;
                    } else if (foundThistleSpi){
                        logger.print(loaderId + LOG_PREFIX + "Loader create by " + element.getClassName() + "#" + element.getMethodName());
                        break;
                    }
                }
            }
        }

    }

}
