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

package sviolet.thistle.util.common;

import sviolet.thistle.entity.common.Destroyable;

import java.io.Closeable;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Closeable工具
 *
 * @author S.Violet
 */
public class CloseableUtils {

    public static void closeQuiet(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception ignore) {
        }
    }

    public static void closeQuiet(Destroyable destroyable) {
        if (destroyable == null) {
            return;
        }
        try {
            destroyable.onDestroy();
        } catch (Exception ignore) {
        }
    }

    public static void closeIfCloseable(Object obj){
        if (obj == null) {
            return;
        }
        try {
            if (obj instanceof Closeable){
                ((Closeable) obj).close();
            } else if (obj instanceof Destroyable) {
                ((Destroyable) obj).onDestroy();
            }
        } catch (Exception ignore){
        }
    }

    /************************************************************************************************************
     * MappedByteBuffer:注意事项
     * MappedByteBuffer在一些运行时环境中(例如HOTSPOT), 会占用内存并占用文件句柄, 导致文件无法读写删除, 直到对象被GC.
     * 且没有常规办法可以回收资源. ANDROID中不存在此问题.
     * isMappedByteBufferCanClean方法可以判断当前运行环境是否能手动回收MappedByteBuffer, 无法手动回收的运行时环境建
     * 议不要随便使用MappedByteBuffer, 改用传统的IO.
     * cleanMappedByteBuffer方法可以手动回收MappedByteBuffer.
     *
     * MappedByteBuffer是通过下列方法获得的:
     *      FileInputStream inputStream = new FileInputStream(file);
     *      Channel channel = inputStream.getChannel();
     *      MappedByteBuffer byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
     */

    private static final String SUN_MISC_CLEANER = "sun.misc.Cleaner";

    private static volatile int isMappedByteBufferCanClean = -1;
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
            @Override
            public Boolean run() {
                try {
                    // JDK 11 这里要改
                    directByteBufferClass = Class.forName("java.nio.DirectByteBuffer");
                    directByteBufferCleanerMethod = directByteBufferClass.getDeclaredMethod("cleaner");
                    if (directByteBufferCleanerMethod == null){
                        throw new Exception("Method cleaner can not find in java.nio.DirectByteBuffer");
                    }
                    if (!SUN_MISC_CLEANER.equals(directByteBufferCleanerMethod.getReturnType().getName())){
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
    public static boolean cleanMappedByteBuffer(final ByteBuffer byteBuffer) {
        if (byteBuffer instanceof MappedByteBuffer && isMappedByteBufferCanClean()){
            return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                @Override
                public Boolean run() {
                    try {
                        if (!directByteBufferClass.isAssignableFrom(byteBuffer.getClass())){
                            return false;
                        }
                        cleanerCleanMethod.invoke(directByteBufferCleanerMethod.invoke(byteBuffer));
                        return true;
                    } catch (Throwable t) {
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
