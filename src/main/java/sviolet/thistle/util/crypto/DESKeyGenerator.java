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
 * Project GitHub: https://github.com/shepherdviolet/thistle
 * Email: shepherdviolet@163.com
 */

package sviolet.thistle.util.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/**
 * DES秘钥生成工具
 */
public class DESKeyGenerator {

	public static final String DES_KEY_ALGORITHM = "DES";
	public static final String DES_EDE_KEY_ALGORITHM = "DESede";

	/**
	 * <p>生成64位DES密钥(不同系统平台相同seed生成结果可能不同), Android使用该方法, 相同seed仍会产生随机秘钥</p>
	 *
	 * @param seed 秘钥种子
	 * @return 秘钥
	 */
	public static byte[] generateDes(byte[] seed) throws NoSuchProviderException, NoSuchAlgorithmException {
		return BaseKeyGenerator.generateKey(seed, 64, DES_KEY_ALGORITHM);
	}

	/**
	 * <p>生成64位DES密钥(不同系统平台相同seed生成结果可能不同), Android使用该方法, 相同seed仍会产生随机秘钥</p>
	 *
	 * @param seed 秘钥种子
	 * @return 秘钥
	 */
	public static byte[] generateDesEde(byte[] seed) throws NoSuchProviderException, NoSuchAlgorithmException {
		return BaseKeyGenerator.generateKey(seed, 192, DES_EDE_KEY_ALGORITHM);
	}

	/**
	 * 利用SHA256摘要算法计算64位固定密钥, 安全性低, 但保证全平台一致
	 *
	 * @param seed 密码种子
	 */
	public static byte[] generateShaKey64(byte[] seed){
		return BaseKeyGenerator.generateShaKey64(seed);
	}

	/**
	 * 利用SHA256摘要算法计算192位固定密钥, 安全性低, 但保证全平台一致
	 *
	 * @param seed 密码种子
	 */
	public static byte[] generateShaKey192(byte[] seed){
		return BaseKeyGenerator.generateShaKey192(seed);
	}

}
