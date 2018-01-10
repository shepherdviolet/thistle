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

package sviolet.thistle.util.crypto;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

/**
 * <p>密钥生成基本逻辑</p>
 *
 * <p>Cipher/Signature/MessageDigest线程不安全!!!</p>
 *
 * @author S.Violet
 *
 */
class BaseKeyGenerator {

    /**
     * linux中secureRandom会从/dev/urandom中获取内核熵, 相对比普通种子安全
     */
    private static SecureRandom secureRandom = new SecureRandom();

    /**
     * <p>生成对称密钥, 用于服务端场合</p>
     *
     * @param secureRandom SecureRandom是线程安全的, 服务端通常使用一个单例的SecureRandom
     * @param bits 秘钥位数(64/128/192/256...)
     * @param keyAlgorithm 秘钥算法类型
     * @return 秘钥
     */
    public static byte[] generateKey(SecureRandom secureRandom, int bits, String keyAlgorithm) throws NoSuchProviderException, NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(keyAlgorithm);
        keyGenerator.init(bits, secureRandom != null ? secureRandom : BaseKeyGenerator.secureRandom);
        SecretKey secretKey = keyGenerator.generateKey();
        return secretKey.getEncoded();
    }

    /**
     * <p>生成对称密钥(不同系统平台相同seed生成结果可能不同), Android使用该方法, 相同seed仍会产生随机秘钥</p>
     *
     * @param seed 秘钥种子
     * @param bits 秘钥位数(64/128/192/256...)
     * @param keyAlgorithm 秘钥算法类型
     * @return 秘钥
     */
    public static byte[] generateKey(byte[] seed, int bits, String keyAlgorithm) throws NoSuchProviderException, NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(keyAlgorithm);
        SecureRandom secureRandom = new SecureRandom(seed);
        keyGenerator.init(bits, secureRandom);
        SecretKey secretKey = keyGenerator.generateKey();
        return secretKey.getEncoded();
    }

    /**
     * 利用SHA256摘要算法计算128位固定密钥, 安全性低, 但保证全平台一致
     *
     * @param seed 密码种子
     */
    public static byte[] generateShaKey64(byte[] seed){
        byte[] sha = DigestCipher.digest(seed, DigestCipher.TYPE_SHA256);
        byte[] password = new byte[8];
        System.arraycopy(sha, 0, password, 0, password.length);
        return password;
    }

    /**
     * 利用SHA256摘要算法计算128位固定密钥, 安全性低, 但保证全平台一致
     *
     * @param seed 密码种子
     */
    public static byte[] generateShaKey128(byte[] seed){
        byte[] sha = DigestCipher.digest(seed, DigestCipher.TYPE_SHA256);
        byte[] password = new byte[16];
        System.arraycopy(sha, 0, password, 0, password.length);
        return password;
    }

    /**
     * 利用SHA256摘要算法计算192位固定密钥, 安全性低, 但保证全平台一致
     *
     * @param seed 密码种子
     */
    public static byte[] generateShaKey192(byte[] seed){
        byte[] sha = DigestCipher.digest(seed, DigestCipher.TYPE_SHA256);
        byte[] password = new byte[24];
        System.arraycopy(sha, 0, password, 0, password.length);
        return password;
    }

    /**
     * 利用SHA256摘要算法计算256位固定密钥, 安全性低, 但保证全平台一致
     *
     * @param seed 密码种子
     */
    public static byte[] generateShaKey256(byte[] seed){
        return DigestCipher.digest(seed, DigestCipher.TYPE_SHA256);
    }

}
