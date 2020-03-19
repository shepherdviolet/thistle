/*
 * Copyright (C) 2015-2020 S.Violet
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

import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.junit.Test;
import sviolet.thistle.util.conversion.Base64Utils;
import sviolet.thistle.util.crypto.base.BouncyCastleProviderUtils;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

/**
 * 一些其他的示例
 *
 * @author S.Violet
 */
public class OtherSamples {

    static {
        BouncyCastleProviderUtils.installProvider();
    }

    /**
     * 解析ASN1格式数据 (DER/PEM/X509等标准的基础数据格式)
     */
    @Test
    public void parseASN1Sequence() throws IOException {
        String asn1Data = "MIICOTCCAeACAQAwgaExGDAWBgNVBAMMD1RoaXN0bGUgdGVzdCBjYTEWMBQGA1UECwwNVGhpc3RsZSBncm91cDEVMBMGA1UECgwMVmlvbGV0IFNoZWxsMQ8wDQYDVQQHDAZOaW5nYm8xETAPBgNVBAgMCFpoZWppYW5nMQswCQYDVQQGEwJDTjElMCMGCSqGSIb3DQEJARYWc2hlcGhlcmR2aW9sZXRAMTYzLmNvbTCCATMwgewGByqGSM49AgEwgeACAQEwLAYHKoZIzj0BAQIhAP////7/////////////////////AAAAAP//////////MEQEIP////7/////////////////////AAAAAP/////////8BCAo6fqenZ9eNE1ankvPZQmn85eJ9RWrj5LdvL1BTZQOkwRBBDLEriwfGYEZX5kERmo5yZSP4wu/8mYL4XFaRYkzTHTHvDc2ovT2d5xZvc7ja2khU9Cph3zGKkdAAt8y5SE58KACIQD////+////////////////cgPfayHGBStTu/QJOdVBIwIBAQNCAASnKbIz8EtduGKxzYB25zWkmGw81zv3zaQrGf3OnmfKBe7HY433ctNFYAxRElJJc0uWLkjk7QzJK2VAnlzwzA4yoAAwCgYIKoEcz1UBg3UDRwAwRAIgS7NyaP77/tAWUgbk0ZljU3p0wwSfJoS6zdHXnSaIl5sCIEC7/93hejEde1qEby40YrKT5GCj9z//hdj0lcc3izfy";
        ASN1Sequence seq = ASN1Sequence.getInstance(ASN1Primitive.fromByteArray(Base64Utils.decode(asn1Data)));
//        System.out.println(seq);
    }

    /**
     * 解析PKCS10证书申请数据
     */
    @Test
    public void parsePKCS10CertificationRequest() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        String pkcs10Data = "MIICOTCCAeACAQAwgaExGDAWBgNVBAMMD1RoaXN0bGUgdGVzdCBjYTEWMBQGA1UECwwNVGhpc3RsZSBncm91cDEVMBMGA1UECgwMVmlvbGV0IFNoZWxsMQ8wDQYDVQQHDAZOaW5nYm8xETAPBgNVBAgMCFpoZWppYW5nMQswCQYDVQQGEwJDTjElMCMGCSqGSIb3DQEJARYWc2hlcGhlcmR2aW9sZXRAMTYzLmNvbTCCATMwgewGByqGSM49AgEwgeACAQEwLAYHKoZIzj0BAQIhAP////7/////////////////////AAAAAP//////////MEQEIP////7/////////////////////AAAAAP/////////8BCAo6fqenZ9eNE1ankvPZQmn85eJ9RWrj5LdvL1BTZQOkwRBBDLEriwfGYEZX5kERmo5yZSP4wu/8mYL4XFaRYkzTHTHvDc2ovT2d5xZvc7ja2khU9Cph3zGKkdAAt8y5SE58KACIQD////+////////////////cgPfayHGBStTu/QJOdVBIwIBAQNCAASnKbIz8EtduGKxzYB25zWkmGw81zv3zaQrGf3OnmfKBe7HY433ctNFYAxRElJJc0uWLkjk7QzJK2VAnlzwzA4yoAAwCgYIKoEcz1UBg3UDRwAwRAIgS7NyaP77/tAWUgbk0ZljU3p0wwSfJoS6zdHXnSaIl5sCIEC7/93hejEde1qEby40YrKT5GCj9z//hdj0lcc3izfy";
        JcaPKCS10CertificationRequest request = new JcaPKCS10CertificationRequest(Base64Utils.decode(pkcs10Data));
        PublicKey pk = request.getPublicKey();
//        System.out.println(pk);
    }

    /**
     * 生成PKCS10证书申请数据
     */
    @Test
    public void buildPKCS10CertificationRequest() throws NoSuchAlgorithmException, IOException, OperatorCreationException {
        SM2KeyGenerator.SM2KeyParamsPair keyPair = SM2KeyGenerator.generateKeyParamsPair();
        X500NameBuilder x500NameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
        x500NameBuilder.addRDN(BCStyle.CN, "Thistle test ca");
        x500NameBuilder.addRDN(BCStyle.OU, "Thistle group");
        x500NameBuilder.addRDN(BCStyle.O, "Violet Shell");
        x500NameBuilder.addRDN(BCStyle.L, "Ningbo");
        x500NameBuilder.addRDN(BCStyle.ST, "Zhejiang");
        x500NameBuilder.addRDN(BCStyle.C, "CN");
        x500NameBuilder.addRDN(BCStyle.EmailAddress, "shepherdviolet@163.com");
        JcaPKCS10CertificationRequestBuilder pkcs10CertificationRequestBuilder = new JcaPKCS10CertificationRequestBuilder(x500NameBuilder.build(), keyPair.getJdkPublicKey());
        JcaContentSignerBuilder contentSignerBuilder = new JcaContentSignerBuilder("SM3withSM2");
        ContentSigner contentSigner = contentSignerBuilder.build(keyPair.getJdkPrivateKey());
        PKCS10CertificationRequest pkcs10CertificationRequest = pkcs10CertificationRequestBuilder.build(contentSigner);// PKCS10的请求
//        System.out.println(Base64Utils.encodeToString(pkcs10CertificationRequest.getEncoded()));
    }

}
