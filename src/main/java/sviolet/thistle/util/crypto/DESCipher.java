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

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * DES加密工具
 *
 * Created by S.Violet on 2016/12/20.
 */
public class DESCipher {

    private static final String CRYPTO_TRANSFORMATION_DES = "DES";
    private static final String CRYPTO_TRANSFORMATION_DES_EDE = "DESede";
    private static final String CRYPTO_TRANSFORMATION_DES_EDE_ECB_NO_PADDING = "DESede/ECB/NoPadding";
    private static final String CRYPTO_TRANSFORMATION_DES_EDE_ECB_PKCS5 = "DESede/ECB/PKCS5Padding";

    /**
     * @param data 数据
     * @param keyData 秘钥数据 8bytes(64bit)
     */
    public static byte[] encryptDes(byte[] data, byte[] keyData) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        return encrypt(data, keyData, CRYPTO_TRANSFORMATION_DES, CRYPTO_TRANSFORMATION_DES);
    }

    /**
     * @param data 数据
     * @param keyData 秘钥数据 24bytes(192bit), 若为16bytes秘钥， 则在后面补上前8bytes（111111112222222211111111）
     */
    public static byte[] encryptDesEdeEcbNoPadding(byte[] data, byte[] keyData) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        return encrypt(data, keyData, CRYPTO_TRANSFORMATION_DES_EDE, CRYPTO_TRANSFORMATION_DES_EDE_ECB_NO_PADDING);
    }

    /**
     * @param data 数据
     * @param keyData 秘钥数据 24bytes(192bit), 若为16bytes秘钥， 则在后面补上前8bytes（111111112222222211111111）
     */
    public static byte[] encryptDesEdeEcbPKCS5(byte[] data, byte[] keyData) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        return encrypt(data, keyData, CRYPTO_TRANSFORMATION_DES_EDE, CRYPTO_TRANSFORMATION_DES_EDE_ECB_PKCS5);
    }

    /**
     * @param data 数据
     * @param keyData 秘钥数据
     */
    private static byte[] encrypt(byte[] data, byte[] keyData, String keyAlgorithm, String cryptoAlgorithm) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        SecretKey keyInstance = new SecretKeySpec(keyData, keyAlgorithm);
        Cipher cipher = Cipher.getInstance(cryptoAlgorithm);
        cipher.init(Cipher.ENCRYPT_MODE, keyInstance);
        return cipher.doFinal(data);
    }

    /**
     * @param data 数据
     * @param keyData 秘钥数据 8bytes(64bit)
     */
    public static byte[] decryptDes(byte[] data, byte[] keyData) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        return decrypt(data, keyData, CRYPTO_TRANSFORMATION_DES, CRYPTO_TRANSFORMATION_DES);
    }

    /**
     * @param data 数据
     * @param keyData 秘钥数据 24bytes(192bit), 若为16bytes秘钥， 则在后面补上前8bytes（111111112222222211111111）
     */
    public static byte[] decryptDesEdeEcbNoPadding(byte[] data, byte[] keyData) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        return decrypt(data, keyData, CRYPTO_TRANSFORMATION_DES_EDE, CRYPTO_TRANSFORMATION_DES_EDE_ECB_NO_PADDING);
    }

    /**
     * @param data 数据
     * @param keyData 秘钥数据 24bytes(192bit), 若为16bytes秘钥， 则在后面补上前8bytes（111111112222222211111111）
     */
    public static byte[] decryptDesEdeEcbPKCS5(byte[] data, byte[] keyData) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        return decrypt(data, keyData, CRYPTO_TRANSFORMATION_DES_EDE, CRYPTO_TRANSFORMATION_DES_EDE_ECB_PKCS5);
    }

    /**
     * @param data 数据
     * @param keyData 秘钥数据
     */
    private static byte[] decrypt(byte[] data, byte[] keyData, String keyAlgorithm, String cryptoAlgorithm) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        SecretKey keyInstance = new SecretKeySpec(keyData, keyAlgorithm);
        Cipher cipher = Cipher.getInstance(cryptoAlgorithm);
        cipher.init(Cipher.DECRYPT_MODE, keyInstance);
        return cipher.doFinal(data);
    }

}
