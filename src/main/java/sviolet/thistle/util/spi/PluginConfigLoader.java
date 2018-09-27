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

import static sviolet.thistle.util.spi.ThistleSpi.*;

/**
 * 插件配置加载器
 *
 * @author S.Violet
 */
class PluginConfigLoader {

    //启动参数忽略插件
    private static final String PROPERTY_PLUGIN_IGNORE_PREFIX = "thistle.spi.ignore.";

    //插件配置文件名
    private static final String CONFIG_FILE_PLUGIN = "plugin.properties";
    //插件忽略文件名
    private static final String CONFIG_FILE_PLUGIN_IGNORE = "plugin-ignore.properties";

    private ClassLoader classLoader;
    private SpiLogger logger;
    private int loaderId;

    //service配置信息
    private Map<String, PluginInfo> pluginInfos = new HashMap<>(8);

    //apply配置信息
    private Map<String, IgnoreInfo> ignoreInfos = new HashMap<>(8);

    PluginConfigLoader(ClassLoader classLoader, SpiLogger logger, int loaderId) {
        this.classLoader = classLoader;
        this.logger = logger;
        this.loaderId = loaderId;
    }

    /**
     * 设置日志打印器
     */
    void setLogger(SpiLogger logger){
        this.logger = logger;
    }

    /**
     * 清空配置
     */
    void invalidConfig(){
        pluginInfos.clear();
        ignoreInfos.clear();
    }

    /**
     * 加载插件
     */
    <T> List<T> loadPlugins(Class<T> type) {
        if (type == null) {
            return null;
        }

        //类名
        String classname = type.getName();
        //获取插件实现信息
        PluginInfo pluginInfo = pluginInfos.get(classname);

        //不存在插件实现
        if (pluginInfo == null || pluginInfo.orderedPlugins == null) {
            if (loglv >= DEBUG) {
                logger.print(loaderId + LOG_PREFIX_LOADER + "No enabled plugins found, type:" + type.getName());
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
                logger.print(loaderId + LOG_PREFIX_LOADER + "ERROR: Plugin " + pluginInfo.type + " (" + plugin.implement + ") instantiation error, config:" + plugin.resource, e);
                throw new RuntimeException("ThistleSpi: Plugin " + pluginInfo.type + " (" + plugin.implement + ") instantiation error, config:" + plugin.resource, e);
            }
            if (!type.isAssignableFrom(pluginObj.getClass())) {
                logger.print(loaderId + LOG_PREFIX_LOADER + "ERROR: " + plugin.implement + " is not instance of " + pluginInfo.type + ", illegal config:" + plugin.resource);
                throw new RuntimeException("ThistleSpi: " + plugin.implement + " is not instance of " + pluginInfo.type + ", illegal config:" + plugin.resource);
            }

            plugins.add((T) pluginObj);

        }

        if (loglv >= DEBUG) {
            StringBuilder stringBuilder = new StringBuilder(loaderId + LOG_PREFIX_LOADER + "Plugin ");
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

    void loadConfig(String configPath){

        if (loglv >= DEBUG) {
            logger.print(loaderId + LOG_PREFIX + "-------------------------------------------------------------");
            logger.print(loaderId + LOG_PREFIX + "Loading plugins from " + configPath + ", DOC: https://github.com/shepherdviolet/thistle");
        }

        //loading plugin.properties

        //加载所有plugin.properties配置文件
        String pluginConfigFile = configPath + CONFIG_FILE_PLUGIN;
        Enumeration<URL> urls;
        try {
            urls = classLoader.getResources(pluginConfigFile);
        } catch (Exception e) {
            logger.print(loaderId + LOG_PREFIX + "ERROR: Error while loading " + pluginConfigFile, e);
            throw new RuntimeException("ThistleSpi: Error while loading " + pluginConfigFile, e);
        }

        if (urls == null || !urls.hasMoreElements()) {
            if (loglv >= VERBOSE) {
                logger.print(loaderId + LOG_PREFIX + "No " + pluginConfigFile + " found in classpath");
            }
            return;
        }

        //遍历所有plugin.properties配置文件
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            String urlStr = String.valueOf(url);

            if (loglv >= VERBOSE) {
                logger.print(loaderId + LOG_PREFIX + "Loading " + url);
            }

            //装载配置
            Properties properties;
            try {
                properties = new Properties();
                properties.load(url.openStream());
            } catch (Exception e) {
                logger.print(loaderId + LOG_PREFIX + "ERROR: Error while loading config " + urlStr, e);
                throw new RuntimeException("ThistleSpi: Error while loading config " + urlStr, e);
            }

            if (properties.size() <= 0) {
                if (loglv >= DEBUG) {
                    logger.print(loaderId + LOG_PREFIX + "Warning: No properties in " + url);
                }
            }

            //遍历所有key-value
            Enumeration<?> names = properties.propertyNames();
            while (names.hasMoreElements()) {
                String key = String.valueOf(names.nextElement());

                //拆解key
                String[] keyItems = key.split(">");
                if (keyItems.length != 2) {
                    logger.print(loaderId + LOG_PREFIX + "ERROR: Illegal key in config file, key:" + key + ", correct format:interface>priority=impl, config:" + urlStr);
                    throw new RuntimeException("ThistleSpi: Illegal key in config file, key:" + key + ", correct format:interface>priority=impl, config:" + urlStr);
                }

                String type = keyItems[0];
                int priority;
                try {
                    priority = Integer.valueOf(keyItems[1]);
                } catch (Exception e) {
                    logger.print(loaderId + LOG_PREFIX + "ERROR: Illegal config, invalid priority " + keyItems[1] + ", should be integer, in key:" + key + ", config:" + urlStr);
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
                    logger.print(loaderId + LOG_PREFIX + "ERROR: Illegal config, value of " + key + " is empty, config:" + urlStr);
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
            logger.print(loaderId + LOG_PREFIX + "ERROR: Error while loading config " + ignoreConfigFile, e);
            throw new RuntimeException("ThistleSpi: Error while loading config " + ignoreConfigFile, e);
        }

        //遍历所有plugin-ignoreImpl.properties配置文件
        while (urls != null && urls.hasMoreElements()) {
            URL url = urls.nextElement();
            String urlStr = String.valueOf(url);

            if (loglv >= VERBOSE) {
                logger.print(loaderId + LOG_PREFIX + "loading " + url);
            }

            //装载配置文件
            Properties properties;
            try {
                properties = new Properties();
                properties.load(url.openStream());
            } catch (Exception e) {
                logger.print(loaderId + LOG_PREFIX + "ERROR: Error while loading config " + urlStr, e);
                throw new RuntimeException("ThistleSpi: Error while loading config " + urlStr, e);
            }

            if (properties.size() <= 0) {
                if (loglv >= DEBUG) {
                    logger.print(loaderId + LOG_PREFIX + "Warning: No properties in " + url);
                }
            }

            //遍历所有key-value
            Enumeration<?> names = properties.propertyNames();
            while (names.hasMoreElements()) {
                String type = String.valueOf(names.nextElement());
                String ignoreStr = properties.getProperty(type);
                if (CheckUtils.isEmptyOrBlank(ignoreStr)) {
                    logger.print(loaderId + LOG_PREFIX + "ERROR: Illegal config, value of " + type + " is empty, config:" + urlStr);
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
                    if (loglv >= DEBUG && count <= 0) {
                        logger.print(loaderId + LOG_PREFIX + "Warning: Plugin implement " + ignoreImpl + " undefined, failed to ignore implement '" + ignoreImpl + "' of '" + pluginInfo.type + "' by -D" + PROPERTY_PLUGIN_IGNORE_PREFIX + pluginInfo.type + "=" + ignoreStr);
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
                    if (loglv >= DEBUG && count <= 0) {
                        logger.print(loaderId + LOG_PREFIX + "Warning: Plugin implement " + ignore.ignoreImpl + " undefined, failed to ignore implement '" + ignore.ignoreImpl + "' of '" + pluginInfo.type + "' by " + ignore.resource);
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

        if (loglv >= DEBUG) {

            for (PluginInfo pluginInfo : pluginInfos.values()) {

                logger.print(loaderId + LOG_PREFIX + "-------------------------------------------------------------");
                logger.print(loaderId + LOG_PREFIX + "Plugin Applied:");
                logger.print(loaderId + LOG_PREFIX + "  type: " + pluginInfo.type);
                logger.print(loaderId + LOG_PREFIX + "  implements:");
                for (Plugin plugin : pluginInfo.orderedPlugins) {
                    logger.print(loaderId + LOG_PREFIX + "  + " + plugin.toAbstractString());
                }

                if (loglv >= VERBOSE) {
                    logger.print(loaderId + LOG_PREFIX + "All Configurations:");
                    for (Plugin plugin : pluginInfo.plugins) {
                        logger.print(loaderId + LOG_PREFIX + (plugin.enabled ? "  + " : "  - ") + plugin);
                    }
                }

            }

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
