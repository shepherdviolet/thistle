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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>内存URL协议(只读)</p>
 * <p>调用registerUrl(String, byte[])方法注册路径和数据后, 可以通过URL协议读取到注册的数据</p>
 * <p></p>
 * <p>示例:</p>
 *
 * <pre>
 *         // 安装 (只要一次)
 *         ReadOnlyMemoryURLStreamHandler readOnlyMemoryURLStreamHandler = new ReadOnlyMemoryURLStreamHandler();
 *         Map<String, URLStreamHandler> urlStreamHandlerMap = new HashMap<>();
 *         urlStreamHandlerMap.put("memory", readOnlyMemoryURLStreamHandler);
 *         MultipleURLStreamHandlerFactory.installHandlers(urlStreamHandlerMap);
 *
 *         // 注册url
 *         readOnlyMemoryURLStreamHandler.registerUrl("/path/file.txt", "test".getBytes(StandardCharsets.UTF_8));
 *
 *         // 使用url获取数据
 *         URL url = new URL("memory:/path/file.txt");
 *         InputStream inputStream = url.openStream();
 * </pre>
 *
 * @author shepherdviolet
 */
public class ReadOnlyMemoryURLStreamHandler extends URLStreamHandler {

    private final Map<String, byte[]> dataMap = new ConcurrentHashMap<>();

    public void registerUrl(String path, byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("data is null");
        }
        dataMap.put(path, data);
    }

    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        return new ReadOnlyMemoryURLConnection(url, dataMap.get(url.getFile()));
    }

    private static class ReadOnlyMemoryURLConnection extends URLConnection {

        private final byte[] data;

        public ReadOnlyMemoryURLConnection(URL url, byte[] data) {
            super(url);
            this.data = data;
        }

        public void connect() throws IOException {
            if (data == null) {
                throw new IOException("ReadOnlyMemoryURLStreamHandler | Url not registered (no such url), url: " + url + ", path:" + url.getFile());
            }
        }

        @Override
        public InputStream getInputStream() throws IOException {
            connect();
            return new ByteArrayInputStream(data);
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            throw new IOException("ReadOnlyMemoryURLStreamHandler | Output (getOutputStream) is not supported");
        }

    }

}
