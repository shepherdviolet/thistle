/*
 * Copyright (C) 2015-2017 S.Violet
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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 网络工具
 */
public class NetworkUtils {

    /**
     * 模拟TELNET
     * @param hostname IP或域名, 例如: 61.135.169.125或www.baidu.com
     * @param port 端口
     * @param timeout 探测超时ms
     * @return true:成功 false:失败
     */
    public static boolean telnet(String hostname, int port, int timeout) {
        Socket socket = null;
        try {
            socket = new Socket();
            InetSocketAddress address = new InetSocketAddress(hostname, port);
            socket.connect(address, timeout);
            return true;
        } catch (Throwable ignore) {
            return false;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

}
