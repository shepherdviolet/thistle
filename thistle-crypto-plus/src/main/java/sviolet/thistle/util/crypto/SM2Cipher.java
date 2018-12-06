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

import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import sviolet.thistle.util.crypto.base.BaseBCCipher;
import sviolet.thistle.util.crypto.base.SM2DefaultCurve;

import java.io.IOException;
import java.io.InputStream;

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
     * 使用SM2私钥签名数据
     * @param data 待签名数据
     * @param id 签名ID, 可为空, 默认"1234567812345678".getBytes()
     * @param privateKeyParams 私钥
     * @param signAlgorithm 签名算法(SM2Cipher.SIGN_ALGORITHM_SM2_SM3)
     * @return 签名数据(R+S)
     */
    public static byte[] sign(byte[] data, byte[] id, ECPrivateKeyParameters privateKeyParams, String signAlgorithm) throws CryptoException {
        return BaseBCCipher.signBySM2PrivateKeyParams(data, id, privateKeyParams);
    }

    /**
     * 使用SM2私钥签名输入流
     * @param inputStream 待签名数据的输入流, 执行完毕后会被关闭
     * @param id 签名ID, 可为空, 默认"1234567812345678".getBytes()
     * @param privateKeyParams 私钥
     * @param signAlgorithm 签名算法(SM2Cipher.SIGN_ALGORITHM_SM2_SM3)
     * @return 签名数据(R+S)
     */
    public static byte[] sign(InputStream inputStream, byte[] id, ECPrivateKeyParameters privateKeyParams, String signAlgorithm) throws CryptoException, IOException {
        return BaseBCCipher.signBySM2PrivateKeyParams(inputStream, id, privateKeyParams);
    }

    /**
     * 使用SM2私钥签名数据, 结果为DER编码
     * @param data 待签名数据
     * @param id 签名ID, 可为空, 默认"1234567812345678".getBytes()
     * @param privateKeyParams 私钥
     * @param signAlgorithm 签名算法(SM2Cipher.SIGN_ALGORITHM_SM2_SM3)
     * @return DER编码签名数据
     */
    public static byte[] signToDER(byte[] data, byte[] id, ECPrivateKeyParameters privateKeyParams, String signAlgorithm) throws CryptoException, IOException {
        return BaseBCCipher.sm2SignDataToDerEncoded(
                BaseBCCipher.signBySM2PrivateKeyParams(data, id, privateKeyParams)
        );
    }

    /**
     * 使用SM2私钥签名输入流, 结果为DER编码
     * @param inputStream 待签名数据的输入流, 执行完毕后会被关闭
     * @param id 签名ID, 可为空, 默认"1234567812345678".getBytes()
     * @param privateKeyParams 私钥
     * @param signAlgorithm 签名算法(SM2Cipher.SIGN_ALGORITHM_SM2_SM3)
     * @return DER编码签名数据
     */
    public static byte[] signToDER(InputStream inputStream, byte[] id, ECPrivateKeyParameters privateKeyParams, String signAlgorithm) throws CryptoException, IOException {
        return BaseBCCipher.sm2SignDataToDerEncoded(
                BaseBCCipher.signBySM2PrivateKeyParams(inputStream, id, privateKeyParams)
        );
    }

    /**
     * 使用SM2公钥验签
     * @param data 数据
     * @param sign 签名, R+S 64bytes
     * @param id 签名ID, 可为空, 默认"1234567812345678".getBytes()
     * @param publicKeyParams 公钥
     * @param signAlgorithm 签名算法(SM2Cipher.SIGN_ALGORITHM_SM2_SM3)
     * @return true:验签通过
     */
    public static boolean verify(byte[] data, byte[] sign, byte[] id, ECPublicKeyParameters publicKeyParams, String signAlgorithm) {
        return BaseBCCipher.verifyBySM2PublicKeyParams(data, sign, id, publicKeyParams);
    }

    /**
     * 使用SM2公钥验签
     * @param inputStream 待签名数据的输入流, 执行完毕后会被关闭
     * @param sign 签名, R+S 64bytes
     * @param id 签名ID, 可为空, 默认"1234567812345678".getBytes()
     * @param publicKeyParams 公钥
     * @param signAlgorithm 签名算法(SM2Cipher.SIGN_ALGORITHM_SM2_SM3)
     * @return true:验签通过
     */
    public static boolean verify(InputStream inputStream, byte[] sign, byte[] id, ECPublicKeyParameters publicKeyParams, String signAlgorithm) throws IOException {
        return BaseBCCipher.verifyBySM2PublicKeyParams(inputStream, sign, id, publicKeyParams);
    }

    /**
     * 使用SM2公钥验签, 用于DER编码的签名数据
     * @param data 数据
     * @param sign 签名, DER编码格式
     * @param id 签名ID, 可为空, 默认"1234567812345678".getBytes()
     * @param publicKeyParams 公钥
     * @param signAlgorithm 签名算法(SM2Cipher.SIGN_ALGORITHM_SM2_SM3)
     * @return true:验签通过
     */
    public static boolean verifyFromDER(byte[] data, byte[] sign, byte[] id, ECPublicKeyParameters publicKeyParams, String signAlgorithm) {
        byte[] rsSign;
        try {
            rsSign = BaseBCCipher.derEncodedToSM2SignData(sign);
        } catch (Exception e) {
            return false;
        }
        return BaseBCCipher.verifyBySM2PublicKeyParams(
                data,
                rsSign,
                id,
                publicKeyParams);
    }

    /**
     * 使用SM2公钥验签, 用于DER编码的签名数据
     * @param inputStream 待签名数据的输入流, 执行完毕后会被关闭
     * @param sign 签名, DER编码格式
     * @param id 签名ID, 可为空, 默认"1234567812345678".getBytes()
     * @param publicKeyParams 公钥
     * @param signAlgorithm 签名算法(SM2Cipher.SIGN_ALGORITHM_SM2_SM3)
     * @return true:验签通过
     */
    public static boolean verifyFromDER(InputStream inputStream, byte[] sign, byte[] id, ECPublicKeyParameters publicKeyParams, String signAlgorithm) throws IOException {
        byte[] rsSign;
        try {
            rsSign = BaseBCCipher.derEncodedToSM2SignData(sign);
        } catch (Exception e) {
            return false;
        }
        return BaseBCCipher.verifyBySM2PublicKeyParams(
                inputStream,
                rsSign,
                id,
                publicKeyParams);
    }

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
     * 使用SM2私钥解密(密文为C1C2C3格式)
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
     * 使用SM2私钥解密(密文为C1C3C2格式)
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

    /**
     * 使用SM2公钥加密(密文为C1C2C3 DER格式)
     * @param publicKeyParams SM2公钥
     * @param data 原文数据
     * @param cryptoAlgorithm 加密算法(SM2Cipher.CRYPTO_ALGORITHM_SM2)
     * @return 密文, 密文为C1C2C3 DER格式（SM2密码算法使用规范 GM/T 0009-2012）
     */
    public static byte[] encryptToDER(byte[] data, ECPublicKeyParameters publicKeyParams, String cryptoAlgorithm) throws InvalidCipherTextException, IOException {
        return BaseBCCipher.sm2CipherTextC1C2C3ToDEREncoded(
                BaseBCCipher.encryptBySM2PublicKeyParams(data, publicKeyParams),
                SM2DefaultCurve.CURVE_LENGTH,
                SM3DigestCipher.SM3_HASH_LENGTH
        );
    }

    /**
     * 使用SM2私钥解密(密文为C1C2C3DER格式)
     * @param privateKeyParams SM2私钥
     * @param data 密文数据, 密文为C1C2C3 DER格式（SM2密码算法使用规范 GM/T 0009-2012）
     * @param cryptoAlgorithm 加密算法(SM2Cipher.CRYPTO_ALGORITHM_SM2)
     * @return 原文
     */
    public static byte[] decryptFromDER(byte[] data, ECPrivateKeyParameters privateKeyParams, String cryptoAlgorithm) throws Exception {
        return BaseBCCipher.decryptBySM2PrivateKeyParams(
                BaseBCCipher.derEncodedToSM2CipherTextC1C2C3(
                        data
                ),
                privateKeyParams);
    }

}
