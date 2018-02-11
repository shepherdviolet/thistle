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

import org.junit.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

public class PKCS12KeyStoreTest {

    @Test
    public void common() throws CertificateException, SignatureException, InvalidKeyException, IOException, KeyStoreException, NoSuchAlgorithmException, InvalidKeySpecException {

        CertificateUtils.X509CertificateAndKey certificateAndKey = CertificateUtils.generateX509RootCertificate(
                "CN=Thistle test ca, OU=Thistle group, O=Violet Shell, L=Ningbo, ST=Zhejiang, C=CN",
                1024,
                3650,
                CertificateUtils.SIGN_ALGORITHM_RSA_SHA256
        );

        CertificateUtils.X509CertificateAndKey certificateAndKey2 = CertificateUtils.generateX509Certificate(
                "CN=Thistle test subject, OU=Thistle group, O=Violet Shell, L=Ningbo, ST=Zhejiang, C=CN",
                1024,
                3650,
                CertificateUtils.SIGN_ALGORITHM_RSA_SHA256,
                certificateAndKey.getCertificate(),
                certificateAndKey.getPrivateKey()
        );

        PKCS12KeyStoreUtils.storeCertificateAndKey(
                "./out/test-case/pkcs12-test.p12",
                "000000",
                "Thistle test subject alias",
                certificateAndKey2.getPrivateKey(),
                certificateAndKey2.getCertificate());

        PKCS12KeyStoreUtils.storeCertificateAndKey(
                "./out/test-case/pkcs12-test-ca.p12",
                "000000",
                "Thistle test ca alias",
                null,
                certificateAndKey.getCertificate());

    }

}
