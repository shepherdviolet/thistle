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
 * Project GitHub: https://github.com/shepherdviolet/turquoise
 * Email: shepherdviolet@163.com
 */

package sviolet.thistle.util.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 文件工具
 *
 * Created by S.Violet on 2017/7/27.
 */

public class FileUtils {

    /**
     * 向文件写入字符串
     * @param file 文件
     * @param msg 字符串
     * @param append true:追加 false:覆盖
     */
    public static void writeString(File file, String msg, boolean append) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file, append));
            writer.write(msg);
        } finally {
            try { if (writer != null) writer.flush(); } catch (IOException ignored) { }
            try { if (writer != null) writer.close(); } catch (IOException ignored) { }
        }
    }

}
