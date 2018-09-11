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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

/**
 * 证书处理基本逻辑<p>
 *
 * Not recommended for direct use<p>
 *
 * 不建议直接使用<p>
 *
 * @author S.Violet
 */
public class BaseCertificateUtils {

    public static final String TYPE_X509 = "X.509";

    /**
     * <p>解析证书, 返回Certificate对象, 可用来获取证书公钥实例等</p>
     * @param certData 证书数据
     * @param type 证书数据格式, 例如X.509
     */
    public static Certificate parseCertificate(byte[] certData, String type) throws CertificateException {
        return parseCertificate(new ByteArrayInputStream(certData), type);
    }

    /**
     * <p>解析证书, 返回Certificate对象, 可用来获取证书公钥实例等</p>
     * @param inputStream 证书数据流, 会被close掉
     * @param type 证书数据格式, 例如X.509
     */
    public static Certificate parseCertificate(InputStream inputStream, String type) throws CertificateException {
        try {
            CertificateFactory factory = CertificateFactory.getInstance(type);
            return factory.generateCertificate(inputStream);
        } finally {
            try {
                inputStream.close();
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * 将证书编码为二进制数据
     * @param certificate 证书
     * @return 二进制数据
     */
    public static byte[] encodeCertificate(Certificate certificate) throws CertificateEncodingException {
        if (certificate == null){
            return null;
        }
        return certificate.getEncoded();
    }

}