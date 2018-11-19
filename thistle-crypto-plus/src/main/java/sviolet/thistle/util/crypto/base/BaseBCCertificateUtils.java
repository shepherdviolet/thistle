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

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509CertificateStructure;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.operator.*;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

/**
 * 证书处理基本逻辑Bouncy Castle<p>
 *
 * Not recommended for direct use<p>
 *
 * 不建议直接使用<p>
 *
 * @author S.Violet
 */
public class BaseBCCertificateUtils {

    private static final SignatureAlgorithmIdentifierFinder SIGNATURE_ALGORITHM_IDENTIFIER_FINDER = new DefaultSignatureAlgorithmIdentifierFinder();
    private static final DigestAlgorithmIdentifierFinder DIGEST_ALGORITHM_IDENTIFIER_FINDER = new DefaultDigestAlgorithmIdentifierFinder();

    private static final ReversedBCStyle REVERSED_BC_STYLE = new ReversedBCStyle();

    /**
     * 倒序的BCStyle, 因为BouncyCastle生成证书后DN信息是颠倒的, 为了保持原顺序, 我们在这里做一下倒序
     */
    private static class ReversedBCStyle extends BCStyle {
        @Override
        public RDN[] fromString(String dirName) {
            RDN[] rdns = super.fromString(dirName);
            if (rdns != null && rdns.length > 1){
                RDN temp;
                for (int i = 0 ; i < rdns.length / 2 ; i++){
                    temp = rdns[i];
                    rdns[i] = rdns[rdns.length - 1 - i];
                    rdns[rdns.length - 1 - i] = temp;
                }
            }
            return rdns;
        }
    }

    /**
     * 生成RSA根证书(自签名证书)
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
     * @param dn 证书的DN信息, 例:CN=Test CA, OU=IT Dept, O=My Company, L=Ningbo, ST=Zhejiang, C=CN
     * @param publicKey 证书的RSA公钥
     * @param privateKey 证书的RSA私钥(自签名)
     * @param validity 证书的有效期(天), 例:3650
     * @param signAlgorithm 签名算法, 例如:SHA256withRSA
     *
     */
    public static X509Certificate generateRSAX509RootCertificate(String dn, RSAPublicKey publicKey, RSAPrivateKey privateKey, int validity, String signAlgorithm) throws IOException, CertificateException, OperatorCreationException {
        return generateRSAX509Certificate(dn, publicKey, validity, signAlgorithm, null, privateKey);
    }

    /**
     * 生成RSA证书(由CA证书私钥颁发)
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
     * @param subjectDn 申请证书的DN信息, 例:CN=Test CA, OU=IT Dept, O=My Company, L=Ningbo, ST=Zhejiang, C=CN
     * @param subjectPublicKey 申请证书的RSA公钥
     * @param subjectValidity 申请证书的有效期(天), 例:3650
     * @param signAlgorithm 签名算法, 例如:SHA256withRSA
     * @param caCertificate CA的证书
     * @param caPrivateKey CA的RSA私钥
     */
    public static X509Certificate generateRSAX509Certificate(String subjectDn, RSAPublicKey subjectPublicKey, int subjectValidity, String signAlgorithm, X509Certificate caCertificate, RSAPrivateKey caPrivateKey) throws IOException, CertificateException, OperatorCreationException {
        //certificate builder
        X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(
                //issuer dn
                new X500Name(REVERSED_BC_STYLE, caCertificate != null ? caCertificate.getSubjectX500Principal().toString() : subjectDn),
                //serial
                BigInteger.probablePrime(32, BaseKeyGenerator.getSystemSecureRandom()),
                //start date
                new Date(),
                //expire date
                new Date(System.currentTimeMillis() + subjectValidity * 24L * 60L * 60L * 1000L),
                //subject dn
                new X500Name(REVERSED_BC_STYLE, subjectDn),
                //public key
                SubjectPublicKeyInfo.getInstance(new ASN1InputStream(subjectPublicKey.getEncoded()).readObject()));

        //sign
        AlgorithmIdentifier signAlgorithmId = SIGNATURE_ALGORITHM_IDENTIFIER_FINDER.find(signAlgorithm);
        AlgorithmIdentifier digestAlgorithmId = DIGEST_ALGORITHM_IDENTIFIER_FINDER.find(signAlgorithmId);
        AsymmetricKeyParameter asymmetricKeyParameter = PrivateKeyFactory.createKey(caPrivateKey.getEncoded());
        ContentSigner contentSigner = new BcRSAContentSignerBuilder(signAlgorithmId, digestAlgorithmId).build(asymmetricKeyParameter);
        X509CertificateHolder holder = certificateBuilder.build(contentSigner);

        //to certificate
        return (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(new ByteArrayInputStream(holder.getEncoded()));
    }

    /**
     * <p>解析ASN.1编码的X509证书数据</p>
     *
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
     * @param inputStream X509证书输入流, ASN.1编码, 非Base64编码
     */
    @SuppressWarnings("deprecation")
    public static X509CertificateStructure parseX509ToStructure(InputStream inputStream) throws IOException {
        try {
            ASN1InputStream asn1InputStream = new ASN1InputStream(inputStream);
            ASN1Sequence seq = (ASN1Sequence) asn1InputStream.readObject();
            return new X509CertificateStructure(seq);
        }finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Throwable ignore){
                }
            }
        }
    }

}
