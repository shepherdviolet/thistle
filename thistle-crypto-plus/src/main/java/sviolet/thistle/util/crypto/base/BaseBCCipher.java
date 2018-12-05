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

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import sviolet.thistle.util.common.CloseableUtils;
import sviolet.thistle.util.conversion.ByteUtils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.*;

/**
 * [Bouncy castle]加解密基本逻辑<p>
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

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /********************************************************************************************************************************
     * SM4 : Crypto
     ********************************************************************************************************************************/

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

    /********************************************************************************************************************************
     * SM2 : Sign Verify
     ********************************************************************************************************************************/



    /********************************************************************************************************************************
     * SM2 : Crypto
     ********************************************************************************************************************************/

    /**
     * 使用SM2公钥加密(密文为C1C2C3格式, 若需要C1C3C2新格式请用工具转换)
     * @param publicKeyParams SM2公钥
     * @param data 原文数据
     * @return 密文, 密文为C1C2C3格式, C1区域为随机公钥点数据(ASN.1格式), C2为密文数据, C3为摘要数据(SM3).
     */
    public static byte[] encryptBySM2PublicKeyParams(byte[] data, ECPublicKeyParameters publicKeyParams) throws InvalidCipherTextException {
        if (data == null) {
            return null;
        }
        if (publicKeyParams == null) {
            throw new NullPointerException("publicKeyParams == null");
        }
        SM2Engine engine = new SM2Engine();
        ParametersWithRandom parametersWithRandom = new ParametersWithRandom(publicKeyParams, BaseKeyGenerator.getSystemSecureRandom());
        engine.init(true, parametersWithRandom);
        return engine.processBlock(data, 0, data.length);
    }

    /**
     * 使用SM2私钥解密(密文为C1C2C3格式, 若需要C1C3C2新格式请用工具转换)
     * @param privateKeyParams SM2私钥
     * @param data 密文数据, 密文为C1C2C3格式, C1区域为随机公钥点数据(ASN.1格式), C2为密文数据, C3为摘要数据(SM3).
     * @return 原文
     */
    public static byte[] decryptBySM2PrivateKeyParams(byte[] data, ECPrivateKeyParameters privateKeyParams) throws InvalidCipherTextException {
        if (data == null) {
            return null;
        }
        if (privateKeyParams == null) {
            throw new NullPointerException("privateKeyParams == null");
        }
        SM2Engine engine = new SM2Engine();
        engine.init(false, privateKeyParams);
        return engine.processBlock(data, 0, data.length);
    }

    /**
     * 将SM2算法用于加密的密文格式, 从C1C2C3改为C1C3C2 (默认为C1C2C3).
     * C1区域为随机公钥点数据(ASN.1格式), C2为密文数据, C3为摘要数据(SM3).
     * @param cipherText C1C2C3格式的密文
     * @param c1Length C1区域长度, BaseBCAsymKeyGenerator.calculateSM2C1Length(SM2DefaultCurve.DOMAIN_PARAMS)
     * @param c3Length C3区域长度, 即SM3摘要结果长度, SM3DigestCipher.SM3_HASH_LENGTH
     * @return C1C3C2格式的密文
     */
    public static byte[] sm2CiphertextC1C2C3ToC1C3C2(byte[] cipherText, int c1Length, int c3Length){
        if (cipherText == null) {
            return null;
        }
        if (c1Length <= 0) {
            throw new IllegalArgumentException("c1Length <= 0");
        }
        if (c3Length <= 0) {
            throw new IllegalArgumentException("c3Length <= 0");
        }
        byte[] c1 = new byte[c1Length];
        System.arraycopy(cipherText, 0, c1, 0, c1.length);
        byte[] c2 = new byte[cipherText.length - c1.length - c3Length];
        System.arraycopy(cipherText, c1.length, c2, 0, c2.length);
        byte[] c3 = new byte[c3Length];
        System.arraycopy(cipherText, c1.length + c2.length, c3, 0, c3.length);
        return ByteUtils.joint(c1, c3, c2);
    }

    /**
     * 将SM2算法用于加密的密文格式, 从C1C3C2改为C1C2C3 (默认为C1C2C3).
     * C1区域为随机公钥点数据(ASN.1格式), C2为密文数据, C3为摘要数据(SM3).
     * @param cipherText C1C3C2格式的密文
     * @param c1Length C1区域长度, BaseBCAsymKeyGenerator.calculateSM2C1Length(SM2DefaultCurve.DOMAIN_PARAMS)
     * @param c3Length C3区域长度, 即SM3摘要结果长度, SM3DigestCipher.SM3_HASH_LENGTH
     * @return C1C2C3格式的密文
     */
    public static byte[] sm2CiphertextC1C3C2ToC1C2C3(byte[] cipherText, int c1Length, int c3Length){
        if (cipherText == null) {
            return null;
        }
        if (c1Length <= 0) {
            throw new IllegalArgumentException("c1Length <= 0");
        }
        if (c3Length <= 0) {
            throw new IllegalArgumentException("c3Length <= 0");
        }
        byte[] c1 = new byte[c1Length];
        System.arraycopy(cipherText, 0, c1, 0, c1.length);
        byte[] c3 = new byte[c3Length];
        System.arraycopy(cipherText, c1.length, c3, 0, c3.length);
        byte[] c2 = new byte[cipherText.length - c1.length - c3.length];
        System.arraycopy(cipherText, c1.length + c3.length, c2, 0, c2.length);
        return ByteUtils.joint(c1, c2, c3);
    }

}
