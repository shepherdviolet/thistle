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

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * 
 * AES加密工具
 * 
 * @author S.Violet (ZhuQinChao)
 *
 */

public class AESCipher{
	
	public static final String CRYPTO_TRANSFORMATION_AES = "AES";
	public static final String CRYPTO_TRANSFORMATION_AES_ECB_PKCS5PADDING = "AES/ECB/PKCS5Padding";

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
}
