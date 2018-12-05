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

import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.junit.Test;

public class SM2CipherTest {

    @Test
    public void key() throws Exception {
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

        //转为PEM格式
        String privatePem = PEMEncodeUtils.sm2PrivateKeyToPEMEncoded(pkcs8);
        String publicPem = PEMEncodeUtils.sm2PublicKeyToPEMEncoded(x509);

//        System.out.println(privatePem);
//        System.out.println(publicPem);

        ECPrivateKeyParameters privateKeyParams = SM2KeyGenerator.generatePrivateKeyParamsByPKCS8(pkcs8);
        ECPublicKeyParameters publicKeyParams = SM2KeyGenerator.generatePublicKeyParamsByX509(x509);


    }

}
