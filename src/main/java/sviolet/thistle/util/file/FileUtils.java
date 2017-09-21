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

import sviolet.thistle.util.common.PlatformUtils;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;

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

    /************************************************************************************************************
     * MappedByteBuffer:注意事项
     * MappedByteBuffer在一些运行时环境中(例如HOTSPOT), 会占用内存并占用文件句柄, 导致文件无法读写删除, 直到对象被GC.
     * 且没有常规办法可以回收资源. ANDROID中不存在此问题.
     * isMappedByteBufferCanClean方法可以判断当前运行环境是否能手动回收MappedByteBuffer, 无法手动回收的运行时环境建
     * 议不要随便使用MappedByteBuffer, 改用传统的IO.
     * cleanMappedByteBuffer方法可以手动回收MappedByteBuffer.
     */

    private static int isMappedByteBufferCanClean = -1;
    private static Class<?> directByteBufferClass;
    private static Method directByteBufferCleanerMethod;
    private static Method cleanerCleanMethod;

    /**
     * 判断当前运行环境是否能手动回收MappedByteBuffer, 无法手动回收的运行时建议不要随便使用MappedByteBuffer, 改用传统的IO.
     * @return true:支持手动回收MappedByteBuffer
     */
    public static boolean isMappedByteBufferCanClean(){
        if (isMappedByteBufferCanClean >= 0){
            return isMappedByteBufferCanClean == 0;
        }
        //安卓平台不可用手动回收, 但是不回收也不会存在文件被锁的问题
        if (PlatformUtils.PLATFORM == PlatformUtils.Platform.DALVIK){
            isMappedByteBufferCanClean = 1;
            return false;
        }
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            public Boolean run() {
                try {
                    directByteBufferClass = Class.forName("java.nio.DirectByteBuffer");
                    directByteBufferCleanerMethod = directByteBufferClass.getDeclaredMethod("cleaner");
                    if (directByteBufferCleanerMethod == null){
                        throw new Exception("Method cleaner can not find in java.nio.DirectByteBuffer");
                    }
                    if (!"sun.misc.Cleaner".equals(directByteBufferCleanerMethod.getReturnType().getName())){
                        throw new Exception("Return type of method cleaner is not sun.misc.Cleaner");
                    }
                    directByteBufferCleanerMethod.setAccessible(true);

                    Class<?> cleanerClass = Class.forName("sun.misc.Cleaner");
                    cleanerCleanMethod = cleanerClass.getDeclaredMethod("clean");
                    if (cleanerCleanMethod == null){
                        throw new Exception("Method clean can not find in sun.misc.Cleaner");
                    }
                    cleanerCleanMethod.setAccessible(true);
                    isMappedByteBufferCanClean = 0;
                    return true;
                } catch (Throwable t) {
//                    new Exception("WARNING:This jre environment can not clean MappedByteBuffer manually(1), isMappedByteBufferCanClean = false", t).printStackTrace();
                    System.out.println("WARNING:This jre environment can not clean MappedByteBuffer manually(1), isMappedByteBufferCanClean = false");
                    isMappedByteBufferCanClean = 1;
                    return false;
                }
            }
        });
    }

    /**
     * 手动回收MappedByteBuffer.
     * @return true:回收成功
     */
    public static boolean cleanMappedByteBuffer(final MappedByteBuffer mappedByteBuffer) {
        if (mappedByteBuffer == null){
            return false;
        }
        if (isMappedByteBufferCanClean()){
            return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                public Boolean run() {
                    try {
                        if (!directByteBufferClass.isAssignableFrom(mappedByteBuffer.getClass())){
                            return false;
                        }
                        cleanerCleanMethod.invoke(directByteBufferCleanerMethod.invoke(mappedByteBuffer));
                        return true;
                    } catch (Throwable t) {
//                        new Exception("WARNING:This jre environment can not clean MappedByteBuffer manually(2), isMappedByteBufferCanClean = false", t).printStackTrace();
                        System.out.println("WARNING:This jre environment can not clean MappedByteBuffer manually(2), isMappedByteBufferCanClean = false");
                        isMappedByteBufferCanClean = 1;
                        return false;
                    }
                }
            });
        } else {
            return false;
        }
    }

}
