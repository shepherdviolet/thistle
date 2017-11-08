/*
 * Copyright (C) 2015 S.Violet
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

package sviolet.thistle.util.crypto;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * 
 * AES加密工具
 * 
 * @author S.Violet ()
 *
 */

public class AESCipher{
	
	public static final String CRYPTO_TRANSFORMATION_AES = "AES";
	public static final String CRYPTO_TRANSFORMATION_AES_ECB_PKCS5PADDING = "AES/ECB/PKCS5Padding";

	public static final String CRYPTO_TRANSFORMATION_AES_CBC_NOPADDING = "AES/CBC/NoPadding";
	public static final String CRYPTO_TRANSFORMATION_AES_CBC_PKCS5PADDING = "AES/CBC/PKCS5Padding";

	/**
	 * 加密
	 * 
	 * @param data 数据
	 * @param key 秘钥(通常只支持128)
	 * @param cryptoTransformation 加密算法/填充算法
	 *
	 * @throws NoSuchAlgorithmException 加密算法无效
	 * @throws NoSuchPaddingException 填充算法无效
	 * @throws InvalidKeyException 秘钥无效
	 * @throws IllegalBlockSizeException 块大小无效
	 * @throws BadPaddingException 填充错误(密码错?)
	 * @throws InvalidAlgorithmParameterException 算法参数无效
	 */
	public static byte[] encrypt(byte[] data, byte[] key, String cryptoTransformation) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException{
		SecretKeySpec keySpec = new SecretKeySpec(key, AESKeyGenerator.AES_KEY_ALGORITHM);
		Cipher cipher = Cipher.getInstance(cryptoTransformation);// 创建密码器
		cipher.init(Cipher.ENCRYPT_MODE, keySpec);// 初始化
		return cipher.doFinal(data);
	}

	/**
	 * 加密(大文件), 注意, 输入输出流会被关闭
	 *
	 * @param in 待加密数据流
     * @param out 加密后数据流
	 * @param key 秘钥(通常只支持128)
	 * @param cryptoTransformation 加密算法/填充算法
	 *
	 * @throws NoSuchAlgorithmException 加密算法无效
	 * @throws NoSuchPaddingException 填充算法无效
	 * @throws InvalidKeyException 秘钥无效
	 * @throws IllegalBlockSizeException 块大小无效
	 * @throws BadPaddingException 填充错误(密码错?)
	 * @throws InvalidAlgorithmParameterException 算法参数无效
	 */
	public static void encrypt(InputStream in, OutputStream out, byte[] key, String cryptoTransformation) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, IOException {
		if (in == null) {
		    throw new NullPointerException("in is null");
        }
        if (out == null) {
            throw new NullPointerException("out is null");
        }

        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, AESKeyGenerator.AES_KEY_ALGORITHM);
            Cipher cipher = Cipher.getInstance(cryptoTransformation);// 创建密码器
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);// 初始化

            in = new CipherInputStream(in, cipher);
            byte[] buff = new byte[1024 * 32];//32K buff
            int length;
            while ((length = in.read(buff)) >= 0) {
                out.write(buff, 0, length);
            }
        } finally {
            in.close();
            out.close();
        }
    }

	/**
	 * 加密(CBC填充需要用该方法并指定iv初始化向量)
	 *
	 * @param data 数据
	 * @param key 秘钥(通常只支持128)
	 * @param ivSeed iv初始化向量
	 * @param cryptoTransformation 加密算法/填充算法
	 *
	 * @throws NoSuchAlgorithmException 加密算法无效
	 * @throws NoSuchPaddingException 填充算法无效
	 * @throws InvalidKeyException 秘钥无效
	 * @throws IllegalBlockSizeException 块大小无效
	 * @throws BadPaddingException 填充错误(密码错?)
	 * @throws InvalidAlgorithmParameterException 算法参数无效
	 */
	public static byte[] encryptCBC(byte[] data, byte[] key, byte[] ivSeed, String cryptoTransformation) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException{
		SecretKeySpec keySpec = new SecretKeySpec(key, AESKeyGenerator.AES_KEY_ALGORITHM);
		Cipher cipher = Cipher.getInstance(cryptoTransformation);// 创建密码器
		IvParameterSpec iv = new IvParameterSpec(ivSeed);
		cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);// 初始化
		return cipher.doFinal(data);
	}

	/**
	 * 解密
	 *
	 * @param data 数据
	 * @param key 秘钥(通常只支持128)
	 * @param cryptoTransformation 加密算法/填充算法
	 *
	 * @throws NoSuchAlgorithmException 加密算法无效
	 * @throws NoSuchPaddingException 填充算法无效
	 * @throws InvalidKeyException 秘钥无效
	 * @throws IllegalBlockSizeException 块大小无效
	 * @throws BadPaddingException 填充错误(密码错?)
	 * @throws InvalidAlgorithmParameterException 算法参数无效
	 */
	public static byte[] decrypt(byte[] data, byte[] key, String cryptoTransformation) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException{
		SecretKeySpec keySpec = new SecretKeySpec(key, AESKeyGenerator.AES_KEY_ALGORITHM);
		Cipher cipher = Cipher.getInstance(cryptoTransformation);// 创建密码器
		cipher.init(Cipher.DECRYPT_MODE, keySpec);// 初始化
		return cipher.doFinal(data);
	}

    /**
     * 解密(大文件), 注意, 输入输出流会被关闭
     *
     * @param in 待解密数据流
     * @param out 解密后数据流
     * @param key 秘钥(通常只支持128)
     * @param cryptoTransformation 加密算法/填充算法
     *
     * @throws NoSuchAlgorithmException 加密算法无效
     * @throws NoSuchPaddingException 填充算法无效
     * @throws InvalidKeyException 秘钥无效
     * @throws IllegalBlockSizeException 块大小无效
     * @throws BadPaddingException 填充错误(密码错?)
     * @throws InvalidAlgorithmParameterException 算法参数无效
     */
    public static void decrypt(InputStream in, OutputStream out, byte[] key, String cryptoTransformation) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, IOException {
        if (in == null) {
            throw new NullPointerException("in is null");
        }
        if (out == null) {
            throw new NullPointerException("out is null");
        }

        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, AESKeyGenerator.AES_KEY_ALGORITHM);
            Cipher cipher = Cipher.getInstance(cryptoTransformation);// 创建密码器
            cipher.init(Cipher.DECRYPT_MODE, keySpec);// 初始化

            out = new CipherOutputStream(out, cipher);
            byte[] buff = new byte[1024 * 32];//32K buff
            int length;
            while ((length = in.read(buff)) >= 0) {
                out.write(buff, 0, length);
            }
        } finally {
            in.close();
            out.close();
        }
    }

	/**
	 * 解密(CBC填充需要用该方法并指定iv初始化向量)
	 *
	 * @param data 数据
	 * @param key 秘钥(通常只支持128)
	 * @param ivSeed iv初始化向量
	 * @param cryptoTransformation 加密算法/填充算法
	 *
	 * @throws NoSuchAlgorithmException 加密算法无效
	 * @throws NoSuchPaddingException 填充算法无效
	 * @throws InvalidKeyException 秘钥无效
	 * @throws IllegalBlockSizeException 块大小无效
	 * @throws BadPaddingException 填充错误(密码错?)
	 * @throws InvalidAlgorithmParameterException 算法参数无效
	 */
	public static byte[] decrypt(byte[] data, byte[] key, byte[] ivSeed, String cryptoTransformation) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException{
		SecretKeySpec keySpec = new SecretKeySpec(key, AESKeyGenerator.AES_KEY_ALGORITHM);
		Cipher cipher = Cipher.getInstance(cryptoTransformation);// 创建密码器
		IvParameterSpec iv = new IvParameterSpec(ivSeed);
		cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);// 初始化
		return cipher.doFinal(data);
	}

}
