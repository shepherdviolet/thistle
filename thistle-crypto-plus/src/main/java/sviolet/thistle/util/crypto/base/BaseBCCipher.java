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

package sviolet.thistle.util.crypto.base;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import sviolet.thistle.util.common.CloseableUtils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.*;

/**
 * 加解密基本逻辑<p>
 *
 * Not recommended for direct use<p>
 *
 * 不建议直接使用<p>
 *
 * Cipher/Signature/MessageDigest线程不安全!!!<p>
 *
 * @author S.Violet
 */
public class BaseBCCipher {

    /**
     * 加密(byte[]数据)
     *
     * @param data 数据
     * @param key 秘钥(SM4:128位)
     * @param keyAlgorithm 秘钥算法
     * @param cryptoAlgorithm 加密算法/填充算法
     */
    public static byte[] encrypt(byte[] data, byte[] key, String keyAlgorithm, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException {
        if (data == null){
            return null;
        }
        Cipher cipher = Cipher.getInstance(cryptoAlgorithm, BouncyCastleProvider.PROVIDER_NAME);
        SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        return cipher.doFinal(data);
    }

    /**
     * 加密(byte[]数据, 使用CBC填充算法时需要用该方法并指定iv初始化向量)
     *
     * @param data 数据
     * @param key 秘钥(SM4:128位)
     * @param keyAlgorithm 秘钥算法
     * @param ivSeed iv初始化向量, SM4 16 bytes
     * @param cryptoAlgorithm 加密算法/填充算法
     */
    public static byte[] encryptCBC(byte[] data, byte[] key, String keyAlgorithm, byte[] ivSeed, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidAlgorithmParameterException {
        if (data == null){
            return null;
        }
        Cipher cipher = Cipher.getInstance(cryptoAlgorithm, BouncyCastleProvider.PROVIDER_NAME);
        SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(ivSeed);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec);
        return cipher.doFinal(data);
    }

    /**
     * 加密(大文件, 注意, 输入输出流会被关闭)
     *
     * @param in 待加密数据流
     * @param out 加密后数据流
     * @param key 秘钥(SM4:128位)
     * @param keyAlgorithm 秘钥算法
     * @param cryptoAlgorithm 加密算法/填充算法
     */
    public static void encrypt(InputStream in, OutputStream out, byte[] key, String keyAlgorithm, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, NoSuchProviderException {
        if (in == null) {
            throw new NullPointerException("in is null");
        }
        if (out == null) {
            throw new NullPointerException("out is null");
        }

        try {
            Cipher cipher = Cipher.getInstance(cryptoAlgorithm, BouncyCastleProvider.PROVIDER_NAME);
            SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            in = new CipherInputStream(in, cipher);
            byte[] buff = new byte[1024 * 32];
            int length;
            while ((length = in.read(buff)) >= 0) {
                out.write(buff, 0, length);
            }
        } finally {
            CloseableUtils.closeQuiet(in);
            CloseableUtils.closeQuiet(out);
        }
    }

    /**
     * 加密(大文件, 注意, 输入输出流会被关闭, 使用CBC填充算法时需要用该方法并指定iv初始化向量)
     *
     * @param in 待加密数据流
     * @param out 加密后数据流
     * @param key 秘钥(SM4:128位)
     * @param keyAlgorithm 秘钥算法
     * @param ivSeed iv初始化向量, SM4 16 bytes
     * @param cryptoAlgorithm 加密算法/填充算法
     */
    public static void encryptCBC(InputStream in, OutputStream out, byte[] key, String keyAlgorithm, byte[] ivSeed, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IOException, NoSuchProviderException {
        if (in == null) {
            throw new NullPointerException("in is null");
        }
        if (out == null) {
            throw new NullPointerException("out is null");
        }

        try {
            Cipher cipher = Cipher.getInstance(cryptoAlgorithm, BouncyCastleProvider.PROVIDER_NAME);
            SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(ivSeed);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec);

            in = new CipherInputStream(in, cipher);
            byte[] buff = new byte[1024 * 32];
            int length;
            while ((length = in.read(buff)) >= 0) {
                out.write(buff, 0, length);
            }
        } finally {
            CloseableUtils.closeQuiet(in);
            CloseableUtils.closeQuiet(out);
        }
    }

    /**
     * 解密(byte[]数据)
     *
     * @param data 数据
     * @param key 秘钥(SM4:128位)
     * @param keyAlgorithm 秘钥算法
     * @param cryptoAlgorithm 加密算法/填充算法
     */
    public static byte[] decrypt(byte[] data, byte[] key, String keyAlgorithm, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException {
        if (data == null){
            return null;
        }
        Cipher cipher = Cipher.getInstance(cryptoAlgorithm, BouncyCastleProvider.PROVIDER_NAME);
        SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        return cipher.doFinal(data);
    }

    /**
     * 解密(byte[]数据, CBC填充需要用该方法并指定iv初始化向量)
     *
     * @param data 数据
     * @param key 秘钥(SM4:128位)
     * @param keyAlgorithm 秘钥算法
     * @param ivSeed iv初始化向量, AES 16 bytes, DES 8bytes
     * @param cryptoAlgorithm 加密算法/填充算法
     */
    public static byte[] decryptCBC(byte[] data, byte[] key, String keyAlgorithm, byte[] ivSeed, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchProviderException {
        if (data == null){
            return null;
        }
        Cipher cipher = Cipher.getInstance(cryptoAlgorithm, BouncyCastleProvider.PROVIDER_NAME);
        SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(ivSeed);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec);
        return cipher.doFinal(data);
    }

    /**
     * 解密(大文件, 注意, 输入输出流会被关闭)
     *
     * @param in 待解密数据流
     * @param out 解密后数据流
     * @param key 秘钥(SM4:128位)
     * @param keyAlgorithm 秘钥算法
     * @param cryptoAlgorithm 加密算法/填充算法
     */
    public static void decrypt(InputStream in, OutputStream out, byte[] key, String keyAlgorithm, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, IOException, NoSuchProviderException {
        if (in == null) {
            throw new NullPointerException("in is null");
        }
        if (out == null) {
            throw new NullPointerException("out is null");
        }

        try {
            Cipher cipher = Cipher.getInstance(cryptoAlgorithm, BouncyCastleProvider.PROVIDER_NAME);
            SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            out = new CipherOutputStream(out, cipher);
            byte[] buff = new byte[1024 * 32];
            int length;
            while ((length = in.read(buff)) >= 0) {
                out.write(buff, 0, length);
            }
        } finally {
            CloseableUtils.closeQuiet(in);
            CloseableUtils.closeQuiet(out);
        }
    }

    /**
     * 解密(大文件, 注意, 输入输出流会被关闭, CBC填充需要用该方法并指定iv初始化向量)
     *
     * @param in 待解密数据流
     * @param out 解密后数据流
     * @param key 秘钥(SM4:128位)
     * @param keyAlgorithm 秘钥算法
     * @param ivSeed iv初始化向量, SM4 16 bytes
     * @param cryptoAlgorithm 加密算法/填充算法
     */
    public static void decryptCBC(InputStream in, OutputStream out, byte[] key, String keyAlgorithm, byte[] ivSeed, String cryptoAlgorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IOException, NoSuchProviderException {
        if (in == null) {
            throw new NullPointerException("in is null");
        }
        if (out == null) {
            throw new NullPointerException("out is null");
        }

        try {
            Cipher cipher = Cipher.getInstance(cryptoAlgorithm, BouncyCastleProvider.PROVIDER_NAME);
            SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(ivSeed);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec);

            out = new CipherOutputStream(out, cipher);
            byte[] buff = new byte[1024 * 32];
            int length;
            while ((length = in.read(buff)) >= 0) {
                out.write(buff, 0, length);
            }
        } finally {
            CloseableUtils.closeQuiet(in);
            CloseableUtils.closeQuiet(out);
        }
    }

}
