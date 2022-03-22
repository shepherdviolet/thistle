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

/**
 * <p>URL协议扩展 URLStreamHandlerFactory安装器</p>
 * <p>默认情况下, URL.setURLStreamHandlerFactory(factory)只能被调用一次. 我们可以使用本安装器解决这个问题. </p>
 * <p>本安装器利用反射, 将URLStreamHandlerFactory强制替换为我们自定义的工厂, 同时利用ParentAwareURLStreamHandlerFactory保持原有工厂不受影响.</p>
 * <p></p>
 * <p>使用方法:</p>
 * <p>URLStreamHandlerFactoryInstaller.setURLStreamHandlerFactory(new URLStreamHandlerFactoryWrapper(yourFactory));</p>
 *
 * @author http://svn.apache.org/repos/asf/commons/sandbox/jnet/trunk/src/main/java/org/apache/commons/jnet
 */
package sviolet.thistle.util.urlext.installer;