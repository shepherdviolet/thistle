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

import java.net.URL;
import java.util.*;

/**
 * [非线程安全] <p>
 *
 * Thistle Spi <p>
 *
 * @author S.Violet
 */
public class ThistleSpi {

    private static final String PROPERTY_DEBUG = "thistle.spi.debug";
    private static final String PROPERTY_SERVICE_APPLY_PREFIX = "thistle.spi.apply.";
    private static final String PROPERTY_PLUGIN_IGNORE_PREFIX = "thistle.spi.ignore.";

    private static final String CONFIG_PATH = "META-INF/thistle-spi/";
    private static final String CONFIG_PATH_LOGGER = "META-INF/thistle-spi-logger/";
    private static final String CONFIG_FILE_SERVICE = "service.properties";
    private static final String CONFIG_FILE_SERVICE_APPLY = "service-apply.properties";
    private static final String CONFIG_FILE_PLUGIN = "plugin.properties";
    private static final String CONFIG_FILE_PLUGIN_IGNORE = "plugin-ignore.properties";

    private static final boolean debug;

    static {
        debug = "true".equals(System.getProperty(PROPERTY_DEBUG, "true"));
    }

    /**
     * [非线程安全]<p>
     * 创建服务加载器:
     * 创建过程会加载所有jar包中的相关配置文件, 根据策略决定每个服务的实现类.
     * 加载多个服务时, 建议使用同一个加载器(避免重复加载相关配置).
     * 如果有动态类加载的需要, 可以在重新加载时, 创建一个新的服务加载器, 新的类加载器会重新加载配置.
     * 配置文件解析出错时会抛出RuntimeException异常.
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
     * [非线程安全]<p>
     * 创建服务加载器:
     * 创建过程会加载所有jar包中的相关配置文件, 根据策略决定每个服务的实现类.
     * 加载多个服务时, 建议使用同一个加载器(避免重复加载相关配置).
     * 如果有动态类加载的需要, 可以在重新加载时, 创建一个新的服务加载器, 新的类加载器会重新加载配置.
     * 配置文件解析出错时会抛出RuntimeException异常.
     * @param classLoader ClassLoader 类加载器
     * @return 服务加载器
     */
    public static ServiceLoader newLoader(ClassLoader classLoader) {
        return newLoader(classLoader, null);
    }

    /**
     * [非线程安全]<p>
     * 创建服务加载器:
     * 创建过程会加载所有jar包中的相关配置文件, 根据策略决定每个服务的实现类.
     * 加载多个服务时, 建议使用同一个加载器(避免重复加载相关配置).
     * 如果有动态类加载的需要, 可以在重新加载时, 创建一个新的服务加载器, 新的类加载器会重新加载配置和类.
     * 配置文件解析出错时会抛出RuntimeException异常.
     * @return 服务加载器(使用上下文类加载器)
     */
    public static ServiceLoader newLoader() {
        return newLoader(null);
    }

    public static class ServiceLoader {

        private ClassLoader classLoader;
        private SpiLogger logger = new DefaultSpiLogger();

        //service配置信息
        private Map<String, ServiceInfo> serviceInfos = new HashMap<>(8);
        //apply配置信息
        private Map<String, ApplyInfo> applyInfos = new HashMap<>(8);
        //service配置信息
        private Map<String, PluginInfo> pluginInfos = new HashMap<>(8);
        //apply配置信息
        private Map<String, IgnoreInfo> ignoreInfos = new HashMap<>(8);

        private ServiceLoader(ClassLoader classLoader, String configPath) {
            this.classLoader = classLoader;
            //加载日志打印器配置文件
            loadServiceConfig(classLoader, logger, CONFIG_PATH_LOGGER, true, serviceInfos, applyInfos);
            //加载日志打印器
            SpiLogger customLogger = loadService(SpiLogger.class);
            if (customLogger != null) {
                logger = customLogger;
            }
            //清除日志打印器的配置
            serviceInfos.clear();
            applyInfos.clear();
            //加载其他配置文件
            loadServiceConfig(classLoader, logger, configPath, false, serviceInfos, applyInfos);
            loadPluginConfig(classLoader, logger, configPath, pluginInfos, ignoreInfos);
        }

        /**
         * [非线程安全]<p>
         * 加载服务(每次都会重新实例化)<p>
         * 加载失败会抛出RuntimeException, 服务不存在则会返回空<p>
         * @param type 服务类型(接口全限定名)
         * @return 服务(若找不到定义会返回空)
         */
        public <T> T loadService(Class<T> type) {
            if (type == null) {
                return null;
            }

            //类名
            String classname = type.getName();
            //获取服务实现信息
            ServiceInfo serviceInfo = serviceInfos.get(classname);

            //不存在服务实现
            if (serviceInfo == null || serviceInfo.appliedService == null) {
                if (debug) {
                    logger.print("Thistle Spi | loadService: no service definition found, type:" + type.getName());
                }
                return null;
            }

            //实例化服务
            Object service;
            try {
                Class clazz = classLoader.loadClass(serviceInfo.appliedService.implement);
                service = clazz.newInstance();
            } catch (Exception e) {
                logger.print("Thistle Spi | loadService: ERROR: Service " + serviceInfo.type + " (" + serviceInfo.appliedService.implement + ") instantiation error, config:" + serviceInfo.appliedService.resource, e);
                throw new RuntimeException("ThistleSpi: Service " + serviceInfo.type + " (" + serviceInfo.appliedService.implement + ") instantiation error, config:" + serviceInfo.appliedService.resource, e);
            }
            if (!type.isAssignableFrom(service.getClass())) {
                logger.print("Thistle Spi | loadService: ERROR: " + serviceInfo.appliedService.implement + " is not instance of " + serviceInfo.type + ", illegal config:" + serviceInfo.appliedService.resource);
                throw new RuntimeException("ThistleSpi: " + serviceInfo.appliedService.implement + " is not instance of " + serviceInfo.type + ", illegal config:" + serviceInfo.appliedService.resource);
            }
            if (debug) {
                logger.print("Thistle Spi | loadService: Service " + serviceInfo.type + " (" + serviceInfo.appliedService.implement + ") loaded successfully");
            }
            return (T) service;
        }

        /**
         * [非线程安全]<p>
         * 加载插件(每次都会重新实例化)<p>
         * 加载失败会抛出RuntimeException, 服务不存在则会返回空<p>
         * @param type 插件类型(接口全限定名)
         * @return 插件(若找不到定义会返回空)
         */
        public <T> List<T> loadPlugins(Class<T> type) {
            if (type == null) {
                return null;
            }

            //类名
            String classname = type.getName();
            //获取插件实现信息
            PluginInfo pluginInfo = pluginInfos.get(classname);

            //不存在插件实现
            if (pluginInfo == null || pluginInfo.orderedPlugins == null) {
                if (debug) {
                    logger.print("Thistle Spi | loadPlugins: no enabled plugins found, type:" + type.getName());
                }
                return null;
            }

            List<T> plugins = new ArrayList<>(pluginInfo.orderedPlugins.size());

            for (Plugin plugin : pluginInfo.orderedPlugins) {

                //实例化服务
                Object pluginObj;
                try {
                    Class clazz = classLoader.loadClass(plugin.implement);
                    pluginObj = clazz.newInstance();
                } catch (Exception e) {
                    logger.print("Thistle Spi | loadPlugins: ERROR: Plugin " + pluginInfo.type + " (" + plugin.implement + ") instantiation error, config:" + plugin.resource, e);
                    throw new RuntimeException("ThistleSpi: Plugin " + pluginInfo.type + " (" + plugin.implement + ") instantiation error, config:" + plugin.resource, e);
                }
                if (!type.isAssignableFrom(pluginObj.getClass())) {
                    logger.print("Thistle Spi | loadPlugins: ERROR: " + plugin.implement + " is not instance of " + pluginInfo.type + ", illegal config:" + plugin.resource);
                    throw new RuntimeException("ThistleSpi: " + plugin.implement + " is not instance of " + pluginInfo.type + ", illegal config:" + plugin.resource);
                }

                plugins.add((T) pluginObj);

            }

            if (debug) {
                StringBuilder stringBuilder = new StringBuilder("Thistle Spi | loadPlugins: Plugin ");
                stringBuilder.append(pluginInfo.type);
                stringBuilder.append(" (");
                for (Plugin plugin : pluginInfo.orderedPlugins) {
                    stringBuilder.append(" ");
                    stringBuilder.append(plugin.implement);
                }
                stringBuilder.append(" ) loaded successfully");
                logger.print(stringBuilder.toString());
            }

            return plugins;
        }

    }

    /* ************************************************************************************************************
     * Load services
     * ************************************************************************************************************/

    private static void loadServiceConfig(
            ClassLoader classLoader,
            SpiLogger logger,
            String configPath,
            boolean loadLogger,
            Map<String, ServiceInfo> serviceInfos,
            Map<String, ApplyInfo> applyInfos){

        if (debug) {
            logger.print("Thistle Spi | -------------------------------------------------------------");
            logger.print("Thistle Spi | Loading " + (loadLogger ? "logger" : "services") + " from " + configPath + ", DOC: https://github.com/shepherdviolet/thistle");
        }

        //loading service.properties

        //加载所有service.properties配置文件
        String serviceConfigFile = configPath + CONFIG_FILE_SERVICE;
        Enumeration<URL> urls;
        try {
            urls = classLoader.getResources(serviceConfigFile);
        } catch (Exception e) {
            logger.print("Thistle Spi | ERROR: Error while loading " + serviceConfigFile, e);
            throw new RuntimeException("ThistleSpi: Error while loading " + serviceConfigFile, e);
        }

        if (urls == null || !urls.hasMoreElements()) {
            if (debug) {
                logger.print("Thistle Spi | No " + serviceConfigFile + " found in classpath");
                logger.print("Thistle Spi | -------------------------------------------------------------");
            }
            return;
        }

        //遍历所有service.properties配置文件
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            String urlStr = String.valueOf(url);

            if (debug) {
                logger.print("Thistle Spi | Loading " + url);
            }

            //装载配置
            Properties properties;
            try {
                properties = new Properties();
                properties.load(url.openStream());
            } catch (Exception e) {
                logger.print("Thistle Spi | ERROR: Error while loading config " + urlStr, e);
                throw new RuntimeException("ThistleSpi: Error while loading config " + urlStr, e);
            }

            if (properties.size() <= 0) {
                if (debug) {
                    logger.print("Thistle Spi | Warning: No properties in " + url);
                }
            }

            //遍历所有key-value
            Enumeration<?> names = properties.propertyNames();
            while (names.hasMoreElements()) {
                String key = String.valueOf(names.nextElement());

                //拆解key
                String[] keyItems = key.split(">");
                if (keyItems.length != 3) {
                    logger.print("Thistle Spi | ERROR: Illegal key in config file, key:" + key + ", correct format:interface>id>level=impl, config:" + urlStr);
                    throw new RuntimeException("ThistleSpi: Illegal key in config file, key:" + key + ", correct format:interface>id>level=impl, config:" + urlStr);
                }

                String type = keyItems[0];
                String id = keyItems[1];
                Level level = Level.parse(keyItems[2]);

                if (level == Level.UNDEFINED) {
                    logger.print("Thistle Spi | ERROR: Illegal config, undefined level " + level + ", should be library/platform/application, in key:" + key + ", config:" + urlStr);
                    throw new RuntimeException("ThistleSpi: Illegal config, undefined level " + level + ", should be library/platform/application, in key:" + key + ", config:" + urlStr);
                }

                //遇到新的服务接口, 则创建一个对象
                ServiceInfo serviceInfo = serviceInfos.get(type);
                if (serviceInfo == null) {
                    serviceInfo = new ServiceInfo();
                    serviceInfo.type = type;
                    serviceInfos.put(type, serviceInfo);
                }

                //实现类
                String implement = properties.getProperty(key);
                if (CheckUtils.isEmptyOrBlank(implement)) {
                    logger.print("Thistle Spi | ERROR: Illegal config, value of " + key + " is empty, config:" + urlStr);
                    throw new RuntimeException("ThistleSpi: Illegal config, value of " + key + " is empty, config:" + urlStr);
                }

                //服务接口信息
                Service service = new Service();
                service.id = id;
                service.level = level;
                service.implement = implement;
                service.resource = urlStr;

                Service previous = serviceInfo.definedServices.get(id);
                //若有重复id, 则抛出异常
                if (previous != null) {
                    logger.print("Thistle Spi | ERROR: Duplicate service defined with same id, type:" + type + ", id:" + id + ", url1:" + url + ", url2:" + previous.resource);
                    throw new RuntimeException("ThistleSpi: Duplicate service defined with same id, type:" + type + ", id:" + id + ", url1:" + url + ", url2:" + previous.resource);
                }

                serviceInfo.definedServices.put(id, service);

            }

        }

        //loading service-apply.properties

        //加载所有service-apply.properties配置文件
        String applyConfigFile = configPath + CONFIG_FILE_SERVICE_APPLY;
        try {
            urls = classLoader.getResources(applyConfigFile);
        } catch (Exception e) {
            logger.print("Thistle Spi | ERROR: Error while loading config " + applyConfigFile, e);
            throw new RuntimeException("ThistleSpi: Error while loading config " + applyConfigFile, e);
        }

        //遍历所有service-apply.properties配置文件
        while (urls != null && urls.hasMoreElements()) {
            URL url = urls.nextElement();
            String urlStr = String.valueOf(url);

            if (debug) {
                logger.print("Thistle Spi | loading " + url);
            }

            //装载配置文件
            Properties properties;
            try {
                properties = new Properties();
                properties.load(url.openStream());
            } catch (Exception e) {
                logger.print("Thistle Spi | ERROR: Error while loading config " + urlStr, e);
                throw new RuntimeException("ThistleSpi: Error while loading config " + urlStr, e);
            }

            if (properties.size() <= 0) {
                if (debug) {
                    logger.print("Thistle Spi | Warning: No properties in " + url);
                }
            }

            //遍历所有key-value
            Enumeration<?> names = properties.propertyNames();
            while (names.hasMoreElements()) {
                String type = String.valueOf(names.nextElement());
                String id = properties.getProperty(type);
                if (CheckUtils.isEmptyOrBlank(id)) {
                    logger.print("Thistle Spi | ERROR: Illegal config, value of " + type + " is empty, config:" + urlStr);
                    throw new RuntimeException("ThistleSpi: Illegal config, value of " + type + " is empty, config:" + urlStr);
                }

                if (applyInfos.containsKey(type)) {
                    //apply配置重复处理
                    ApplyInfo previous = applyInfos.get(type);
                    if (id.equals(previous.id)){
                        //若id相同, 不抛出错误, 仅做提醒
                        if (debug) {
                            logger.print("Thistle Spi | Warning: Duplicate apply defined with same value, key:" + type + ", value:" + id + ", url1:" + url + ", url2:" + previous.resource);
                        }
                    } else {
                        //若id不相同, 则需要抛出异常
                        String idFromJvmArgs = System.getProperty(PROPERTY_SERVICE_APPLY_PREFIX + type);
                        //允许使用-Dthistle.spi.apply解决apply冲突
                        //we can use -Dthistle.spi.apply to resolve duplicate error
                        String duplicateError = "Duplicate apply defined with different value, key:" + type + ", value1:" + id + ", value2:" + previous.id + ", url1:" + url + ", url2:" + previous.resource;
                        if (CheckUtils.isEmptyOrBlank(idFromJvmArgs)) {
                            //如果没有-Dthistle.spi.apply, 直接抛出异常
                            //no -Dthistle.spi.apply, throw exception
                            logger.print("Thistle Spi | ERROR: " + duplicateError);
                            throw new RuntimeException("ThistleSpi: " + duplicateError);
                        } else {
                            //如果有-Dthistle.spi.apply, 先放一马
                            //try with -Dthistle.spi.apply
                            previous.duplicateError = duplicateError;
                            if (debug) {
                                logger.print("Thistle Spi | Warning: (Resolve by -Dthistle.spi.apply)" + duplicateError);
                            }
                        }
                    }
                    continue;
                }

                //创建apply信息
                ApplyInfo applyInfo = new ApplyInfo();
                applyInfo.type = type;
                applyInfo.id = properties.getProperty(type);
                applyInfo.resource = urlStr;
                applyInfos.put(type, applyInfo);

            }

        }

        //apply service

        if (debug) {
            logger.print("Thistle Spi | Loading finish");
        }

        //遍历所有服务
        for (ServiceInfo spi : serviceInfos.values()) {

            //优先用-Dthistle.spi.apply选择服务实现
            String applyId = System.getProperty(PROPERTY_SERVICE_APPLY_PREFIX + spi.type);
            if (!CheckUtils.isEmptyOrBlank(applyId)) {
                Service service = spi.definedServices.get(applyId);
                if (service != null) {
                    spi.appliedService = service;
                    spi.applyReason = "-D" + PROPERTY_SERVICE_APPLY_PREFIX + spi.type + "=" + applyId;
                    continue;
                }
                if (debug) {
                    logger.print("Thistle Spi | Warning: No service named " + applyId + ", failed to apply service '" + spi.type + "' to id '" + applyId + "' by -D" + PROPERTY_SERVICE_APPLY_PREFIX + spi.type + "=" + applyId);
                }
            }

            //然后用apply配置选择服务实现
            if (applyInfos.containsKey(spi.type)){
                ApplyInfo applyInfo = applyInfos.get(spi.type);
                if (applyInfo.duplicateError != null) {
                    logger.print("Thistle Spi | ERROR: " + applyInfo.duplicateError);
                    throw new RuntimeException("ThistleSpi: " + applyInfo.duplicateError);
                }
                Service service = spi.definedServices.get(applyInfo.id);
                if (service != null) {
                    spi.appliedService = service;
                    spi.applyReason = service.resource;
                    continue;
                }
                if (debug) {
                    logger.print("Thistle Spi | Warning: No service named " + applyInfo.id + ", failed to apply service '" + spi.type + "' to id '" + applyInfo.id + "' by " + applyInfo.resource);
                    logger.print("Thistle Spi | Warning: We will apply '" + spi.type + "' service by level automatically (application > platform > library)");
                }
            }

            //最后用优先级选择服务实现

            List<Service> appliedServices = new ArrayList<>(1);
            int highestPriority = -1;
            for (Service service : spi.definedServices.values()) {
                if (service.level.getPriority() > highestPriority) {
                    appliedServices.clear();
                    appliedServices.add(service);
                    highestPriority = service.level.getPriority();
                } else if (service.level.getPriority() == highestPriority) {
                    appliedServices.add(service);
                }
            }

            if (appliedServices.size() <= 0) {
                continue;
            }

            if (appliedServices.size() > 1) {
                StringBuilder stringBuilder = new StringBuilder("Duplicate service defined with same level, type:" + spi.type + ", conflicts:");
                for (Service service : appliedServices) {
                    stringBuilder.append(service);
                    stringBuilder.append("|");
                }
                logger.print("Thistle Spi | ERROR: " + stringBuilder.toString());
                throw new RuntimeException("ThistleSpi: " + stringBuilder.toString());
            }

            spi.appliedService = appliedServices.get(0);
            spi.applyReason = "level automatically (application > platform > library)";

        }

        if (debug) {

            for (ServiceInfo serviceInfo : serviceInfos.values()) {

                logger.print("Thistle Spi | -------------------------------------------------------------");
                logger.print("Thistle Spi | Service:");
                logger.print("Thistle Spi | type: " + serviceInfo.type);
                logger.print("Thistle Spi | impl: " + serviceInfo.appliedService.implement);
                logger.print("Thistle Spi | url: " + serviceInfo.appliedService.resource);
                logger.print("Thistle Spi | reason: Applied by " + serviceInfo.applyReason);
                logger.print("Thistle Spi | Definitions:");

                for (Service service : serviceInfo.definedServices.values()) {
                    if (service == serviceInfo.appliedService) {
                        logger.print("Thistle Spi | + " + service);
                    } else {
                        logger.print("Thistle Spi | - " + service);
                    }
                }

            }

            logger.print("Thistle Spi | -------------------------------------------------------------");
        }

    }

    private static class ServiceInfo {

        private String type;
        private Service appliedService;
        private String applyReason;
        private Map<String, Service> definedServices = new HashMap<>(1);

    }

    private static class Service {

        private String id;
        private Level level;
        private String implement;
        private String resource;

        @Override
        public String toString() {
            return "Service{" +
                    "id=" + id +
                    ", level=" + level +
                    ", impl=" + implement +
                    ", url=" + resource +
                    '}';
        }
    }

    private static class ApplyInfo {

        private String type;
        private String id;
        private String resource;
        private String duplicateError;

    }

    private enum Level {

        LIBRARY(0),
        PLATFORM(1),
        APPLICATION(2),
        UNDEFINED(-1);

        //The higher the value, the higher the priority
        private int priority;

        Level(int priority) {
            this.priority = priority;
        }

        private int getPriority(){
            return priority;
        }

        private static Level parse(String level){
            if (level == null) {
                return null;
            }
            switch (level.toUpperCase()) {
                case "LIBRARY":
                    return LIBRARY;
                case "PLATFORM":
                    return PLATFORM;
                case "APPLICATION":
                    return APPLICATION;
                default:
                    return UNDEFINED;
            }
        }

    }

    /* ************************************************************************************************************
     * Load plugins
     * ************************************************************************************************************/

    private static void loadPluginConfig(
            ClassLoader classLoader,
            SpiLogger logger,
            String configPath,
            Map<String, PluginInfo> pluginInfos,
            Map<String, IgnoreInfo> ignoreInfos){

        if (debug) {
            logger.print("Thistle Spi | -------------------------------------------------------------");
            logger.print("Thistle Spi | Loading plugins from " + configPath + ", DOC: https://github.com/shepherdviolet/thistle");
        }

        //loading plugin.properties

        //加载所有plugin.properties配置文件
        String pluginConfigFile = configPath + CONFIG_FILE_PLUGIN;
        Enumeration<URL> urls;
        try {
            urls = classLoader.getResources(pluginConfigFile);
        } catch (Exception e) {
            logger.print("Thistle Spi | ERROR: Error while loading " + pluginConfigFile, e);
            throw new RuntimeException("ThistleSpi: Error while loading " + pluginConfigFile, e);
        }

        if (urls == null || !urls.hasMoreElements()) {
            if (debug) {
                logger.print("Thistle Spi | No " + pluginConfigFile + " found in classpath");
                logger.print("Thistle Spi | -------------------------------------------------------------");
            }
            return;
        }

        //遍历所有plugin.properties配置文件
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            String urlStr = String.valueOf(url);

            if (debug) {
                logger.print("Thistle Spi | Loading " + url);
            }

            //装载配置
            Properties properties;
            try {
                properties = new Properties();
                properties.load(url.openStream());
            } catch (Exception e) {
                logger.print("Thistle Spi | ERROR: Error while loading config " + urlStr, e);
                throw new RuntimeException("ThistleSpi: Error while loading config " + urlStr, e);
            }

            if (properties.size() <= 0) {
                if (debug) {
                    logger.print("Thistle Spi | Warning: No properties in " + url);
                }
            }

            //遍历所有key-value
            Enumeration<?> names = properties.propertyNames();
            while (names.hasMoreElements()) {
                String key = String.valueOf(names.nextElement());

                //拆解key
                String[] keyItems = key.split(">");
                if (keyItems.length != 2) {
                    logger.print("Thistle Spi | ERROR: Illegal key in config file, key:" + key + ", correct format:interface>priority=impl, config:" + urlStr);
                    throw new RuntimeException("ThistleSpi: Illegal key in config file, key:" + key + ", correct format:interface>priority=impl, config:" + urlStr);
                }

                String type = keyItems[0];
                int priority;
                try {
                    priority = Integer.valueOf(keyItems[1]);
                } catch (Exception e) {
                    logger.print("Thistle Spi | ERROR: Illegal config, invalid priority " + keyItems[1] + ", should be integer, in key:" + key + ", config:" + urlStr);
                    throw new RuntimeException("ThistleSpi: Illegal config, invalid priority " + keyItems[1] + ", should be integer, in key:" + key + ", config:" + urlStr);
                }

                //遇到新的服务接口, 则创建一个对象
                PluginInfo pluginInfo = pluginInfos.get(type);
                if (pluginInfo == null) {
                    pluginInfo = new PluginInfo();
                    pluginInfo.type = type;
                    pluginInfos.put(type, pluginInfo);
                }

                //实现类
                String implement = properties.getProperty(key);
                if (CheckUtils.isEmptyOrBlank(implement)) {
                    logger.print("Thistle Spi | ERROR: Illegal config, value of " + key + " is empty, config:" + urlStr);
                    throw new RuntimeException("ThistleSpi: Illegal config, value of " + key + " is empty, config:" + urlStr);
                }

                //服务接口信息
                Plugin plugin = new Plugin();
                plugin.priority = priority;
                plugin.implement = implement;
                plugin.resource = urlStr;
                pluginInfo.plugins.add(plugin);

            }

        }

        //loading plugin-ignoreImpl.properties

        //加载所有plugin-ignoreImpl.properties配置文件
        String ignoreConfigFile = configPath + CONFIG_FILE_PLUGIN_IGNORE;
        try {
            urls = classLoader.getResources(ignoreConfigFile);
        } catch (Exception e) {
            logger.print("Thistle Spi | ERROR: Error while loading config " + ignoreConfigFile, e);
            throw new RuntimeException("ThistleSpi: Error while loading config " + ignoreConfigFile, e);
        }

        //遍历所有plugin-ignoreImpl.properties配置文件
        while (urls != null && urls.hasMoreElements()) {
            URL url = urls.nextElement();
            String urlStr = String.valueOf(url);

            if (debug) {
                logger.print("Thistle Spi | loading " + url);
            }

            //装载配置文件
            Properties properties;
            try {
                properties = new Properties();
                properties.load(url.openStream());
            } catch (Exception e) {
                logger.print("Thistle Spi | ERROR: Error while loading config " + urlStr, e);
                throw new RuntimeException("ThistleSpi: Error while loading config " + urlStr, e);
            }

            if (properties.size() <= 0) {
                if (debug) {
                    logger.print("Thistle Spi | Warning: No properties in " + url);
                }
            }

            //遍历所有key-value
            Enumeration<?> names = properties.propertyNames();
            while (names.hasMoreElements()) {
                String type = String.valueOf(names.nextElement());
                String ignoreStr = properties.getProperty(type);
                if (CheckUtils.isEmptyOrBlank(ignoreStr)) {
                    logger.print("Thistle Spi | ERROR: Illegal config, value of " + type + " is empty, config:" + urlStr);
                    throw new RuntimeException("ThistleSpi: Illegal config, value of " + type + " is empty, config:" + urlStr);
                }

                IgnoreInfo ignoreInfo = ignoreInfos.get(type);
                if (ignoreInfo == null) {
                    ignoreInfo = new IgnoreInfo();
                    ignoreInfo.type = type;
                    ignoreInfos.put(type, ignoreInfo);
                }

                String[] ignoreImpls = ignoreStr.split(",");

                for (String ignoreImpl : ignoreImpls) {
                    if (ignoreImpl == null) {
                        continue;
                    }
                    ignoreImpl = ignoreImpl.trim();
                    if (ignoreImpl.length() <= 0) {
                        continue;
                    }
                    Ignore ignore = new Ignore();
                    ignore.ignoreImpl = ignoreImpl;
                    ignore.resource = urlStr;
                    ignoreInfo.ignores.add(ignore);
                }
            }

        }

        //apply service

        if (debug) {
            logger.print("Thistle Spi | Loading finish");
        }

        //遍历所有服务
        for (PluginInfo pluginInfo : pluginInfos.values()) {

            //优先用-Dthistle.spi.ignore忽略插件实现
            String ignoreStr = System.getProperty(PROPERTY_PLUGIN_IGNORE_PREFIX + pluginInfo.type);
            if (!CheckUtils.isEmptyOrBlank(ignoreStr)) {
                String[] ignoreImpls = ignoreStr.split(",");
                for (String ignoreImpl : ignoreImpls) {
                    if (ignoreImpl == null) {
                        continue;
                    }
                    ignoreImpl = ignoreImpl.trim();
                    if (ignoreImpl.length() <= 0) {
                        continue;
                    }
                    int count = 0;
                    for (Plugin plugin : pluginInfo.plugins) {
                        if (ignoreImpl.equals(plugin.implement)) {
                            count++;
                            plugin.enabled = false;
                            plugin.disableReason = "-D" + PROPERTY_PLUGIN_IGNORE_PREFIX + pluginInfo.type + "=" + ignoreStr;
                        }
                    }
                    if (debug && count <= 0) {
                        logger.print("Thistle Spi | Warning: Plugin implement " + ignoreImpl + " undefined, failed to ignore implement '" + ignoreImpl + "' of '" + pluginInfo.type + "' by -D" + PROPERTY_PLUGIN_IGNORE_PREFIX + pluginInfo.type + "=" + ignoreStr);
                    }
                }
            }

            //然后用配置忽略插件实现
            if (ignoreInfos.containsKey(pluginInfo.type)){
                IgnoreInfo ignoreInfo = ignoreInfos.get(pluginInfo.type);
                for (Ignore ignore : ignoreInfo.ignores) {
                    int count = 0;
                    for (Plugin plugin : pluginInfo.plugins) {
                        if (ignore.ignoreImpl.equals(plugin.implement)) {
                            count++;
                            plugin.enabled = false;
                            plugin.disableReason = ignore.resource;
                        }
                    }
                    if (debug && count <= 0) {
                        logger.print("Thistle Spi | Warning: Plugin implement " + ignore.ignoreImpl + " undefined, failed to ignore implement '" + ignore.ignoreImpl + "' of '" + pluginInfo.type + "' by " + ignore.resource);
                    }
                }
            }

            //最后取可用的插件排序
            for (Plugin plugin : pluginInfo.plugins) {
                if (plugin.enabled) {
                    pluginInfo.orderedPlugins.add(plugin);
                }
            }
            Collections.sort(pluginInfo.orderedPlugins, new Comparator<Plugin>() {
                @Override
                public int compare(Plugin o1, Plugin o2) {
                    return o1.priority - o2.priority;
                }
            });

        }

        if (debug) {

            for (PluginInfo pluginInfo : pluginInfos.values()) {

                logger.print("Thistle Spi | -------------------------------------------------------------");
                logger.print("Thistle Spi | Plugin:");
                logger.print("Thistle Spi | type: " + pluginInfo.type);
                logger.print("Thistle Spi | Enabled:");
                for (Plugin plugin : pluginInfo.orderedPlugins) {
                    logger.print("Thistle Spi | + " + plugin.toAbstractString());
                }
                logger.print("Thistle Spi | Definitions:");
                for (Plugin plugin : pluginInfo.plugins) {
                    logger.print("Thistle Spi | " + (plugin.enabled ? "+ " : "- ") + plugin);
                }

            }

            logger.print("Thistle Spi | -------------------------------------------------------------");
        }

    }

    private static class PluginInfo {

        private String type;
        private List<Plugin> plugins = new ArrayList<>(8);
        private List<Plugin> orderedPlugins = new ArrayList<>(8);

    }

    private static class Plugin {

        private int priority;
        private String implement;
        private String resource;
        private boolean enabled = true;
        private String disableReason;

        public String toAbstractString(){
            return "Plugin{" +
                    "priority=" + priority +
                    ", impl=" + implement +
                    ", url=" + resource +
                    '}';
        }

        @Override
        public String toString() {
            return "Plugin{" +
                    "enable=" + enabled +
                    ", priority=" + priority +
                    ", impl=" + implement +
                    (enabled ? "" : ", disable by " + disableReason) +
                    ", url=" + resource +
                    '}';
        }

    }

    private static class IgnoreInfo {

        private String type;
        private List<Ignore> ignores = new ArrayList<>(8);

    }

    private static class Ignore {

        private String ignoreImpl;
        private String resource;

        @Override
        public String toString() {
            return "Ignore{" +
                    "ignoreImpl=" + ignoreImpl +
                    ", resource=" + resource +
                    '}';
        }

    }

}
