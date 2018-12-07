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
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.junit.Assert;
import org.junit.Test;
import sviolet.thistle.util.conversion.ByteUtils;
import sviolet.thistle.util.crypto.base.BaseBCAsymKeyGenerator;
import sviolet.thistle.util.crypto.base.CommonCryptoException;
import sviolet.thistle.util.crypto.base.SM2DefaultCurve;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.spec.InvalidKeySpecException;

public class SM2CipherTest {

    private static final String STRING = "English中文#$%@#$%@GSDFG654465rq43we5■☝▌▋卍¶¶¶☹ΥΥθΕサイけにケ◆♂‥√▒卍ЫПЬрпㅂㅝㅂ㉹㉯╠╕┚╜ㅛㅛ㉰㉯⑩⒅⑯413English中文#$%@#$%@GSDFG654465rq43we5■☝▌▋卍¶¶¶☹ΥΥθΕサイけにケ◆♂‥√▒卍ЫПЬрпㅂㅝㅂ㉹㉯╠╕┚╜ㅛㅛ㉰㉯⑩⒅⑯413English中文#$%@#$%@GSDFG654465rq43we5■☝▌▋卍¶¶¶☹ΥΥθΕサイけにケ◆♂‥√▒卍ЫПЬрпㅂㅝㅂ㉹㉯╠╕┚╜ㅛㅛ㉰㉯⑩⒅⑯413";

    @Test
    public void common() throws CommonCryptoException, InvalidKeySpecException, IOException, CryptoException {
        //生成随机密钥对
        SM2KeyGenerator.SM2KeyParamsPair keyPair = SM2KeyGenerator.generateKeyParamsPair();

//        System.out.println(keyPair);
//        System.out.println(keyPair.getPrivateD());
//        System.out.println(ByteUtils.bytesToHex(keyPair.getPublicASN1Encoding()));

        //用公私钥要素反向生成公私钥实例(D, Q), 忽略结果, 仅测试
        SM2KeyGenerator.generatePrivateKeyParams(keyPair.getPrivateD());
        SM2KeyGenerator.generatePublicKeyParamsByASN1(keyPair.getPublicASN1Encoding());

        //公私钥转为标准编码
        byte[] pkcs8 = keyPair.getPKCS8EncodedPrivateKey();
        byte[] x509 = keyPair.getX509EncodedPublicKey();

//        System.out.println(ByteUtils.bytesToHex(pkcs8));
//        System.out.println(ByteUtils.bytesToHex(x509));

        //转为PEM格式
        String privatePem = PEMEncodeUtils.sm2PrivateKeyToPEMEncoded(pkcs8);
        String publicPem = PEMEncodeUtils.sm2PublicKeyToPEMEncoded(x509);

//        System.out.println(privatePem);
//        System.out.println(publicPem);

        //标准编码转公私钥
        ECPrivateKeyParameters privateKeyParams = SM2KeyGenerator.generatePrivateKeyParamsByPKCS8(pkcs8);
        ECPublicKeyParameters publicKeyParams = SM2KeyGenerator.generatePublicKeyParamsByX509(x509);

        //加密
        byte[] encrypted = SM2Cipher.encrypt(STRING.getBytes("UTF-8"), publicKeyParams, SM2Cipher.CRYPTO_ALGORITHM_SM2);

//        System.out.println(ByteUtils.bytesToHex(encrypted));

        //解密
        String decrypted = new String(SM2Cipher.decrypt(encrypted, privateKeyParams, SM2Cipher.CRYPTO_ALGORITHM_SM2), "UTF-8");
        Assert.assertEquals(STRING, decrypted);

//        System.out.println(decrypted);

        //加密
        encrypted = SM2Cipher.encryptToC1C3C2(STRING.getBytes("UTF-8"), publicKeyParams, SM2Cipher.CRYPTO_ALGORITHM_SM2);

//        System.out.println(ByteUtils.bytesToHex(encrypted));

        //解密
        decrypted = new String(SM2Cipher.decryptFromC1C3C2(encrypted, privateKeyParams, SM2Cipher.CRYPTO_ALGORITHM_SM2), "UTF-8");
        Assert.assertEquals(STRING, decrypted);

        //加密
        encrypted = SM2Cipher.encryptToDER(STRING.getBytes("UTF-8"), publicKeyParams, SM2Cipher.CRYPTO_ALGORITHM_SM2);

//        System.out.println(ByteUtils.bytesToHex(encrypted));

        //解密
        decrypted = new String(SM2Cipher.decryptFromDER(encrypted, privateKeyParams, SM2Cipher.CRYPTO_ALGORITHM_SM2), "UTF-8");
        Assert.assertEquals(STRING, decrypted);

//        System.out.println(decrypted);

        byte[] sign = SM2Cipher.sign(STRING.getBytes("UTF-8"), null, privateKeyParams, SM2Cipher.SIGN_ALGORITHM_SM2_SM3);
        boolean result = SM2Cipher.verify(STRING.getBytes("UTF-8"), sign, null, publicKeyParams, SM2Cipher.SIGN_ALGORITHM_SM2_SM3);
        Assert.assertTrue(result);

        //加签验签
        sign = SM2Cipher.signToRS(new ByteArrayInputStream(STRING.getBytes("UTF-8")), null, privateKeyParams, SM2Cipher.SIGN_ALGORITHM_SM2_SM3);
        result = SM2Cipher.verifyFromRS(new ByteArrayInputStream(STRING.getBytes("UTF-8")), sign, null, publicKeyParams, SM2Cipher.SIGN_ALGORITHM_SM2_SM3);
        Assert.assertTrue(result);

        //特殊:openssl可识别的SEC1标准的私钥数据
        byte[] sec1 = BaseBCAsymKeyGenerator.encodePkcs8ToSec1(pkcs8);
        byte[] pkcs8New = BaseBCAsymKeyGenerator.encodeSec1ToPkcs8(SM2DefaultCurve.EC_PARAM_SPEC_FOR_SEC1, sec1);

//        System.out.println(ByteUtils.bytesToHex(sec1));
//        System.out.println(ByteUtils.bytesToHex(pkcs8New));

        Assert.assertArrayEquals(pkcs8, pkcs8New);

    }

}
