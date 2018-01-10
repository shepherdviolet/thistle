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

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

/**
 * AES秘钥生成工具
 *
 * @author S.Violet
 */
public class AESKeyGenerator {

	public static final String KEY_ALGORITHM = "AES";

    /**
     * <p>生成对称密钥, 用于服务端场合</p>
     *
     * @return 秘钥
     */
    public static byte[] generate() throws NoSuchProviderException, NoSuchAlgorithmException {
        return BaseKeyGenerator.generateKey((SecureRandom) null, 128, KEY_ALGORITHM);
    }

	/**
	 * <p>生成对称密钥, 用于服务端场合</p>
	 *
	 * @param secureRandom SecureRandom是线程安全的, 服务端通常使用一个单例的SecureRandom
	 * @return 秘钥
	 */
	public static byte[] generate(SecureRandom secureRandom) throws NoSuchProviderException, NoSuchAlgorithmException {
		return BaseKeyGenerator.generateKey(secureRandom, 128, KEY_ALGORITHM);
	}

	/**
	 * <p>生成128位AES密钥(不同系统平台相同seed生成结果可能不同), Android使用该方法, 相同seed仍会产生随机秘钥</p>
	 *
	 * @param seed 秘钥种子
	 * @return 秘钥
	 */
	public static byte[] generate(byte[] seed) throws NoSuchProviderException, NoSuchAlgorithmException {
		return BaseKeyGenerator.generateKey(seed, 128, KEY_ALGORITHM);
	}

	/**
	 * <p>生成AES密钥(不同系统平台相同seed生成结果可能不同), Android使用该方法, 相同seed仍会产生随机秘钥</p>
	 *
	 * @param seed 秘钥种子
	 * @param bits 秘钥位数(128)
	 * @return 秘钥
	 */
	public static byte[] generate(byte[] seed, int bits) throws NoSuchProviderException, NoSuchAlgorithmException {
		return BaseKeyGenerator.generateKey(seed, bits, KEY_ALGORITHM);
	}

	/**
	 * 利用SHA256摘要算法计算128位固定密钥, 安全性低, 但保证全平台一致
	 *
	 * @param seed 密码种子
	 */
	public static byte[] generateShaKey128(byte[] seed){
		return BaseKeyGenerator.generateShaKey128(seed);
	}

	/**
	 * 利用SHA256摘要算法计算256位固定密钥, 安全性低, 但保证全平台一致
	 *
	 * @param seed 密码种子
	 */
	public static byte[] generateShaKey256(byte[] seed){
		return BaseKeyGenerator.generateShaKey256(seed);
	}

}
