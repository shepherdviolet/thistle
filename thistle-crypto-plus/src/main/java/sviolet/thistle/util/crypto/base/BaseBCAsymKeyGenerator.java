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

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.security.*;

/**
 * [Bouncy castle]非对称密钥生成基本逻辑<p>
 * <p>
 * Not recommended for direct use<p>
 * <p>
 * 不建议直接使用<p>
 *
 * @author S.Violet
 */
public class BaseBCAsymKeyGenerator {

    public static final byte PREFIX_UNCOMPRESSED = 0x04;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 随机生成ECC/SM2密钥对, domainParameters是椭圆曲线参数, 需要:椭圆曲线/G点/N(order)/H(cofactor)
     *
     * @param domainParameters domainParameters = new ECDomainParameters(CURVE, G_POINT, N, H)
     * @param secureRandom     默认送空
     * @return 密钥对(与JDK的密钥实例不同)
     */
    public static AsymmetricCipherKeyPair generateEcKeyParamsPair(ECDomainParameters domainParameters, SecureRandom secureRandom) {
        ECKeyGenerationParameters keyGenParams = new ECKeyGenerationParameters(domainParameters,
                secureRandom != null ? secureRandom : BaseKeyGenerator.getSystemSecureRandom());
        ECKeyPairGenerator keyPairGen = new ECKeyPairGenerator();
        keyPairGen.init(keyGenParams);
        return keyPairGen.generateKeyPair();
    }

    /**
     * 将BouncyCastle的XXXKeyParameters私钥实例转换为JDK的XXXKey私钥实例, 用于与JDK加密工具适配, 或获取PKCS8编码的私钥数据
     *
     * @param privateKeyParams 私钥, BouncyCastle的XXXKeyParameters密钥实例
     * @param publicKeyParams 公钥, 可为空(但送空会导致openssl无法读取PKCS8数据), BouncyCastle的XXXKeyParameters密钥实例
     * @param keyAlgorithm 密钥算法(EC, SM2暂时也用EC)
     * @return JDK的XXXKey密钥实例, 可以调用ECPrivateKey.getEncoded()方法获取PKCS8编码的私钥数据(甚至进一步转为PEM等格式)
     */
    public static BCECPrivateKey ecPrivateKeyParamsToEcPrivateKey(ECPrivateKeyParameters privateKeyParams, ECPublicKeyParameters publicKeyParams, String keyAlgorithm) throws Exception {
        if (privateKeyParams == null) {
            throw new RuntimeException("privateKeyParams == null");
        }
        ECDomainParameters domainParameters = privateKeyParams.getParameters();
        ECParameterSpec parameterSpec = new ECParameterSpec(
                domainParameters.getCurve(),
                domainParameters.getG(),
                domainParameters.getN(),
                domainParameters.getH());
        BCECPublicKey publicKey = null;
        if (publicKeyParams != null) {
            publicKey = new BCECPublicKey(keyAlgorithm, publicKeyParams, parameterSpec, BouncyCastleProvider.CONFIGURATION);
        }
        return new BCECPrivateKey(keyAlgorithm, privateKeyParams, publicKey, parameterSpec, BouncyCastleProvider.CONFIGURATION);
    }

    /**
     * 将BouncyCastle的XXXKeyParameters公钥实例转换为JDK的XXXKey公钥实例, 用于与JDK加密工具适配, 或获取X509编码的公钥数据
     *
     * @param publicKeyParams 公钥, BouncyCastle的XXXKeyParameters密钥实例
     * @param keyAlgorithm 密钥算法(EC, SM2暂时也用EC)
     * @return JDK的XXXKey密钥实例, 可以调用ECPublicKey.getEncoded()方法获取X509编码的公钥数据(甚至进一步转为PEM等格式)
     */
    public static BCECPublicKey ecPublicKeyParamsToEcPublicKey(ECPublicKeyParameters publicKeyParams, String keyAlgorithm) throws Exception {
        if (publicKeyParams == null) {
            throw new RuntimeException("publicKeyParams == null");
        }
        ECDomainParameters domainParameters = publicKeyParams.getParameters();
        ECParameterSpec parameterSpec = new ECParameterSpec(
                domainParameters.getCurve(),
                domainParameters.getG(),
                domainParameters.getN(),
                domainParameters.getH());
        return new BCECPublicKey(keyAlgorithm, publicKeyParams, parameterSpec, BouncyCastleProvider.CONFIGURATION);
    }

    /**
     * 根据已知的D值生成ECC/SM2私钥实例, domainParameters是椭圆曲线参数
     *
     * @param domainParameters domainParameters = new ECDomainParameters(CURVE, G_POINT, N, H)
     * @param d D值
     * @return 私钥实例(与JDK的密钥实例不同)
     */
    public static ECPrivateKeyParameters parseEcPrivateKeyParams(ECDomainParameters domainParameters, BigInteger d) throws Exception {
        if (d == null) {
            throw new NullPointerException("d == null");
        }
        return new ECPrivateKeyParameters(d, domainParameters);
    }

    /**
     * 根据已知的坐标点(ASN.1编码数据)生成ECC/SM2公钥实例, domainParameters是椭圆曲线参数
     *
     * @param domainParameters domainParameters = new ECDomainParameters(CURVE, G_POINT, N, H)
     * @param pointASN1Encoding 公钥坐标点(ASN.1编码数据)
     * @return 公钥实例(与JDK的密钥实例不同)
     */
    public static ECPublicKeyParameters parseEcPublicKeyParams(ECDomainParameters domainParameters, byte[] pointASN1Encoding) throws Exception {
        if (pointASN1Encoding == null) {
            throw new RuntimeException("pointASN1Encoding == null");
        }
        //将ASN.1编码的数据转为ECPoint实例
        ECPoint point = domainParameters.getCurve().decodePoint(pointASN1Encoding);
        return new ECPublicKeyParameters(point, domainParameters);
    }

    /**
     * 根据已知的坐标(X/Y)生成ECC/SM2公钥实例, domainParameters是椭圆曲线参数
     *
     * @param domainParameters domainParameters = new ECDomainParameters(CURVE, G_POINT, N, H)
     * @param xBytes 坐标X, 字节形式(bigInteger.toByteArray()获得)
     * @param yBytes 坐标Y, 字节形式(bigInteger.toByteArray()获得)
     * @return 公钥实例(与JDK的密钥实例不同)
     */
    public static ECPublicKeyParameters parseEcPublicKeyParams(ECDomainParameters domainParameters, byte[] xBytes, byte[] yBytes) throws Exception {
        //将ASN.1编码的数据转为ECPoint实例
        return parseEcPublicKeyParams(domainParameters, pointToASN1Encoding(xBytes, yBytes));
    }

    /**
     * 将坐标(X/Y)转为ASN.1编码的坐标数据(非压缩)
     * @param xBytes 坐标X, 字节形式(bigInteger.toByteArray()获得)
     * @param yBytes 坐标Y, 字节形式(bigInteger.toByteArray()获得)
     * @return ASN.1编码的坐标数据(非压缩)
     */
    public static byte[] pointToASN1Encoding(byte[] xBytes, byte[] yBytes) {
        if (xBytes == null) {
            throw new NullPointerException("xBytes == null");
        }
        if (yBytes == null) {
            throw new NullPointerException("yBytes == null");
        }
        byte[] asn1Encoding = new byte[1 + xBytes.length + yBytes.length];
        asn1Encoding[0] = PREFIX_UNCOMPRESSED;
        System.arraycopy(xBytes, 0, asn1Encoding, 1, xBytes.length);
        System.arraycopy(yBytes, 0, asn1Encoding, 1 + xBytes.length, yBytes.length);
        return asn1Encoding;
    }

    /**
     * 根据密钥实例(公钥或私钥)计算SM2用于加密时, 密文C1区域的长度, 密文为C1C3C2或C1C2C3, C1区域为随机公钥点数据(ASN.1格式)
     *
     * @param keyParams 密钥实例(公钥或私钥)
     * @return 密文C1区域长度
     */
    public static int calculateSM2C1Length(ECKeyParameters keyParams) {
        return calculateSM2C1Length(keyParams.getParameters());
    }

    /**
     * 根据密钥实例(公钥或私钥)计算SM2用于加密时, 密文C1区域的长度, 密文为C1C3C2或C1C2C3, C1区域为随机公钥点数据(ASN.1格式).
     * domainParameters是椭圆曲线参数, 需要:椭圆曲线/G点/N(order)/H(cofactor)
     *
     * @param domainParameters domainParameters = new ECDomainParameters(CURVE, G_POINT, N, H)
     * @return 密文C1区域长度
     */
    public static int calculateSM2C1Length(ECDomainParameters domainParameters) {
        return (domainParameters.getCurve().getFieldSize() + 7) / 8;
    }

}
