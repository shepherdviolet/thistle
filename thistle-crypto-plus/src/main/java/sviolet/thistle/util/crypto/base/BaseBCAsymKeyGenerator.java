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
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;

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
        return new ECPublicKeyParameters(domainParameters.getCurve().decodePoint(pointASN1Encoding), domainParameters);
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

}
