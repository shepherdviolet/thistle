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

/**
 * ThistleSpi采用SLF4J输出日志
 *
 * @author S.Violet
 */
public class SlfSpiLogger implements SpiLogger {

    static {
        // 若org.slf4j.Logger类存在则使用slf4j输出日志
        try {
            Class.forName("org.slf4j.Logger");
            provider = (SpiLogger) Class.forName("sviolet.slate.common.x.common.thistlespi.SlfSpiLoggerProvider").newInstance();
        } catch (Exception e) {
            // 否则使用默认方式输出日志
            provider = new DefaultSpiLogger();
        }
    }

    private static SpiLogger provider;

    public SlfSpiLogger() {
    }

    @Override
    public void print(String s) {
        provider.print(s);
    }

    @Override
    public void print(String s, Throwable throwable) {
        provider.print(s, throwable);
    }

}
