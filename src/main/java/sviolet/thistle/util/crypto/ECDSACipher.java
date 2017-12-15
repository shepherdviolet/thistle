/*
 * Copyright (C) 2015-2016 S.Violet
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

package sviolet.thistle.util.crypto;

import sviolet.thistle.util.common.PlatformUtils;
import sviolet.thistle.util.file.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;

/**
 * <p>ECDSA签名验签工具</p>
 *
 * <p>Cipher/Signature/MessageDigest线程不安全!!!</p>
 *
 * <p>
 * 性能测试:
 * 安卓单线程/secp256r1
 * 密钥产生:0.3ms 签名:0.8ms 验签:1.7ms
 * </p>
 *
 */
public class ECDSACipher {

    public static final String SIGN_ALGORITHM_ECDSA_SHA256 = "SHA256withECDSA";

    /**
     * @param privateKey 私钥
     * @param signAlgorithm 签名逻辑: ECDSACipher.SIGN_ALGORITHM_ECDSA_SHA256
     * @throws NoSuchAlgorithmException 无效的signAlgorithm
     * @throws InvalidKeyException 无效的私钥
     */
    public static Signature generateECDSASignatureInstance(ECPrivateKey privateKey, String signAlgorithm) throws NoSuchAlgorithmException, InvalidKeyException {
        Signature signature = Signature.getInstance(signAlgorithm);
        signature.initSign(privateKey);
        return signature;
    }

    /**
     * 用私钥对信息生成数字签名<p>
     *  
     * @param data 需要签名的数据
     * @param privateKey 私钥
     * @param signAlgorithm 签名逻辑: ECDSACipher.SIGN_ALGORITHM_ECDSA_SHA256
     *  
     * @return 数字签名
     * @throws NoSuchAlgorithmException 无效的signAlgorithm
     * @throws InvalidKeyException 无效的私钥
     * @throws SignatureException 签名异常
     */  
    public static byte[] sign(byte[] data, ECPrivateKey privateKey, String signAlgorithm) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException{
        Signature signature = generateECDSASignatureInstance(privateKey, signAlgorithm);
        signature.update(data);
        return signature.sign();
    }

    /**
     * <p>用私钥对信息生成数字签名, 根据运行时环境选择使用NIO或IO方式</p>
     *
     * @param file 需要签名的文件
     * @param privateKey 私钥
     * @param signAlgorithm 签名逻辑: RSACipher.SIGN_ALGORITHM_RSA_MD5 / RSACipher.SIGN_ALGORITHM_RSA_SHA1
     *
     * @return 数字签名
     * @throws NoSuchAlgorithmException 无效的signAlgorithm
     * @throws InvalidKeyException 无效的私钥
     * @throws SignatureException 签名异常
     */
    public static byte[] sign(File file, ECPrivateKey privateKey, String signAlgorithm) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException {
        if (PlatformUtils.PLATFORM == PlatformUtils.Platform.DALVIK){
            //安卓API11以上使用NIO, API10以下会很慢
            if (PlatformUtils.ANDROID_VERSION < 11){
                return signIo(file, privateKey, signAlgorithm);
            } else {
                return signNio(file, privateKey, signAlgorithm);
            }
        }
        //能手动回收MappedByteBuffer则使用NIO
        if (FileUtils.isMappedByteBufferCanClean()){
            return signNio(file, privateKey, signAlgorithm);
        } else {
            return signIo(file, privateKey, signAlgorithm);
        }
    }

    /**
     * <p>用私钥对信息生成数字签名(NIO)</p>
     *
     * 注意:非安卓平台使用该方法前, 请使用FileUtils.isMappedByteBufferCanClean()判断MappedByteBuffer是否能被手动回收,
     * 如果isMappedByteBufferCanClean返回false, 建议使用signIo, 否则操作后, 文件将在一段时间内无法被读写删除<br/>
     *
     * 注意:安卓平台API11以上使用, API10以下会很慢<br/>
     *
     * @param file 需要签名的文件
     * @param privateKey 私钥
     * @param signAlgorithm 签名逻辑: ECDSACipher.SIGN_ALGORITHM_ECDSA_SHA256
     *
     * @return 数字签名
     * @throws NoSuchAlgorithmException 无效的signAlgorithm
     * @throws InvalidKeyException 无效的私钥
     * @throws SignatureException 签名异常
     */
    public static byte[] signNio(File file, ECPrivateKey privateKey, String signAlgorithm) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException {
        FileInputStream inputStream = null;
        FileChannel channel = null;
        MappedByteBuffer byteBuffer = null;
        try {
            inputStream = new FileInputStream(file);
            channel = inputStream.getChannel();
            byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            Signature signature = generateECDSASignatureInstance(privateKey, signAlgorithm);
            signature.update(byteBuffer);
            return signature.sign();
        } finally {
            if (inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
            if (channel != null){
                try {
                    channel.close();
                } catch (IOException ignored) {
                }
            }
            //尝试将MappedByteBuffer回收, 解决后续文件无法被读写删除的问题
            FileUtils.cleanMappedByteBuffer(byteBuffer);
        }
    }

    /**
     * <p>用私钥对信息生成数字签名(IO)</p>
     *
     * @param file 需要签名的文件
     * @param privateKey 私钥
     * @param signAlgorithm 签名逻辑: ECDSACipher.SIGN_ALGORITHM_ECDSA_SHA256
     *
     * @return 数字签名
     * @throws NoSuchAlgorithmException 无效的signAlgorithm
     * @throws InvalidKeyException 无效的私钥
     * @throws SignatureException 签名异常
     */
    public static byte[] signIo(File file, ECPrivateKey privateKey, String signAlgorithm) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            Signature signature = generateECDSASignatureInstance(privateKey, signAlgorithm);
            byte[] buff = new byte[1024];
            int size;
            while((size = inputStream.read(buff)) != -1){
                signature.update(buff, 0, size);
            }
            return signature.sign();
        } finally {
            if (inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * <p>用公钥验证数字签名</p>
     *  
     * @param data 被签名的数据
     * @param sign 数字签名
     * @param publicKey 公钥
     * @param signAlgorithm 签名逻辑: ECDSACipher.SIGN_ALGORITHM_ECDSA_SHA256
     *  
     * @return true:数字签名有效
     * @throws NoSuchAlgorithmException 无效的signAlgorithm
     * @throws InvalidKeyException 无效的私钥
     * @throws SignatureException 签名异常
     *  
     */  
    public static boolean verify(byte[] data, byte[] sign, ECPublicKey publicKey, String signAlgorithm) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException{
        Signature signature = Signature.getInstance(signAlgorithm);
        signature.initVerify(publicKey);
        signature.update(data);
        return signature.verify(sign);  
    }

    /**
     * <p>用公钥验证数字签名, 根据运行时环境选择使用NIO或IO方式</p>
     *
     * @param file 被签名的文件
     * @param sign 数字签名
     * @param publicKey 公钥
     * @param signAlgorithm 签名逻辑: RSACipher.SIGN_ALGORITHM_RSA_MD5 / RSACipher.SIGN_ALGORITHM_RSA_SHA1
     *
     * @return true:数字签名有效
     * @throws NoSuchAlgorithmException 无效的signAlgorithm
     * @throws InvalidKeyException 无效的私钥
     * @throws SignatureException 签名异常
     */
    public static boolean verify(File file, byte[] sign, ECPublicKey publicKey, String signAlgorithm) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException {
        if (PlatformUtils.PLATFORM == PlatformUtils.Platform.DALVIK){
            //安卓API11以上使用NIO, API10以下会很慢
            if (PlatformUtils.ANDROID_VERSION < 11){
                return verifyIo(file, sign, publicKey, signAlgorithm);
            } else {
                return verifyNio(file, sign, publicKey, signAlgorithm);
            }
        }
        //能手动回收MappedByteBuffer则使用NIO
        if (FileUtils.isMappedByteBufferCanClean()){
            return verifyNio(file, sign, publicKey, signAlgorithm);
        } else {
            return verifyIo(file, sign, publicKey, signAlgorithm);
        }
    }

    /**
     * <p>用公钥验证数字签名(NIO)</p>
     *
     * 注意:非安卓平台使用该方法前, 请使用FileUtils.isMappedByteBufferCanClean()判断MappedByteBuffer是否能被手动回收,
     * 如果isMappedByteBufferCanClean返回false, 建议使用signIo, 否则操作后, 文件将在一段时间内无法被读写删除<br/>
     *
     * 注意:安卓平台API11以上使用, API10以下会很慢<br/>
     *
     * @param file 被签名的文件
     * @param sign 数字签名
     * @param publicKey 公钥
     * @param signAlgorithm 签名逻辑: ECDSACipher.SIGN_ALGORITHM_ECDSA_SHA256
     *
     * @return true:数字签名有效
     * @throws NoSuchAlgorithmException 无效的signAlgorithm
     * @throws InvalidKeyException 无效的私钥
     * @throws SignatureException 签名异常
     *
     */
    public static boolean verifyNio(File file, byte[] sign, ECPublicKey publicKey, String signAlgorithm) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException {
        FileInputStream inputStream = null;
        FileChannel channel = null;
        MappedByteBuffer byteBuffer = null;
        try {
            inputStream = new FileInputStream(file);
            channel = inputStream.getChannel();
            byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            Signature signature = Signature.getInstance(signAlgorithm);
            signature.initVerify(publicKey);
            signature.update(byteBuffer);
            return signature.verify(sign);
        } finally {
            if (inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
            if (channel != null){
                try {
                    channel.close();
                } catch (IOException ignored) {
                }
            }
            //尝试将MappedByteBuffer回收, 解决后续文件无法被读写删除的问题
            FileUtils.cleanMappedByteBuffer(byteBuffer);
        }
    }

    /**
     * <p>用公钥验证数字签名(IO)</p>
     *
     * @param file 被签名的文件
     * @param sign 数字签名
     * @param publicKey 公钥
     * @param signAlgorithm 签名逻辑: ECDSACipher.SIGN_ALGORITHM_ECDSA_SHA256
     *
     * @return true:数字签名有效
     * @throws NoSuchAlgorithmException 无效的signAlgorithm
     * @throws InvalidKeyException 无效的私钥
     * @throws SignatureException 签名异常
     *
     */
    public static boolean verifyIo(File file, byte[] sign, ECPublicKey publicKey, String signAlgorithm) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            Signature signature = Signature.getInstance(signAlgorithm);
            signature.initVerify(publicKey);
            byte[] buff = new byte[1024];
            int size;
            while((size = inputStream.read(buff)) != -1){
                signature.update(buff, 0, size);
            }
            return signature.verify(sign);
        } finally {
            if (inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

}
