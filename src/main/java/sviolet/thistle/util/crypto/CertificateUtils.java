/*
 * Copyright (C) 2015-2017 S.Violet
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

import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;

import static sviolet.thistle.util.crypto.RSAKeyGenerator.RSA_KEY_ALGORITHM;

/**
 * <p>证书工具</p>
 *
 * @author S.Violet
 */

public class CertificateUtils {

    public static final String KEK_ALGORITHM_RSA = "RSA";

    public static final String SIGN_ALGORITHM_RSA_MD5 = "MD5withRSA";
    public static final String SIGN_ALGORITHM_RSA_SHA1 = "SHA1withRSA";
    public static final String SIGN_ALGORITHM_RSA_SHA256 = "SHA256withRSA";

    //parse//////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * <p>解析X509格式的证书, 返回Certificate对象, 可用来获取证书公钥实例等</p>
     * @param certData X509格式证书数据
     */
    public static Certificate parseX509ToCertificate(byte[] certData) throws CertificateException {
        return parseX509ToCertificate(new ByteArrayInputStream(certData));
    }

    /**
     * <p>解析X509格式的证书, 返回Certificate对象, 可用来获取证书公钥实例等</p>
     * @param inputStream X509格式证书数据流, 会被close掉
     */
    public static Certificate parseX509ToCertificate(InputStream inputStream) throws CertificateException {
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            return factory.generateCertificate(inputStream);
        } finally {
            try {
                inputStream.close();
            } catch (Exception ignore) {
            }
        }
    }

    //generate////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 生成根证书(自签名证书)
     *
     * <p>
     * CN=(名称或域名),
     * OU=(部门名称),
     * O=(组织名称),
     * L=(城市或区域名称),
     * ST=(州或省份名称),
     * C=(国家代码)
     * </p>
     *
     * @param name X500 Name, Example:CN=Test CA, OU=IT Dep, O=My Company, L=Ningbo, ST=Zhejiang, C=CN
     * @param bits 密钥位数1024/2048
     * @param validity 有效期(天), Example:3650
     * @param signAlgorithm 签名算法, CertificateUtils.SIGN_ALGORITHM_RSA_SHA256
     */
    public static X509CertificateAndKey generateX509RootCertificate(String name, int bits, int validity, String signAlgorithm) throws IOException, InvalidKeyException, CertificateException, SignatureException {
        try {
            X500Name x500Name = new X500Name(name);
            CertAndKeyGen rootCertAndKeyGen = new CertAndKeyGen(KEK_ALGORITHM_RSA, signAlgorithm, null);
            rootCertAndKeyGen.setRandom(BaseKeyGenerator.getSecureRandom());
            rootCertAndKeyGen.generate(bits);
            return new X509CertificateAndKey(rootCertAndKeyGen.getSelfCertificate(x500Name, validity * 24L * 60L * 60L), rootCertAndKeyGen.getPrivateKey());
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 生成证书(由CA证书私钥颁发)
     *
     * <p>
     * CN=(名称或域名),
     * OU=(部门名称),
     * O=(组织名称),
     * L=(城市或区域名称),
     * ST=(州或省份名称),
     * C=(国家代码)
     * </p>
     *
     * @param name X500 Name, Example:CN=Test CA, OU=IT Dep, O=My Company, L=Ningbo, ST=Zhejiang, C=CN
     * @param bits 密钥位数1024/2048
     * @param validity 有效期(天), Example:3650
     * @param signAlgorithm 签名算法, CertificateUtils.SIGN_ALGORITHM_RSA_SHA256
     * @param caCertificate CA证书
     * @param caPrivateKey CA私钥
     */
    public static X509CertificateAndKey generateX509Certificate(String name, int bits, int validity, String signAlgorithm, X509Certificate caCertificate, PrivateKey caPrivateKey) throws InvalidKeyException, IOException, CertificateException, SignatureException {
        try {
            X500Name x500Name = new X500Name(name);
            CertAndKeyGen certAndKeyGen = new CertAndKeyGen(KEK_ALGORITHM_RSA, signAlgorithm, null);
            certAndKeyGen.setRandom(BaseKeyGenerator.getSecureRandom());
            certAndKeyGen.generate(bits);
            X509CertInfo x509CertInfo = new X509CertInfo();
            x509CertInfo.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
            x509CertInfo.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(BaseKeyGenerator.getSecureRandom().nextInt() & 2147483647));
            x509CertInfo.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(AlgorithmId.get(signAlgorithm)));
            x509CertInfo.set(X509CertInfo.SUBJECT, x500Name);
            x509CertInfo.set(X509CertInfo.KEY, new CertificateX509Key(certAndKeyGen.getPublicKey()));
            x509CertInfo.set(X509CertInfo.VALIDITY, new CertificateValidity(new Date(), new Date(System.currentTimeMillis() + validity * 24L * 60L * 60L * 1000L)));
            x509CertInfo.set(X509CertInfo.ISSUER, new X500Name(caCertificate.getSubjectDN().getName()));
            X509CertImpl certificate = new X509CertImpl(x509CertInfo);
            certificate.sign(caPrivateKey, signAlgorithm);
            return new X509CertificateAndKey(certificate, certAndKeyGen.getPrivateKey());
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 证书与私钥
     */
    public static class X509CertificateAndKey {

        private X509Certificate certificate;
        private PrivateKey privateKey;

        private X509CertificateAndKey(X509Certificate certificate, PrivateKey privateKey) {
            this.certificate = certificate;
            this.privateKey = privateKey;
        }

        public X509Certificate getCertificate() {
            return certificate;
        }

        public PrivateKey getPrivateKey() {
            return privateKey;
        }

        public byte[] getX509EncodedCertificate() throws CertificateEncodingException {
            if (certificate == null){
                return null;
            }
            return certificate.getEncoded();
        }

        public byte[] getPKCS8EncodedPrivateKey() throws InvalidKeySpecException {
            if (privateKey == null){
                return null;
            }

            KeyFactory factory;
            try {
                factory = KeyFactory.getInstance(RSA_KEY_ALGORITHM);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            return factory.getKeySpec(privateKey, PKCS8EncodedKeySpec.class).getEncoded();
        }
    }

    //Bouncy Castle///////////////////////////////////////////////////////////////////////////////////////////

    /**
     *  <p><pre>
     *  证书版本:cert.getVersion()
     *  序列号:cert.getSerialNumber().getValue().toString(16)
     *  算法标识:cert.getSignatureAlgorithm().getObjectId().getId()
     *  签发者:cert.getIssuer()
     *  开始时间:cert.getStartDate().getTime()
     *  结束时间:cert.getEndDate().getTime()
     *  主体名:cert.getSubject()
     *  签名值:cert.getSignature().getBytes()
     *
     *  主体公钥:SubjectPublicKeyInfo publicInfo = cert.getSubjectPublicKeyInfo();
     *  标识符:publicInfo.getAlgorithmId().getObjectId().getId()
     *  公钥值:publicInfo.getPublicKeyData().getBytes()
     *  </pre></p>
     *
     *  <p>
     *  标识符:<Br>
     *  <pre>
     *  rsaEncryption	RSA算法标识	1.2.840.113549.1.1.1
     *  sha1withRSAEncryption	SHA1的RSA签名	1.2.840.113549.1.1.5
     *  ECC	ECC算法标识	1.2.840.10045.2.1
     *  SM2	SM2算法标识	1.2.156.10197.1.301
     *  SM3WithSM2	SM3的SM2签名	1.2.156.10197.1.501
     *  sha1withSM2	SHA1的SM2签名	1.2.156.10197.1.502
     *  sha256withSM2	SHA256的SM2签名	1.2.156.10197.1.503
     *  sm3withRSAEncryption	SM3的RSA签名	1.2.156.10197.1.504
     *  commonName	主体名	2.5.4.3
     *  emailAddress	邮箱	1.2.840.113549.1.9.1
     *  cRLDistributionPoints	CRL分发点	2.5.29.31
     *  extKeyUsage	扩展密钥用法	2.5.29.37
     *  subjectAltName	使用者备用名称	2.5.29.17
     *  CP	证书策略	2.5.29.32
     *  clientAuth	客户端认证	1.3.6.1.5.5.7.3.2
     *  </pre></p>
     *
     * @param certBase64 X509证书数据, ASN.1编码, Base64编码
     */
//    public static X509CertificateStructure parseX509ToStructure (String certBase64) throws IOException {
//        return parseX509ToStructure(Base64Utils.decode(certBase64));
//    }

    /**
     * <p><pre>
     *  证书版本:cert.getVersion()
     *  序列号:cert.getSerialNumber().getValue().toString(16)
     *  算法标识:cert.getSignatureAlgorithm().getObjectId().getId()
     *  签发者:cert.getIssuer()
     *  开始时间:cert.getStartDate().getTime()
     *  结束时间:cert.getEndDate().getTime()
     *  主体名:cert.getSubject()
     *  签名值:cert.getSignature().getBytes()
     *
     *  主体公钥:SubjectPublicKeyInfo publicInfo = cert.getSubjectPublicKeyInfo();
     *  标识符:publicInfo.getAlgorithmId().getObjectId().getId()
     *  公钥值:publicInfo.getPublicKeyData().getBytes()
     *  </pre></p>
     *
     *  <p>
     *  标识符:<Br>
     *  <pre>
     *  rsaEncryption	RSA算法标识	1.2.840.113549.1.1.1
     *  sha1withRSAEncryption	SHA1的RSA签名	1.2.840.113549.1.1.5
     *  ECC	ECC算法标识	1.2.840.10045.2.1
     *  SM2	SM2算法标识	1.2.156.10197.1.301
     *  SM3WithSM2	SM3的SM2签名	1.2.156.10197.1.501
     *  sha1withSM2	SHA1的SM2签名	1.2.156.10197.1.502
     *  sha256withSM2	SHA256的SM2签名	1.2.156.10197.1.503
     *  sm3withRSAEncryption	SM3的RSA签名	1.2.156.10197.1.504
     *  commonName	主体名	2.5.4.3
     *  emailAddress	邮箱	1.2.840.113549.1.9.1
     *  cRLDistributionPoints	CRL分发点	2.5.29.31
     *  extKeyUsage	扩展密钥用法	2.5.29.37
     *  subjectAltName	使用者备用名称	2.5.29.17
     *  CP	证书策略	2.5.29.32
     *  clientAuth	客户端认证	1.3.6.1.5.5.7.3.2
     *  </pre></p>
     *
     * @param certData X509证书数据, ASN.1编码, 非Base64编码
     */
//    public static X509CertificateStructure parseX509ToStructure (byte[] certData) throws IOException {
//        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(certData);
//        ASN1InputStream asn1InputStream = new ASN1InputStream(byteArrayInputStream);
//        ASN1Sequence seq = (ASN1Sequence) asn1InputStream.readObject();
//        return new X509CertificateStructure(seq);
//    }

}
