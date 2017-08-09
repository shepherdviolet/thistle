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

import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * RSA秘钥生成工具
 */
public class RSAKeyGenerator {

    public static final String RSA_KEY_ALGORITHM = "RSA";

    /**
     * 随机生成RSA密钥对(2048位)
     *
     * @return 密钥对
     */
    public static RSAKeyPair generateKeyPair() {
        return generateKeyPair(2048);
    }

    /**
     * 随机生成RSA密钥对
     *
     * @param bits 秘钥位数(默认1024)
     * @return 密钥对
     */
    public static RSAKeyPair generateKeyPair(int bits) {
        KeyPairGenerator keyPairGen;
        try {
            keyPairGen = KeyPairGenerator.getInstance(RSA_KEY_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        keyPairGen.initialize(bits);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        return new RSAKeyPair((RSAPublicKey) keyPair.getPublic(), (RSAPrivateKey) keyPair.getPrivate());
    }

    /**
     * <p>根据X509格式的公钥数据生成公钥</p>
     *
     * @param x509EncodedPublicKey X509格式公钥数据
     * @return 公钥
     */
    public static RSAPublicKey generatePublicKeyByX509(byte[] x509EncodedPublicKey) throws InvalidKeySpecException {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(x509EncodedPublicKey);
        KeyFactory factory;
        try {
            factory = KeyFactory.getInstance(RSA_KEY_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return (RSAPublicKey) factory.generatePublic(keySpec);
    }

    /**
     * <p>将秘钥转为bytes, 具体编码根据Key的编码类型决定</p>
     */
    public static byte[] parseKeyToBytes(Key key){
        if (key == null){
            return null;
        }
        return key.getEncoded();
    }

    /**
     * <p>根据PKCS8格式的私钥数据生成私钥</p>
     *
     * @param pkcs8EncodedPrivateKey PKCS8格式私钥数据
     * @return 私钥
     */
    public static RSAPrivateKey generatePrivateKeyByPKCS8(byte[] pkcs8EncodedPrivateKey) throws InvalidKeySpecException {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8EncodedPrivateKey);
        KeyFactory factory;
        try {
            factory = KeyFactory.getInstance(RSA_KEY_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return (RSAPrivateKey) factory.generatePrivate(keySpec);
    }

    /**
     * 用模和指数生成RSA公钥
     *
     * @param modulus  模
     * @param exponent 指数
     */
    public static RSAPublicKey generatePublicKey(BigInteger modulus, BigInteger exponent) throws InvalidKeySpecException {
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance(RSA_KEY_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus, exponent);
        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }

    /**
     * 用模和指数生成RSA私钥
     *
     * @param modulus  模
     * @param exponent 指数
     */
    public static RSAPrivateKey generatePrivateKey(BigInteger modulus, BigInteger exponent) throws InvalidKeySpecException {
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance(RSA_KEY_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(modulus, exponent);
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }

    public static class RSAKeyPair {

        private RSAPublicKey publicKey = null;
        private RSAPrivateKey privateKey = null;

        public RSAKeyPair(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }

        /**
         * 获取公钥
         */
        public RSAPublicKey getPublicKey() {
            return publicKey;
        }

        /**
         * 获取私钥
         */
        public RSAPrivateKey getPrivateKey() {
            return privateKey;
        }

        /**
         * 获取模数
         */
        public BigInteger getModulus() {
            return publicKey.getModulus();
        }

        /**
         * 获取公钥指数
         */
        public BigInteger getPublicExponent() {
            return publicKey.getPublicExponent();
        }

        /**
         * 获取私钥指数
         */
        public BigInteger getPrivateExponent() {
            return privateKey.getPrivateExponent();
        }

        public byte[] getX509EncodedPublicKey() throws InvalidKeySpecException {
            KeyFactory factory;
            try {
                factory = KeyFactory.getInstance(RSA_KEY_ALGORITHM);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            return factory.getKeySpec(publicKey, X509EncodedKeySpec.class).getEncoded();
        }

        public byte[] getPKCS8EncodedPrivateKey() throws InvalidKeySpecException {
            KeyFactory factory;
            try {
                factory = KeyFactory.getInstance(RSA_KEY_ALGORITHM);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            return factory.getKeySpec(privateKey, PKCS8EncodedKeySpec.class).getEncoded();
        }

    }
}
