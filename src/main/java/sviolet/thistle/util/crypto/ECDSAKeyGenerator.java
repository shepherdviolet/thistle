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

import org.jetbrains.annotations.Nullable;
import sviolet.thistle.util.conversion.Base64Utils;

import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * ECDSA秘钥生成工具
 */
public class ECDSAKeyGenerator {

    public static final String ECDSA_KEY_ALGORITHM = "EC";

    /**
     * 随机生成ECDSA密钥对(secp256r1:256位)
     *
     * @return 密钥对
     */
    public static ECKeyPair generateKeyPair() {
        KeyPairGenerator keyPairGen;
        try {
            keyPairGen = KeyPairGenerator.getInstance(ECDSA_KEY_ALGORITHM);
            keyPairGen.initialize(new ECGenParameterSpec("secp256r1"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
        KeyPair keyPair = keyPairGen.generateKeyPair();
        return new ECKeyPair((ECPublicKey) keyPair.getPublic(), (ECPrivateKey) keyPair.getPrivate());
    }

    /**
     * <p>根据X509格式的公钥数据生成公钥</p>
     *
     * @param x509EncodedPublicKey X509格式公钥数据
     * @return 公钥
     */
    public static ECPublicKey generatePublicKeyByX509(byte[] x509EncodedPublicKey) throws InvalidKeySpecException {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(x509EncodedPublicKey);
        KeyFactory factory;
        try {
            factory = KeyFactory.getInstance(ECDSA_KEY_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return (ECPublicKey) factory.generatePublic(keySpec);
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
    public static ECPrivateKey generatePrivateKeyByPKCS8(byte[] pkcs8EncodedPrivateKey) throws InvalidKeySpecException {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8EncodedPrivateKey);
        KeyFactory factory;
        try {
            factory = KeyFactory.getInstance(ECDSA_KEY_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return (ECPrivateKey) factory.generatePrivate(keySpec);
    }

    public static class ECKeyPair {

        private ECPublicKey publicKey = null;
        private ECPrivateKey privateKey = null;

        public ECKeyPair(ECPublicKey publicKey, ECPrivateKey privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }

        /**
         * 获取公钥
         */
        public ECPublicKey getPublicKey() {
            return publicKey;
        }

        /**
         * 获取私钥
         */
        public ECPrivateKey getPrivateKey() {
            return privateKey;
        }

        @Nullable
        public byte[] getX509EncodedPublicKey() throws InvalidKeySpecException {
            if (publicKey == null){
                return null;
            }

            KeyFactory factory;
            try {
                factory = KeyFactory.getInstance(ECDSA_KEY_ALGORITHM);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            return factory.getKeySpec(publicKey, X509EncodedKeySpec.class).getEncoded();
        }

        @Nullable
        public byte[] getPKCS8EncodedPrivateKey() throws InvalidKeySpecException {
            if (privateKey == null){
                return null;
            }

            KeyFactory factory;
            try {
                factory = KeyFactory.getInstance(ECDSA_KEY_ALGORITHM);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            return factory.getKeySpec(privateKey, PKCS8EncodedKeySpec.class).getEncoded();
        }

        @Override
        public String toString() {
            try {
                return "ECKeyPair\n<public>" + Base64Utils.encodeToString(getX509EncodedPublicKey()) + "\n<private>" + Base64Utils.encodeToString(getPKCS8EncodedPrivateKey());
            } catch (InvalidKeySpecException e) {
                return "ECKeyPair\n<exception>" + e.getMessage();
            }
        }
    }
}
