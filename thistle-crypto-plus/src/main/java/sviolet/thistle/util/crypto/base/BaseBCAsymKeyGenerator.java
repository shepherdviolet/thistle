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
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

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
    public static BCECPrivateKey ecPrivateKeyParamsToEcPrivateKey(ECPrivateKeyParameters privateKeyParams, ECPublicKeyParameters publicKeyParams, String keyAlgorithm) {
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
    public static BCECPublicKey ecPublicKeyParamsToEcPublicKey(ECPublicKeyParameters publicKeyParams, String keyAlgorithm) {
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
     * 将JDK的XXXKey私钥实例转换为BouncyCastle的XXXKeyParameters私钥实例
     *
     * @param privateKey JDK的XXXKey私钥实例
     * @return BouncyCastle的XXXKeyParameters私钥实例
     */
    public static ECPrivateKeyParameters ecPrivateKeyToEcPrivateKeyParams(BCECPrivateKey privateKey) {
        if (privateKey == null) {
            throw new RuntimeException("privateKey == null");
        }
        ECParameterSpec parameterSpec = privateKey.getParameters();
        ECDomainParameters domainParameters = new ECDomainParameters(
                parameterSpec.getCurve(),
                parameterSpec.getG(),
                parameterSpec.getN(),
                parameterSpec.getH());
        return new ECPrivateKeyParameters(privateKey.getD(), domainParameters);
    }

    /**
     * 将JDK的XXXKey公钥实例转换为BouncyCastle的XXXKeyParameters公钥实例
     *
     * @param publicKey JDK的XXXKey公钥实例
     * @return BouncyCastle的XXXKeyParameters公钥实例
     */
    public static ECPublicKeyParameters ecPublicKeyToEcPublicKeyParams(BCECPublicKey publicKey) {
        if (publicKey == null) {
            throw new RuntimeException("publicKey == null");
        }
        ECParameterSpec parameterSpec = publicKey.getParameters();
        ECDomainParameters domainParameters = new ECDomainParameters(
                parameterSpec.getCurve(),
                parameterSpec.getG(),
                parameterSpec.getN(),
                parameterSpec.getH());
        return new ECPublicKeyParameters(publicKey.getQ(), domainParameters);
    }

    /**
     * 将PKCS8数据解析为JDK的XXXKey私钥实例, 用于再转化为BouncyCastle的XXXKeyParameters私钥实例
     *
     * @param pkcs8 PKCS8私钥数据
     * @param keyAlgorithm 密钥算法(EC, SM2暂时也用EC)
     * @return JDK的XXXKey私钥实例
     */
    public static BCECPrivateKey parseEcPrivateKeyByPkcs8(byte[] pkcs8, String keyAlgorithm) throws InvalidKeySpecException{
        if (pkcs8 == null) {
            throw new RuntimeException("pkcs8 == null");
        }
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8);
            KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm, BouncyCastleProvider.PROVIDER_NAME);
            return (BCECPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (NoSuchProviderException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 将X509数据解析为JDK的XXXKey公钥实例, 用于再转化为BouncyCastle的XXXKeyParameters公钥实例
     *
     * @param x509 X509公钥数据
     * @param keyAlgorithm 密钥算法(EC, SM2暂时也用EC)
     * @return JDK的XXXKey公钥实例
     */
    public static BCECPublicKey parseEcPublicKeyByX509(byte[] x509, String keyAlgorithm) throws InvalidKeySpecException {
        if (x509 == null) {
            throw new RuntimeException("x509 == null");
        }
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(x509);
            KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm, BouncyCastleProvider.PROVIDER_NAME);
            return (BCECPublicKey) keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (NoSuchProviderException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 根据已知的D值生成ECC/SM2私钥实例, domainParameters是椭圆曲线参数
     *
     * @param domainParameters domainParameters = new ECDomainParameters(CURVE, G_POINT, N, H)
     * @param d D值
     * @return 私钥实例(与JDK的密钥实例不同)
     */
    public static ECPrivateKeyParameters parseEcPrivateKeyParams(ECDomainParameters domainParameters, BigInteger d) throws CommonCryptoException {
        if (d == null) {
            throw new NullPointerException("d == null");
        }
        try {
            return new ECPrivateKeyParameters(d, domainParameters);
        } catch (Exception e) {
            throw new CommonCryptoException("Error while parsing D to privateKeyParameters", e);
        }
    }

    /**
     * 根据已知的坐标点(ASN.1编码数据)生成ECC/SM2公钥实例, domainParameters是椭圆曲线参数
     *
     * @param domainParameters domainParameters = new ECDomainParameters(CURVE, G_POINT, N, H)
     * @param pointASN1Encoding 公钥坐标点(ASN.1编码数据)
     * @return 公钥实例(与JDK的密钥实例不同)
     */
    public static ECPublicKeyParameters parseEcPublicKeyParams(ECDomainParameters domainParameters, byte[] pointASN1Encoding) throws CommonCryptoException {
        if (pointASN1Encoding == null) {
            throw new RuntimeException("pointASN1Encoding == null");
        }
        try {
            //将ASN.1编码的数据转为ECPoint实例
            ECPoint point = domainParameters.getCurve().decodePoint(pointASN1Encoding);
            return new ECPublicKeyParameters(point, domainParameters);
        } catch (Exception e) {
            throw new CommonCryptoException("Error while parsing ASN.1 point to publicKeyParameters", e);
        }
    }

    /**
     * 根据已知的坐标(X/Y)生成ECC/SM2公钥实例, domainParameters是椭圆曲线参数
     *
     * @param domainParameters domainParameters = new ECDomainParameters(CURVE, G_POINT, N, H)
     * @param xBytes 坐标X, 字节形式(bigInteger.toByteArray()获得)
     * @param yBytes 坐标Y, 字节形式(bigInteger.toByteArray()获得)
     * @return 公钥实例(与JDK的密钥实例不同)
     */
    public static ECPublicKeyParameters parseEcPublicKeyParams(ECDomainParameters domainParameters, byte[] xBytes, byte[] yBytes) throws CommonCryptoException {
        try {
            //将ASN.1编码的数据转为ECPoint实例
            return parseEcPublicKeyParams(domainParameters, BaseCryptoUtils.pointToASN1Encoding(xBytes, yBytes));
        } catch (Exception e) {
            throw new CommonCryptoException("Error while parsing point (X/Y) to publicKeyParameters", e);
        }
    }

}
