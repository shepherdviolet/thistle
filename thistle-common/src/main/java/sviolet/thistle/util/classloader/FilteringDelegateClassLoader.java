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

package sviolet.thistle.util.classloader;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>过滤部分类加载不走双亲委派模型</p>
 *
 * <p>示例:</p>
 *
 * <pre>
 *         // 加载classpath下的jar包, 当类名是org.bouncycastle开头时, 不走双亲委派模型, 直接由本ClassLoader加载类
 *         ClassLoader classLoader = new FilteringDelegateClassLoader(new URL[]{
 *                 getClass().getClassLoader().getResource("bcpkix-jdk15on-1.60.jar"),
 *                 getClass().getClassLoader().getResource("bcprov-jdk15on-1.60.jar")
 *         }, Main.class.getClassLoader()) {
 *             protected boolean isClassDelegatedByParent(String name) {
 *                 return !name.startsWith("org.bouncycastle");
 *             }
 *         };
 *</pre>
 */
public abstract class FilteringDelegateClassLoader extends URLClassLoader {

    private final Set<String> filteredClasses = new HashSet<>();

    public FilteringDelegateClassLoader(URL[] urls) {
        super(urls);
    }

    public FilteringDelegateClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public FilteringDelegateClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // 过滤类名
        if (!isClassDelegatedByParent(name)) {
            // 不走双亲委派模型, 直接由本ClassLoader加载类
            synchronized (getClassLoadingLock(name)) {
                try {
                    Class<?> c = findLoadedClass(name);
                    if (c == null) {
                        c = findClass(name);
                    }
                    if (resolve) {
                        resolveClass(c);
                    }
                    filteredClasses.add(name);
                    return c;
                } catch (ClassNotFoundException ignore) {
                    // load from parent if not found
                }
            }
        }
        return super.loadClass(name, resolve);
    }

    @Override
    public URL getResource(String name) {
        return super.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return super.getResources(name);
    }

    protected abstract boolean isClassDelegatedByParent(String name);

    @Override
    public String toString() {
        return "FilteringDelegateClassLoader{" +
                "filteredClasses=" + filteredClasses +
                '}';
    }

}
