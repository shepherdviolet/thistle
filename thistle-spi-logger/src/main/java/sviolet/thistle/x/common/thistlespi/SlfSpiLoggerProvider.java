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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ThistleSpi采用SLF4J输出日志(提供者)
 *
 * @author S.Violet
 */
class SlfSpiLoggerProvider implements SpiLogger {

    private static final Logger logger = LoggerFactory.getLogger(SlfSpiLogger.class);

    SlfSpiLoggerProvider() {
    }

    @Override
    public void print(String s) {
        logger.info(s);
    }

    @Override
    public void print(String s, Throwable throwable) {
        logger.error(s, throwable);
    }

}
