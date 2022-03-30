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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;

public class ResInJarURLStreamHandlerFactory implements URLStreamHandlerFactory {

    public static final String PROTOCOL = "resinjar";

    public static void install() {
        if (!URLStreamHandlerFactoryInstaller.isProtocolInstalled(PROTOCOL)) {
            URLStreamHandlerFactoryInstaller.setURLStreamHandlerFactory(new URLStreamHandlerFactoryWrapper(new ResInJarURLStreamHandlerFactory()));
        }
    }

    private ResInJarURLStreamHandlerFactory() {
    }

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (PROTOCOL.equals(protocol)) {
            return new ResInJarURLStreamHandler();
        }
        return null;
    }

    public static class ResInJarURLStreamHandler extends URLStreamHandler {

        @Override
        protected URLConnection openConnection(URL url) throws IOException {
            return new ResInJarURLConnection(url);
        }

        private static class ResInJarURLConnection extends URLConnection {

            private URL targetUrl;

            public ResInJarURLConnection(URL url) {
                super(url);
            }

            public void connect() throws IOException {
                if (!this.connected) {
                    try {
                        targetUrl = getClassLoader().getResource(getURL().getPath());
                    } catch (Throwable t){
                        throw new ResourceNotFoundException("ResInJarURLStreamHandlerFactory | Resource '" + getURL() +
                                "' not found in classpath, classloader: " + getClassLoader(), t);
                    }
                    if (targetUrl == null) {
                        throw new ResourceNotFoundException("ResInJarURLStreamHandlerFactory | Resource '" + getURL() +
                                "' not found in classpath, classloader: " + getClassLoader());
                    }
                    this.connected = true;
                }
            }

            @Override
            public InputStream getInputStream() throws IOException {
                this.connect();
                return targetUrl.openStream();
            }

            @Override
            public OutputStream getOutputStream() throws IOException {
                throw new UnknownServiceException("ResInJarURLStreamHandlerFactory | protocol doesn't support output");
            }

            private ClassLoader getClassLoader() {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                if (classLoader == null) {
                    classLoader = ClassLoader.getSystemClassLoader();
                }
                return classLoader;
            }

        }

    }

    public static class ResourceNotFoundException extends IOException {

        private static final long serialVersionUID = 7084349948577031022L;

        public ResourceNotFoundException(String message) {
            super(message);
        }

        public ResourceNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }

    }

}
