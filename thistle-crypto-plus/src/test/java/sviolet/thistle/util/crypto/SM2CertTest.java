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

import org.bouncycastle.operator.OperatorCreationException;
import org.junit.Test;
import sviolet.thistle.util.conversion.Base64Utils;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;

public class SM2CertTest {

    @Test
    public void common() throws OperatorCreationException, CertificateException, InvalidKeySpecException, NoSuchAlgorithmException, IOException, InvalidKeyException, NoSuchProviderException, SignatureException {
        //生成随机密钥对
        SM2KeyGenerator.SM2KeyParamsPair rootKeyPair = SM2KeyGenerator.generateKeyParamsPair();

        //生成根证书
        X509Certificate rootCert = AdvancedCertificateUtils.generateSm2X509RootCertificate(
                "CN=Test CA, OU=IT Dept, O=My Company, L=Ningbo, ST=Zhejiang, C=CN",
                3650,
                rootKeyPair.getPublicKeyParams(),
                rootKeyPair.getPrivateKeyParams());

        //简单验证证书
        rootCert.checkValidity(new Date());
        rootCert.verify(rootKeyPair.getJdkPublicKey());

        System.out.println(PEMEncodeUtils.certificateToPEMEncoded(AdvancedCertificateUtils.parseCertificateToEncoded(rootCert)));

        //生成随机密钥对
        SM2KeyGenerator.SM2KeyParamsPair userKeyPair = SM2KeyGenerator.generateKeyParamsPair();

        //产生CSR
        byte[] csr = AdvancedCertificateUtils.generateSm2Csr(
                "CN=Test User, OU=IT Dept, O=My Company, L=Ningbo, ST=Zhejiang, C=CN",
                userKeyPair.getPublicKeyParams(),
                userKeyPair.getPrivateKeyParams());

        //生成用户证书
        X509Certificate userCert = AdvancedCertificateUtils.generateSm2X509Certificate(
                csr,
                3650,
                rootCert,
                rootKeyPair.getPrivateKeyParams());

        //简单验证证书
        userCert.checkValidity(new Date());
        userCert.verify(rootKeyPair.getJdkPublicKey());

        System.out.println(PEMEncodeUtils.certificateToPEMEncoded(AdvancedCertificateUtils.parseCertificateToEncoded(userCert)));

    }

}
