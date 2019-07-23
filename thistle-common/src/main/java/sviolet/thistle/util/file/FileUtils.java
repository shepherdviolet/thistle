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

package sviolet.thistle.util.file;

import sviolet.thistle.util.common.CloseableUtils;

import java.io.*;
import java.nio.ByteBuffer;

/**
 * 文件工具
 *
 * @author S.Violet
 */
public class FileUtils {

    /**
     * 向文件写入字符串
     * @param file 文件
     * @param msg 字符串
     * @param append true:追加 false:覆盖
     */
    public static void writeString(File file, String msg, boolean append) throws IOException {
        writeString(file, msg, "utf-8", append);
    }

    /**
     * 向文件写入字符串
     * @param file 文件
     * @param msg 字符串
     * @param charset 字符编码
     * @param append true:追加 false:覆盖
     */
    public static void writeString(File file, String msg, String charset, boolean append) throws IOException {
        File dirFile = file.getParentFile();
        if (dirFile != null && !dirFile.exists()){
            if (!dirFile.mkdirs()){
                throw new IOException("Can not make directory before write string to file, path:" + dirFile.getAbsolutePath());
            }
        }
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append), charset));
            writer.write(msg);
        } finally {
            try { if (writer != null) {writer.flush();} } catch (IOException ignored) { }
            try { if (writer != null) {writer.close();} } catch (IOException ignored) { }
        }
    }

    /**
     * 读取整个文件(字符串形式), 仅限于读取小文件, 小心OOM
     * @param file 文件
     * @param maxLength 最大长度, 如果文件大小超过该设定值, 会抛出异常
     * @return 字符串
     */
    public static String readString(File file, int maxLength) throws LengthOutOfLimitException, IOException {
        return readString(file, "utf-8", maxLength);
    }

    /**
     * 读取整个文件(字符串形式), 仅限于读取小文件, 小心OOM
     * @param file 文件
     * @param charset 字符编码
     * @param maxLength 最大长度, 如果文件大小超过该设定值, 会抛出异常
     * @return 字符串
     */
    public static String readString(File file, String charset, int maxLength) throws LengthOutOfLimitException, IOException {

        if (!file.exists() || !file.isFile()) {
            throw new FileNotFoundException("File not found, path:" + file.getAbsolutePath());
        }

        if (file.length() > maxLength){
            throw new LengthOutOfLimitException("File length out of limit, file:" + file.getAbsolutePath() + ", length:" + file.length());
        }

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
            StringBuilder stringBuilder = new StringBuilder();
            char[] buff = new char[1024];
            int length;
            while ((length = bufferedReader.read(buff)) >= 0) {
                stringBuilder.append(buff, 0, length);
            }
            return stringBuilder.toString();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception ignore){
                }
            }
        }
    }

    /**
     * 读取文件的头部数据
     * @param file 文件
     * @param start 起始位置
     * @param buffer 获取的数据
     * @throws IOException exception
     */
    public static long readHead(File file, int start, byte[] buffer) throws IOException {
        if (file == null || !file.exists()){
            return 0;
        }
        RandomAccessFile randomAccessFile = null;
        try{
            randomAccessFile = new RandomAccessFile(file, "r");
            randomAccessFile.seek(start);
            return randomAccessFile.read(buffer);
        } finally {
            if (randomAccessFile != null){
                try {
                    randomAccessFile.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * 判断当前运行环境是否能手动回收MappedByteBuffer, 无法手动回收的运行时建议不要随便使用MappedByteBuffer, 改用传统的IO.
     * @return true:支持手动回收MappedByteBuffer
     */
    public static boolean isMappedByteBufferCanClean(){
        return CloseableUtils.isMappedByteBufferCanClean();
    }

    /**
     * 手动回收MappedByteBuffer.
     * @return true:回收成功
     */
    public static boolean cleanMappedByteBuffer(ByteBuffer byteBuffer) {
        return CloseableUtils.cleanMappedByteBuffer(byteBuffer);
    }

    /****************************************************************************************************
     * Exceptions
     */

    public static class LengthOutOfLimitException extends Exception {
        public LengthOutOfLimitException(String message) {
            super(message);
        }
    }

}
