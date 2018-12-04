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

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import sviolet.thistle.util.crypto.base.BaseBCAsymKeyGenerator;
import sviolet.thistle.util.crypto.base.SM2DefaultCurve;

import java.math.BigInteger;

/**
 * <p>SM2秘钥生成工具</p>
 *
 * <p>BouncyCastle是使用XXXKeyParameters密钥实例的, 而JDK默认使用XXXKey密钥实例, 本工具类在加解密加解签时, 统一使用BouncyCastle
 * 的XXXKeyParameters密钥实例, 这与RSA/ECDSA工具类中使用XXXKey实例不同. 另外, 本工具类提供将密钥实例转换为JDK密钥实例的方法. </p>
 *
 * @author S.Violet
 */
public class SM2KeyGenerator {

    /**
     * 密钥类型:SM2
     */
    public static final String SM2_KEY_ALGORITHM = "SM2";

    /**
     * 随机生成SM2密钥对(sm2p256v1)
     *
     * @return 密钥对(与JDK的密钥实例不同)
     */
    public static SM2KeyParamsPair generateKeyParamsPair() {
        AsymmetricCipherKeyPair asymKeyPair = BaseBCAsymKeyGenerator.generateEcKeyParamsPair(SM2DefaultCurve.DOMAIN_PARAMS, null);
        return new SM2KeyParamsPair((ECPublicKeyParameters)asymKeyPair.getPublic(), (ECPrivateKeyParameters)asymKeyPair.getPrivate());
    }

    /**
     * 根据已知的D值生成ECC/SM2私钥实例(sm2p256v1)
     *
     * @param d D值
     * @return 私钥实例(与JDK的密钥实例不同)
     */
    public static ECPrivateKeyParameters generatePrivateKeyParams(BigInteger d) throws Exception {
        return BaseBCAsymKeyGenerator.parseEcPrivateKeyParams(SM2DefaultCurve.DOMAIN_PARAMS, d);
    }

    /**
     * 根据已知的坐标(ASN.1编码数据)生成ECC/SM2公钥实例(sm2p256v1)
     *
     * @param asn1Encoding 公钥坐标点(ASN.1编码数据)
     * @return 公钥实例(与JDK的密钥实例不同)
     */
    public static ECPublicKeyParameters generatePublicKeyParamsByASN1(byte[] asn1Encoding) throws Exception {
        return BaseBCAsymKeyGenerator.parseEcPublicKeyParams(SM2DefaultCurve.DOMAIN_PARAMS, asn1Encoding);
    }

    /**
     * 根据已知的坐标(X/Y)生成ECC/SM2公钥实例(sm2p256v1)
     *
     * @param xBytes 坐标X, 字节形式(bigInteger.toByteArray()获得)
     * @param yBytes 坐标Y, 字节形式(bigInteger.toByteArray()获得)
     * @return 公钥实例(与JDK的密钥实例不同)
     */
    public static ECPublicKeyParameters generatePublicKeyParams(byte[] xBytes, byte[] yBytes) throws Exception {
        return BaseBCAsymKeyGenerator.parseEcPublicKeyParams(SM2DefaultCurve.DOMAIN_PARAMS, xBytes, yBytes);
    }

    /**
     * 根据已知的坐标(X/Y)生成ECC/SM2公钥实例(sm2p256v1)
     *
     * @param x 坐标X
     * @param y 坐标Y
     * @return 公钥实例(与JDK的密钥实例不同)
     */
    public static ECPublicKeyParameters generatePublicKeyParams(BigInteger x, BigInteger y) throws Exception {
        if (x == null || y == null) {
            throw new NullPointerException("x or y is null");
        }
        return BaseBCAsymKeyGenerator.parseEcPublicKeyParams(SM2DefaultCurve.DOMAIN_PARAMS, x.toByteArray(), y.toByteArray());
    }

    public static class SM2KeyParamsPair {

        private ECPublicKeyParameters publicKeyParams;
        private ECPrivateKeyParameters privateKeyParams;

        public SM2KeyParamsPair(ECPublicKeyParameters publicKeyParams, ECPrivateKeyParameters privateKeyParams) {
            this.publicKeyParams = publicKeyParams;
            this.privateKeyParams = privateKeyParams;
        }

        /**
         * 获取公钥实例, 用于加解签操作(与JDK的密钥实例不同)
         */
        public ECPublicKeyParameters getPublicKeyParams() {
            return publicKeyParams;
        }

        /**
         * 获取私钥实例, 用于加解签操作(与JDK的密钥实例不同)
         */
        public ECPrivateKeyParameters getPrivateKeyParams() {
            return privateKeyParams;
        }

        /**
         * 获取公钥坐标点的ASN.1编码数据(非压缩)
         */
        public byte[] getPublicASN1Encoding(){
            return publicKeyParams.getQ().getEncoded(false);
        }

        /**
         * 获取私钥的D值
         */
        public BigInteger getPrivateD(){
            return privateKeyParams.getD();
        }

//        public byte[] getX509EncodedPublicKey() throws InvalidKeySpecException {
//            return encodePublicKeyToX509(publicKeyParams);
//        }
//
//        public byte[] getPKCS8EncodedPrivateKey() throws InvalidKeySpecException {
//            return encodePrivateKeyToPKCS8(privateKeyParams);
//        }
//
//        @Override
//        public String toString() {
//            try {
//                return "ECKeyPair\n<public>" + Base64Utils.encodeToString(getX509EncodedPublicKey()) + "\n<private>" + Base64Utils.encodeToString(getPKCS8EncodedPrivateKey());
//            } catch (InvalidKeySpecException e) {
//                return "ECKeyPair\n<exception>" + e.getMessage();
//            }
//        }

    }
}
