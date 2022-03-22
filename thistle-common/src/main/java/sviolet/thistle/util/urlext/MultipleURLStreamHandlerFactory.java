/*
 * Copyright (C) 2015-2022 S.Violet
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

package sviolet.thistle.util.urlext;

import sviolet.thistle.util.urlext.installer.URLStreamHandlerFactoryInstaller;
import sviolet.thistle.util.urlext.installer.URLStreamHandlerFactoryWrapper;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Map;

/**
 * <p>URL协议扩展 多协议工厂</p>
 * <p>安装一个URLStreamHandlerFactory, 内置多种协议, 根据协议名路由. 可以安装多次, 但是建议一次性安装(建议只调用一次installHandlers
 * 将所有需要的协议都设置好). </p>
 * <p></p>
 * <p>使用方法:</p>
 * <p>MultipleURLStreamHandlerFactory.installHandlers(yourHandlers);</p>
 *
 * @author shepherdviolet
 */
public class MultipleURLStreamHandlerFactory implements URLStreamHandlerFactory {

    public static void installHandlers(Map<String, URLStreamHandler> urlStreamHandlerMap) {
        if (urlStreamHandlerMap == null || urlStreamHandlerMap.size() == 0) {
            return;
        }
        URLStreamHandlerFactoryInstaller.setURLStreamHandlerFactory(new URLStreamHandlerFactoryWrapper(
                new MultipleURLStreamHandlerFactory(urlStreamHandlerMap)));
    }

    public final Map<String, URLStreamHandler> urlStreamHandlerMap;

    private MultipleURLStreamHandlerFactory(Map<String, URLStreamHandler> urlStreamHandlerMap) {
        this.urlStreamHandlerMap = urlStreamHandlerMap;
    }

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (urlStreamHandlerMap != null) {
            return urlStreamHandlerMap.get(protocol);
        }
        return null;
    }

}
