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

import sviolet.thistle.util.conversion.DateTimeUtils;

/**
 * ThistleSpi默认日志实现
 *
 * @author S.Violet
 */
public class DefaultSpiLogger implements SpiLogger {

    @Override
    public final void print(String msg) {
        System.out.println(DateTimeUtils.getDateTime() + " " + msg);
    }

    @Override
    public final void print(String msg, Throwable throwable) {
        System.out.println(DateTimeUtils.getDateTime() + " " + msg);
        if (throwable != null) {
            throwable.printStackTrace();
        }
    }

}
