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

package sviolet.thistle.util.crypto;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import sviolet.thistle.util.crypto.base.BaseBCCipher;
import sviolet.thistle.util.crypto.base.SM2DefaultCurve;

/**
 * <p>SM2签名加密工具</p>
 *
 * <p>Cipher/Signature/MessageDigest线程不安全!!!</p>
 *
 * @author S.Violet
 */
public class SM2Cipher {

    /**
     * 签名算法:SM3withSM2
     */
    public static final String SIGN_ALGORITHM_SM2_SM3 = "SM3withSM2";

    /**
     * 加密算法:SM2
     */
    public static final String CRYPTO_ALGORITHM_SM2 = "SM2";

    /**
     * 使用SM2公钥加密(密文为C1C2C3格式)
     * @param publicKeyParams SM2公钥
     * @param data 原文数据
     * @param cryptoAlgorithm 加密算法(SM2Cipher.CRYPTO_ALGORITHM_SM2)
     * @return 密文, 密文为C1C2C3格式, C1区域为随机公钥点数据(ASN.1格式), C2为密文数据, C3为摘要数据(SM3).
     */
    public static byte[] encrypt(byte[] data, ECPublicKeyParameters publicKeyParams, String cryptoAlgorithm) throws InvalidCipherTextException {
        return BaseBCCipher.encryptBySM2PublicKeyParams(data, publicKeyParams);
    }

    /**
     * 使用SM2公钥加密(密文为C1C2C3格式)
     * @param privateKeyParams SM2私钥
     * @param data 密文数据, 密文为C1C2C3格式, C1区域为随机公钥点数据(ASN.1格式), C2为密文数据, C3为摘要数据(SM3).
     * @param cryptoAlgorithm 加密算法(SM2Cipher.CRYPTO_ALGORITHM_SM2)
     * @return 原文
     */
    public static byte[] decrypt(byte[] data, ECPrivateKeyParameters privateKeyParams, String cryptoAlgorithm) throws InvalidCipherTextException {
        return BaseBCCipher.decryptBySM2PrivateKeyParams(data, privateKeyParams);
    }

    /**
     * 使用SM2公钥加密(密文为C1C3C2格式)
     * @param publicKeyParams SM2公钥
     * @param data 原文数据
     * @param cryptoAlgorithm 加密算法(SM2Cipher.CRYPTO_ALGORITHM_SM2)
     * @return 密文, 密文为C1C3C2格式, C1区域为随机公钥点数据(ASN.1格式), C2为密文数据, C3为摘要数据(SM3).
     */
    public static byte[] encryptToC1C3C2(byte[] data, ECPublicKeyParameters publicKeyParams, String cryptoAlgorithm) throws InvalidCipherTextException {
        return BaseBCCipher.sm2CiphertextC1C2C3ToC1C3C2(
                BaseBCCipher.encryptBySM2PublicKeyParams(data, publicKeyParams),
                SM2DefaultCurve.C1_LENGTH,
                SM3DigestCipher.SM3_HASH_LENGTH
        );
    }

    /**
     * 使用SM2公钥加密(密文为C1C3C2格式)
     * @param privateKeyParams SM2私钥
     * @param data 密文数据, 密文为C1C3C2格式, C1区域为随机公钥点数据(ASN.1格式), C2为密文数据, C3为摘要数据(SM3).
     * @param cryptoAlgorithm 加密算法(SM2Cipher.CRYPTO_ALGORITHM_SM2)
     * @return 原文
     */
    public static byte[] decryptFromC1C3C2(byte[] data, ECPrivateKeyParameters privateKeyParams, String cryptoAlgorithm) throws InvalidCipherTextException {
        return BaseBCCipher.decryptBySM2PrivateKeyParams(
                BaseBCCipher.sm2CiphertextC1C3C2ToC1C2C3(
                        data,
                        SM2DefaultCurve.C1_LENGTH,
                        SM3DigestCipher.SM3_HASH_LENGTH
                ),
                privateKeyParams);
    }

}
