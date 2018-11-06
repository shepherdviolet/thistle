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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static sviolet.thistle.x.common.thistlespi.Constants.*;

/**
 * ThistleSpi<br>
 * Java Service Provider Interfaces (SPI) 变种实现<br>
 * 支持`服务装载`和`插件装载`两种方式<br>
 * `服务装载`:根据启动参数/优先级, 从Classpath下声明的多个服务实现中选择唯一的一个进行装载<br>
 * `插件装载`:装载Classpath下声明的全部插件实现, 并根据优先级排序(数字越小优先级越高), 允许通过启动参数和配置排除部分实现<br>
 *
 * @author S.Violet
 */
public class ThistleSpi {

    /**
     * 实现类构造参数Properties中会包含key为_PROPERTIES_URL_的参数, 显示配置文件的路径
     */
    public static final String PROPERTIES_URL = "_PROPERTIES_URL_";

    private static final AtomicInteger LOADER_ID_COUNTER = new AtomicInteger(0);

    private static final Map<String, ServiceLoader> LOADER_CACHE = new ConcurrentHashMap<>(16);

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
            //default config path
            configPath = CONFIG_PATH_DEFAULT;
        }
        if (!configPath.endsWith("/")) {
            //add /
            configPath = configPath + "/";
        }

        //context classloader
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        //create loader if cached disabled
        if (!CACHE_ENABLED) {
            return newLoader(classLoader, configPath);
        }

        //get from cache
        String cacheKey = classLoader.hashCode() + "#" + configPath;
        ServiceLoader serviceLoader = LOADER_CACHE.get(cacheKey);
        boolean fromCache = true;

        //create loader
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

        //print caller
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

    /**
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
            //default classloader
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        if (CheckUtils.isEmptyOrBlank(configPath)) {
            //default config path
            configPath = CONFIG_PATH_DEFAULT;
        }
        if (!configPath.endsWith("/")) {
            //add /
            configPath = configPath + "/";
        }
        return new ServiceLoader(classLoader, configPath);
    }

    /**
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

    // ************************************************************************************************

    /**
     * ServiceLoader, loading services and plugins
     */
    public static class ServiceLoader {

        /**
         * 加载服务, 每次都会重新实例化, 请自行持有服务对象<p>
         * 若服务未定义会返回空, 实例化失败会抛出RuntimeException异常<p>
         * @param type 服务类型(接口全限定名)
         * @return 服务(若找不到定义会返回空)
         */
        public <T> T loadService(Class<T> type) {
            return serviceFactory.loadService(type);
        }

        /**
         * 加载插件, 每次都会重新实例化, 请自行持有插件对象<p>
         * 若插件未定义会返回空列表, 实例化失败会抛出RuntimeException异常<p>
         * @param type 插件类型(接口全限定名)
         * @return 插件(若找不到定义会返回空)
         */
        public <T> List<T> loadPlugins(Class<T> type) {
            return pluginFactory.loadPlugins(type);
        }

        // ************************************************************************************************

        //inner logger
        private SpiLogger logger = new DefaultSpiLogger();
        //loader id
        private int loaderId;

        private ClassLoader classLoader;
        private ServiceFactory serviceFactory;
        private PluginFactory pluginFactory;

        private ServiceLoader(ClassLoader classLoader, String configPath) {
            //加载器编号
            loaderId = LOADER_ID_COUNTER.getAndIncrement();
            //类加载器
            this.classLoader = classLoader;
            //创建服务加载工厂
            serviceFactory = new ServiceFactory(classLoader, logger, loaderId);
            //加载日志打印器配置文件
            serviceFactory.loadConfig(CONFIG_PATH_LOGGER, true);
            //log
            if (LOG_LV >= DEBUG) {
                logger.print(loaderId + LOG_PREFIX + "-------------------------------------------------------------");
            }
            //加载内置日志打印器
            SpiLogger customLogger = serviceFactory.loadService(SpiLogger.class);
            if (customLogger != null) {
                //替换为自定义的日志打印器
                logger = customLogger;
                serviceFactory.setLogger(logger);
            }
            //清空服务配置加载器
            serviceFactory.invalidConfig();
            //打印调用者和ClassLoader
            printCallerInfo();
            //加载服务配置文件
            serviceFactory.loadConfig(configPath, false);
            //创建插件加载工厂
            pluginFactory = new PluginFactory(classLoader, logger, loaderId);
            //加载插件配置文件
            pluginFactory.loadConfig(configPath);
            //log
            if (LOG_LV >= INFO) {
                logger.print(loaderId + LOG_PREFIX + "-------------------------------------------------------------");
            }
        }

        /**
         * 打印调用者
         */
        private void printCallerInfo() {
            if (LOG_LV >= INFO) {
                logger.print(loaderId + LOG_PREFIX + LogUtils.getCallerInfo() + "With classloader " + classLoader.getClass().getName());
            }
        }

    }

}
