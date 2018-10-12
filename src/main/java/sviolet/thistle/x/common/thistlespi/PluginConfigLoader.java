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

import java.net.URL;
import java.util.*;

import static sviolet.thistle.x.common.thistlespi.ThistleSpi.*;

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

    private static final int MAX_INFO_LOG_LINES = 10;

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
            if (LOG_LV >= INFO) {
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
                pluginObj = Utils.newInstance(clazz, plugin.arg);
            } catch (Exception e) {
                logger.print(loaderId + LOG_PREFIX_LOADER + "ERROR: Plugin " + pluginInfo.type + " (" + plugin.implement + ") instantiation error, config:" + plugin.resource, e);
                throw new RuntimeException("ThistleSpi: Plugin " + pluginInfo.type + " (" + plugin.implement + ") instantiation error, config:" + plugin.resource, e);
            }
            if (!type.isAssignableFrom(pluginObj.getClass())) {
                RuntimeException e = new RuntimeException("ThistleSpi: " + plugin.implement + " is not instance of " + pluginInfo.type + ", illegal config:" + plugin.resource);
                logger.print(loaderId + LOG_PREFIX_LOADER + "ERROR: " + plugin.implement + " is not instance of " + pluginInfo.type + ", illegal config:" + plugin.resource, e);
                throw e;
            }

            plugins.add((T) pluginObj);

        }

        if (LOG_LV >= INFO) {
            StringBuilder stringBuilder = new StringBuilder(loaderId + LOG_PREFIX_LOADER + "Plugin ");
            stringBuilder.append(pluginInfo.type);
            stringBuilder.append(" (");
            int i = 0;
            for (Plugin plugin : pluginInfo.orderedPlugins) {
                if (LOG_LV < DEBUG && i++ >= MAX_INFO_LOG_LINES) {
                    stringBuilder.append(" ... ");
                    stringBuilder.append(pluginInfo.orderedPlugins.size() - MAX_INFO_LOG_LINES);
                    stringBuilder.append(" more");
                    break;
                }
                stringBuilder.append(" ");
                stringBuilder.append(plugin.implement);
            }
            stringBuilder.append(" ) loaded successfully");
            logger.print(stringBuilder.toString());
        }

        return plugins;
    }

    void loadConfig(String configPath){

        if (LOG_LV >= INFO) {
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
            if (LOG_LV >= DEBUG) {
                logger.print(loaderId + LOG_PREFIX + "No " + pluginConfigFile + " found in classpath");
            }
            return;
        }

        //遍历所有plugin.properties配置文件
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            String urlStr = String.valueOf(url);

            if (LOG_LV >= DEBUG) {
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
                if (LOG_LV >= INFO) {
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
                    RuntimeException e = new RuntimeException("ThistleSpi: Illegal key in config file, key:" + key + ", correct format:interface>priority=impl, config:" + urlStr);
                    logger.print(loaderId + LOG_PREFIX + "ERROR: Illegal key in config file, key:" + key + ", correct format:interface>priority=impl, config:" + urlStr, e);
                    throw e;
                }

                String type = keyItems[0];
                int priority;
                try {
                    priority = Integer.valueOf(keyItems[1]);
                } catch (Exception e) {
                    logger.print(loaderId + LOG_PREFIX + "ERROR: Illegal config, invalid priority " + keyItems[1] + ", should be integer, in key:" + key + ", config:" + urlStr, e);
                    throw new RuntimeException("ThistleSpi: Illegal config, invalid priority " + keyItems[1] + ", should be integer, in key:" + key + ", config:" + urlStr, e);
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
                    RuntimeException e = new RuntimeException("ThistleSpi: Illegal config, value of " + key + " is empty, config:" + urlStr);
                    logger.print(loaderId + LOG_PREFIX + "ERROR: Illegal config, value of " + key + " is empty, config:" + urlStr, e);
                    throw e;
                }

                //获取构造参数
                String arg = null;
                int argStart = implement.indexOf("(");
                //value第一个字符就是(, 非法
                if (argStart == 0) {
                    RuntimeException e = new RuntimeException("ThistleSpi: Illegal config, value of " + key + " starts with (, config:" + urlStr);
                    logger.print(loaderId + LOG_PREFIX + "ERROR: Illegal config, value of " + key + " starts with (, config:" + urlStr, e);
                    throw e;
                }
                //存在(字符, 尝试截取构造参数
                if (argStart > 0) {
                    //value最后一个字符不是), 非法
                    if (')' != implement.charAt(implement.length() - 1)) {
                        RuntimeException e = new RuntimeException("ThistleSpi: Illegal config, value of " + key + " has ( but no ) at last, config:" + urlStr);
                        logger.print(loaderId + LOG_PREFIX + "ERROR: Illegal config, value of " + key + " has ( but no ) at last, config:" + urlStr, e);
                        throw e;
                    }
                    arg = implement.substring(argStart + 1);
                    implement = implement.substring(0, argStart);
                }

                //服务接口信息
                Plugin plugin = new Plugin();
                plugin.priority = priority;
                plugin.implement = implement;
                plugin.arg = arg;
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

            if (LOG_LV >= DEBUG) {
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
                if (LOG_LV >= INFO) {
                    logger.print(loaderId + LOG_PREFIX + "Warning: No properties in " + url);
                }
            }

            //遍历所有key-value
            Enumeration<?> names = properties.propertyNames();
            while (names.hasMoreElements()) {
                String type = String.valueOf(names.nextElement());
                String ignoreStr = properties.getProperty(type);
                if (CheckUtils.isEmptyOrBlank(ignoreStr)) {
                    RuntimeException e = new RuntimeException("ThistleSpi: Illegal config, value of " + type + " is empty, config:" + urlStr);
                    logger.print(loaderId + LOG_PREFIX + "ERROR: Illegal config, value of " + type + " is empty, config:" + urlStr, e);
                    throw e;
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
                    if (LOG_LV >= INFO && count <= 0) {
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
                    if (LOG_LV >= INFO && count <= 0) {
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

        if (LOG_LV >= INFO) {

            for (PluginInfo pluginInfo : pluginInfos.values()) {

                logger.print(loaderId + LOG_PREFIX + "-------------------------------------------------------------");
                logger.print(loaderId + LOG_PREFIX + "Plugin Applied:");
                logger.print(loaderId + LOG_PREFIX + "  type: " + pluginInfo.type);
                logger.print(loaderId + LOG_PREFIX + "  implements:");

                int i = 0;
                for (Plugin plugin : pluginInfo.orderedPlugins) {
                    if (LOG_LV < DEBUG && i++ >= MAX_INFO_LOG_LINES) {
                        logger.print(loaderId + LOG_PREFIX + "    ...... " + (pluginInfo.orderedPlugins.size() - MAX_INFO_LOG_LINES) +
                                " more omitted ('-D" + ThistleSpi.PROPERTY_LOGLV + "=debug' to show more)");
                        break;
                    }
                    logger.print(loaderId + LOG_PREFIX + "  + " + plugin.toAbstractString());
                }

                if (LOG_LV >= DEBUG) {
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
        private String arg;
        private String resource;
        private boolean enabled = true;
        private String disableReason;

        public String toAbstractString(){
            return "Plugin{" +
                    "priority=" + priority +
                    ", impl=" + implement +
                    ", arg=" + arg +
                    '}';
        }

        @Override
        public String toString() {
            return "Plugin{" +
                    "priority=" + priority +
                    ", impl=" + implement +
                    ", arg=" + arg +
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
