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

package sviolet.thistle.util.common;

import sviolet.thistle.util.conversion.DateTimeUtils;
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
    private static final String PROPERTY_LOGGER = "thistle.spi.logger";
    private static final String PROPERTY_APPLY_PREFIX = "thistle.spi.apply.";

    private static final String SERVICE_CONFIG_PATH = "META-INF/thistle.spi.service";
    private static final String SERVICE_CONFIG_ID = "id";
    private static final String SERVICE_CONFIG_LEVEL = "level";

    private static final String APPLY_CONFIG_PATH = "META-INF/thistle.spi.apply";

    private static final boolean debug;
    private static final String loggerClass;

    static {
        debug = "true".equals(System.getProperty(PROPERTY_DEBUG, "true"));
        loggerClass = System.getProperty(PROPERTY_LOGGER, null);
    }

    /**
     * [非线程安全]<p>
     * 创建服务加载器<p>
     * 该方法会从Classpath中加载所有配置文件, 若配置文件格式有误, 会抛出RuntimeException<p>
     * @param classLoader ClassLoader
     * @return 服务加载器
     */
    public static ServiceLoader newLoader(ClassLoader classLoader) {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        return new ServiceLoader(classLoader);
    }

    /**
     * [非线程安全]<p>
     * 创建服务加载器<p>
     * 该方法会从Classpath中加载所有配置文件, 若配置文件格式有误, 会抛出RuntimeException<p>
     * @return 服务加载器
     */
    public static ServiceLoader newLoader() {
        return newLoader(null);
    }

    public static class ServiceLoader {

        private ClassLoader classLoader;
        private Logger logger;

        private Map<String, SpiInfo> spiInfos = new HashMap<>(8);
        private Map<String, ApplyInfo> applyInfos = new HashMap<>(8);
        private Map<String, Object> serviceCache = new HashMap<>(8);

        private ServiceLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            logger = loadLogger(classLoader);
            loadProperties(classLoader, logger, spiInfos, applyInfos);
        }

        /**
         * [非线程安全]<p>
         * 加载服务(重复加载会从缓存中获取)<p>
         * 加载失败会抛出RuntimeException, 服务不存在则会返回空<p>
         * @param type 服务类型(接口全限定名)
         * @return 服务(若找不到定义会返回空)
         */
        public <T> T loadService(Class<T> type) {
            if (type == null) {
                return null;
            }
            String classname = type.getName();
            if (serviceCache.containsKey(classname)) {
                return (T) serviceCache.get(classname);
            }

            SpiInfo spiInfo = spiInfos.get(classname);
            if (spiInfo.appliedService == null) {
                if (debug) {
                    logger.print("Thistle Spi | Warning: loadService failed, no service definition found, type:" + type.getName());
                }
                serviceCache.put(classname, null);
                return null;
            }

            try {
                Class clazz = classLoader.loadClass(spiInfo.appliedService.implement);
                T service = (T) clazz.newInstance();
                serviceCache.put(classname, service);
                if (debug) {
                    logger.print("Thistle Spi | Load service " + spiInfo.type + " (" + spiInfo.appliedService.implement + ") succeed");
                }
                return service;
            } catch (ClassCastException e) {
                logger.print("Thistle Spi | ERROR: " + spiInfo.appliedService.implement + " is not instance of " + spiInfo.type + ", illegal config:" + spiInfo.appliedService.resource, e);
                throw new RuntimeException("ThistleSpi: " + spiInfo.appliedService.implement + " is not instance of " + spiInfo.type + ", illegal config:" + spiInfo.appliedService.resource, e);
            } catch (Exception e) {
                logger.print("Thistle Spi | ERROR: Service " + spiInfo.type + " (" + spiInfo.appliedService.implement + ") instantiation error, config:" + spiInfo.appliedService.resource, e);
                throw new RuntimeException("ThistleSpi: Service " + spiInfo.type + " (" + spiInfo.appliedService.implement + ") instantiation error, config:" + spiInfo.appliedService.resource, e);
            }
        }

        public Set<String> loadedServiceTypes(){
            return serviceCache.keySet();
        }

        public Collection<Object> loadedServices(){
            return serviceCache.values();
        }

    }

    public interface Logger {

        void print(String msg);

        void print(String msg, Throwable throwable);

    }

    public static class DefaultLogger implements Logger {

        @Override
        public void print(String msg) {
            System.out.println(DateTimeUtils.getDateTime() + " " + msg);
        }

        @Override
        public void print(String msg, Throwable throwable) {
            System.out.println(DateTimeUtils.getDateTime() + " " + msg);
            if (throwable != null) {
                throwable.printStackTrace();
            }
        }

    }

    /* ************************************************************************************************************
     * inner logic
     * ************************************************************************************************************/

    private static Logger loadLogger(ClassLoader classLoader) {
        if (CheckUtils.isEmptyOrBlank(loggerClass)) {
            return new DefaultLogger();
        }
        try {
            Class clazz = classLoader.loadClass(loggerClass);
            return (Logger) clazz.newInstance();
        } catch (ClassCastException e) {
            throw new RuntimeException("ThistleSpi: Logger is not an instance of sviolet.thistle.util.common.ThistleSpi$Logger which defined by -D" + PROPERTY_LOGGER + "=" + loggerClass, e);
        } catch (Exception e) {
            throw new RuntimeException("ThistleSpi: Error while creating logger defined by -D" + PROPERTY_LOGGER + "=" + loggerClass, e);
        }
    }

    private static void loadProperties(ClassLoader classLoader, Logger logger, Map<String, SpiInfo> spiInfos, Map<String, ApplyInfo> applyInfos){

        if (debug) {
            logger.print("Thistle Spi | -------------------------------------------------------------");
            logger.print("Thistle Spi | Loading start, DOC: https://github.com/shepherdviolet/thistle");
        }

        //loading thistle.spi.service

        Enumeration<URL> urls;
        try {
            urls = classLoader.getResources(SERVICE_CONFIG_PATH);
        } catch (Exception e) {
            logger.print("Thistle Spi | ERROR: Error while loading config, " + SERVICE_CONFIG_PATH, e);
            throw new RuntimeException("ThistleSpi: Error while loading config, " + SERVICE_CONFIG_PATH, e);
        }

        if (urls == null || !urls.hasMoreElements()) {
            if (debug) {
                logger.print("Thistle Spi | No " + SERVICE_CONFIG_PATH + " found in classpath");
            }
            return;
        }

        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            String urlStr = String.valueOf(url);

            if (debug) {
                logger.print("Thistle Spi | Loading " + url);
            }

            Properties properties;
            try {
                properties = new Properties();
                properties.load(url.openStream());
            } catch (Exception e) {
                logger.print("Thistle Spi | ERROR: Error while loading config, url" + urlStr, e);
                throw new RuntimeException("ThistleSpi: Error while loading config, url" + urlStr, e);
            }

            String id = properties.getProperty(SERVICE_CONFIG_ID);
            Level level = Level.parse(properties.getProperty(SERVICE_CONFIG_LEVEL));

            if (CheckUtils.isEmptyOrBlank(id)) {
                logger.print("Thistle Spi | ERROR: Illegal config, missing id=?, config:" + urlStr);
                throw new RuntimeException("ThistleSpi: Illegal config, missing id=?, config:" + urlStr);
            }
            if (level == null) {
                logger.print("Thistle Spi | ERROR: Illegal config, missing level=?, config:" + urlStr);
                throw new RuntimeException("ThistleSpi: Illegal config, missing level=?, config:" + urlStr);
            }
            if (level == Level.UNDEFINED) {
                logger.print("Thistle Spi | ERROR: Illegal config, undefined level:" + level + ", should be library/platform/application, config:" + urlStr);
                throw new RuntimeException("ThistleSpi: Illegal config, undefined level:" + level + ", should be library/platform/application, config:" + urlStr);
            }
            if (properties.size() <= 2) {
                if (debug) {
                    logger.print("Thistle Spi | Warning: No service info defined in " + url);
                }
            }

            Enumeration<?> names = properties.propertyNames();
            while (names.hasMoreElements()) {
                String type = String.valueOf(names.nextElement());
                if (SERVICE_CONFIG_ID.equals(type) || SERVICE_CONFIG_LEVEL.equals(type)) {
                    continue;
                }

                SpiInfo spiInfo = spiInfos.get(type);
                if (spiInfo == null) {
                    spiInfo = new SpiInfo();
                    spiInfo.type = type;
                    spiInfos.put(type, spiInfo);
                }

                String implement = properties.getProperty(type);
                if (CheckUtils.isEmptyOrBlank(implement)) {
                    logger.print("Thistle Spi | ERROR: Illegal config, value of " + type + " is empty, config:" + urlStr);
                    throw new RuntimeException("ThistleSpi: Illegal config, value of " + type + " is empty, config:" + urlStr);
                }

                ServiceInfo serviceInfo = new ServiceInfo();
                serviceInfo.id = id;
                serviceInfo.level = level;
                serviceInfo.implement = implement;
                serviceInfo.resource = urlStr;
                spiInfo.definedServices.put(id, serviceInfo);

            }

        }

        //loading thistle.spi.apply

        try {
            urls = classLoader.getResources(APPLY_CONFIG_PATH);
        } catch (Exception e) {
            logger.print("Thistle Spi | ERROR: Error while loading config, " + APPLY_CONFIG_PATH, e);
            throw new RuntimeException("ThistleSpi: Error while loading config, " + APPLY_CONFIG_PATH, e);
        }

        while (urls != null && urls.hasMoreElements()) {
            URL url = urls.nextElement();
            String urlStr = String.valueOf(url);

            if (debug) {
                logger.print("Thistle Spi | loading " + url);
            }

            Properties properties;
            try {
                properties = new Properties();
                properties.load(url.openStream());
            } catch (Exception e) {
                logger.print("Thistle Spi | ERROR: Error while loading config, config" + urlStr, e);
                throw new RuntimeException("ThistleSpi: Error while loading config, config" + urlStr, e);
            }

            if (properties.size() <= 0) {
                if (debug) {
                    logger.print("Thistle Spi | Warning: No apply info defined in " + url);
                }
            }

            Enumeration<?> names = properties.propertyNames();
            while (names.hasMoreElements()) {
                String type = String.valueOf(names.nextElement());
                String id = properties.getProperty(type);
                if (CheckUtils.isEmptyOrBlank(id)) {
                    logger.print("Thistle Spi | ERROR: Illegal config, value of " + type + " is empty, config:" + urlStr);
                    throw new RuntimeException("ThistleSpi: Illegal config, value of " + type + " is empty, config:" + urlStr);
                }

                if (applyInfos.containsKey(type)) {
                    ApplyInfo previous = applyInfos.get(type);
                    if (id.equals(previous.id)){
                        if (debug) {
                            logger.print("Thistle Spi | Warning: Duplicate apply info defined with same value, key:" + type + ", value:" + id + ", url1:" + url + ", url2:" + previous.resource);
                        }
                    } else {
                        //we can use -Dthistle.spi.apply to resolve duplicate error
                        String idFromJvmArgs = System.getProperty(PROPERTY_APPLY_PREFIX + type);
                        String duplicateError = "Duplicate apply info defined with different value, key:" + type + ", value1:" + id + "value2:" + previous.id + ", url1:" + url + ", url2:" + previous.resource;
                        if (CheckUtils.isEmptyOrBlank(idFromJvmArgs)) {
                            //no -Dthistle.spi.apply, throw exception
                            logger.print("Thistle Spi | ERROR: " + duplicateError);
                            throw new RuntimeException("ThistleSpi: " + duplicateError);
                        } else {
                            //try with -Dthistle.spi.apply
                            previous.duplicateError = duplicateError;
                            if (debug) {
                                logger.print("Thistle Spi | Warning: (Try to resolve by -Dthistle.spi.apply)" + duplicateError);
                            }
                        }
                    }
                    continue;
                }

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

        for (SpiInfo spi : spiInfos.values()) {

            String applyId = System.getProperty(PROPERTY_APPLY_PREFIX + spi.type);
            if (!CheckUtils.isEmptyOrBlank(applyId)) {
                ServiceInfo serviceInfo = spi.definedServices.get(applyId);
                if (serviceInfo != null) {
                    spi.appliedService = serviceInfo;
                    spi.applyReason = "-D" + PROPERTY_APPLY_PREFIX + spi.type + "=" + applyId;
                    continue;
                }
                if (debug) {
                    logger.print("Thistle Spi | Warning: No service named " + applyId + ", failed to apply service '" + spi.type + "' with id '" + applyId + "' by -D" + PROPERTY_APPLY_PREFIX + spi.type + "=" + applyId);
                }
            }

            if (applyInfos.containsKey(spi.type)){
                ApplyInfo applyInfo = applyInfos.get(spi.type);
                if (applyInfo.duplicateError != null) {
                    logger.print("Thistle Spi | ERROR: " + applyInfo.duplicateError);
                    throw new RuntimeException("ThistleSpi: " + applyInfo.duplicateError);
                }
                ServiceInfo serviceInfo = spi.definedServices.get(applyInfo.id);
                if (serviceInfo != null) {
                    spi.appliedService = serviceInfo;
                    spi.applyReason = serviceInfo.resource;
                    continue;
                }
                if (debug) {
                    logger.print("Thistle Spi | Warning: No service named " + applyInfo.id + ", failed to apply service '" + spi.type + "' with id '" + applyInfo.id + "' by " + applyInfo.resource);
                    logger.print("Thistle Spi | Warning: We will apply '" + spi.type + "' service by level automatically (application > platform > library)");
                }
            }

            List<ServiceInfo> appliedServices = new ArrayList<>(1);
            int highestPriority = -1;
            for (ServiceInfo serviceInfo : spi.definedServices.values()) {
                if (serviceInfo.level.getPriority() > highestPriority) {
                    appliedServices.clear();
                    appliedServices.add(serviceInfo);
                    highestPriority = serviceInfo.level.getPriority();
                } else if (serviceInfo.level.getPriority() == highestPriority) {
                    appliedServices.add(serviceInfo);
                }
            }

            if (appliedServices.size() <= 0) {
                continue;
            }

            if (appliedServices.size() > 1) {
                StringBuilder stringBuilder = new StringBuilder("Duplicate service defined with same level, type:" + spi.type + ", conflicts:");
                for (ServiceInfo serviceInfo : appliedServices) {
                    stringBuilder.append(serviceInfo);
                    stringBuilder.append("|");
                }
                logger.print("Thistle Spi | ERROR: " + stringBuilder.toString());
                throw new RuntimeException("ThistleSpi: " + stringBuilder.toString());
            }

            spi.appliedService = appliedServices.get(0);
            spi.applyReason = "level automatically (application > platform > library)";

        }

        if (debug) {

            for (SpiInfo spiInfo : spiInfos.values()) {

                logger.print("Thistle Spi | -------------------------------------------------------------");
                logger.print("Thistle Spi | Service:");
                logger.print("Thistle Spi | type: " + spiInfo.type);
                logger.print("Thistle Spi | impl: " + spiInfo.appliedService.implement);
                logger.print("Thistle Spi | url: " + spiInfo.appliedService.resource);
                logger.print("Thistle Spi | reason: Applied by " + spiInfo.applyReason);
                logger.print("Thistle Spi | Definitions:");

                for (ServiceInfo serviceInfo : spiInfo.definedServices.values()) {
                    if (serviceInfo == spiInfo.appliedService) {
                        logger.print("Thistle Spi | >> " + serviceInfo);
                    } else {
                        logger.print("Thistle Spi | -- " + serviceInfo);
                    }
                }

            }

            logger.print("Thistle Spi | -------------------------------------------------------------");
        }

    }

    private static class SpiInfo {

        private String type;
        private ServiceInfo appliedService;
        private String applyReason;
        private Map<String, ServiceInfo> definedServices = new HashMap<>(1);

    }

    private static class ServiceInfo {

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

}
