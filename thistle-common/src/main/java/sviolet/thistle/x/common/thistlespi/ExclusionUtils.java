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

import sviolet.thistle.util.conversion.ByteUtils;
import sviolet.thistle.util.crypto.DigestCipher;

import java.io.IOException;
import java.net.URL;

import static sviolet.thistle.x.common.thistlespi.Constants.*;
import static sviolet.thistle.x.common.thistlespi.Constants.LOG_PREFIX;

/**
 * 排除相关
 *
 * @author S.Violet
 */
class ExclusionUtils {

    /**
     * 检查配置文件是否被强制排除, true: exclude false: pass
     * @param url 配置文件的URL
     * @param logger (日志相关)日志打印器
     * @param loaderId (日志相关)加载器ID
     */
    static boolean checkFileExclusion(URL url, SpiLogger logger, int loaderId) throws IOException {
        // check hash if log-lv is debug or any exclusion set
        if (LOG_LV >= DEBUG || FILE_EXCLUSION_SET.size() > 0) {
            //md5
            String hash = ByteUtils.bytesToHex(DigestCipher.digestInputStream(url.openStream(), DigestCipher.TYPE_MD5));
            //check
            if (FILE_EXCLUSION_SET.contains(hash)) {
                if (LOG_LV >= INFO) {
                    logger.print(loaderId + LOG_PREFIX + "!!! Exclude config " + url + " by -D" + STARTUP_PROP_FILE_EXCLUSION);
                }
                return true;
            }
            logger.print(loaderId + LOG_PREFIX + "Loading config " + url + " <hash> " + hash);
        }
        return false;
    }

}
