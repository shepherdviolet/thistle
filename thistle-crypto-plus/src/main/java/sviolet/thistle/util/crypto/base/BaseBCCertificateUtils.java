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

import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.asn1.x9.X9ECPoint;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.util.KeyUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.*;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import sviolet.thistle.util.common.CloseableUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;

/**
 * [Bouncy castle]证书处理基本逻辑<p>
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

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /***********************************************************************************************
     * Common
     ***********************************************************************************************/

    /**
     * 使用BouncyCastle从输入流中解析证书, 适用于SM2等更多算法的证书
     *
     * @param data 证书数据(X509格式)
     * @param type 证书数据格式, 例如X.509
     * @return 如果type是X.509, 可以强制类型转换为X509Certificate
     */
    public static Certificate parseCertificateByBouncyCastle(byte[] data, String type) throws CertificateException, NoSuchProviderException {
        return parseCertificateByBouncyCastle(new ByteArrayInputStream(data), type);
    }

    /**
     * 使用BouncyCastle从输入流中解析证书, 适用于SM2等更多算法的证书
     *
     * @param inputStream 证书数据流(X509格式), 会被close掉
     * @param type 证书数据格式, 例如X.509
     * @return 如果type是X.509, 可以强制类型转换为X509Certificate
     */
    public static Certificate parseCertificateByBouncyCastle(InputStream inputStream, String type) throws CertificateException, NoSuchProviderException {
        try {
            return CertificateFactory.getInstance(type, BouncyCastleProvider.PROVIDER_NAME).generateCertificate(inputStream);
        } finally {
            CloseableUtils.closeQuiet(inputStream);
        }
    }

    /**
     * 使用颁发者公钥验证证书有效性
     * @param certificate 证书
     * @param issuerPublicKey 颁发者公钥
     * @return true:有效
     */
    public static boolean verifyCertificate(X509Certificate certificate, BCECPublicKey issuerPublicKey) {
        try {
            certificate.verify(issuerPublicKey, BouncyCastleProvider.PROVIDER_NAME);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    /**
     * 使用颁发者公钥验证证书有效性
     * @param certificate 证书
     * @param issuerPublicKeyParams 颁发者公钥
     * @return true:有效
     */
    public static boolean verifyCertificate(X509Certificate certificate, ECPublicKeyParameters issuerPublicKeyParams) {
        try {
            certificate.verify(BaseBCAsymKeyGenerator.ecPublicKeyParamsToEcPublicKey(issuerPublicKeyParams, "EC"),
                    BouncyCastleProvider.PROVIDER_NAME);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    /***********************************************************************************************
     * RSA
     ***********************************************************************************************/

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
     * @param issuerCertificate 证书颁发者(CA)的证书
     * @param issuerPrivateKey 证书颁发者(CA)的RSA私钥
     */
    public static X509Certificate generateRSAX509Certificate(String subjectDn, RSAPublicKey subjectPublicKey, int subjectValidity, String signAlgorithm, X509Certificate issuerCertificate, RSAPrivateKey issuerPrivateKey) throws IOException, CertificateException, OperatorCreationException {
        //certificate builder
        X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(
                //issuer dn
                new X500Name(REVERSED_BC_STYLE, issuerCertificate != null ? issuerCertificate.getSubjectX500Principal().toString() : subjectDn),
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
        AsymmetricKeyParameter asymmetricKeyParameter = PrivateKeyFactory.createKey(issuerPrivateKey.getEncoded());
        ContentSigner contentSigner = new BcRSAContentSignerBuilder(signAlgorithmId, digestAlgorithmId).build(asymmetricKeyParameter);
        X509CertificateHolder holder = certificateBuilder.build(contentSigner);

        //to certificate
        return (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(new ByteArrayInputStream(holder.getEncoded()));
    }

    /***********************************************************************************************
     * SM2
     ***********************************************************************************************/

    /**
     * 生成SM2证书的申请数据CSR (CSR包含申请者信息, CA收到CSR后签名并返回证书)
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
     * @param subjectDn 申请人DN信息, 例:CN=Test CA, OU=IT Dept, O=My Company, L=Ningbo, ST=Zhejiang, C=CN
     * @param publicKeyParams 申请人公钥
     * @param privateKeyParams 申请人私钥
     * @return 证书申请数据, CSR
     */
    public static byte[] generateSm2Csr(String subjectDn,
                                        ECPublicKeyParameters publicKeyParams,
                                        ECPrivateKeyParameters privateKeyParams) throws OperatorCreationException, IOException {
        //转换密钥类型
        BCECPublicKey publicKey = BaseBCAsymKeyGenerator.ecPublicKeyParamsToEcPublicKey(publicKeyParams, "EC");
        BCECPrivateKey privateKey = BaseBCAsymKeyGenerator.ecPrivateKeyParamsToEcPrivateKey(privateKeyParams, publicKeyParams, "EC");
        //公钥需要特殊处理, 指定算法/协议/公钥值
        SubjectPublicKeyInfo subjectPublicKeyInfo = new SubjectPublicKeyInfo(
                new AlgorithmIdentifier(X9ObjectIdentifiers.id_ecPublicKey, new ASN1ObjectIdentifier("1.2.156.10197.1.301")),
                ASN1OctetString.getInstance(new X9ECPoint(publicKey.getQ(), false).toASN1Primitive()).getOctets());
        byte[] publicKeyEncoded = KeyUtil.getEncodedSubjectPublicKeyInfo(subjectPublicKeyInfo);
        //产生CSR
        X500Name dn = new X500Name(REVERSED_BC_STYLE, subjectDn);
        PKCS10CertificationRequestBuilder certReqBuilder = new PKCS10CertificationRequestBuilder(
                dn,
                SubjectPublicKeyInfo.getInstance(publicKeyEncoded));
        ContentSigner signer = new JcaContentSignerBuilder("SM3withSM2")
                .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .build(privateKey);
        return certReqBuilder
                .build(signer)
                .getEncoded();
    }

    /**
     * 生成SM2证书
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
     * @param csr 证书申请数据, BaseBCCertificateUtils.generateSm2Csr(...)
     * @param validity 申请证书的有效期(天), 例:3650
     * @param issuerDn 证书颁发者(CA)的DN信息, 例:CN=Test CA, OU=IT Dept, O=My Company, L=Ningbo, ST=Zhejiang, C=CN
     * @param issuerPublicKeyParams 证书颁发者(CA)的公钥
     * @param issuerPrivateKeyParams 证书颁发者(CA)的私钥
     * @param generateCaCert true:生成CA证书 false:生成用户证书
     * @param usage 证书用途, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.dataEncipherment | KeyUsage.keyCertSign | KeyUsage.cRLSign)
     */
    public static X509Certificate generateSm2X509Certificate(byte[] csr,
                                                      int validity,
                                                      String issuerDn,
                                                      ECPublicKeyParameters issuerPublicKeyParams,
                                                      ECPrivateKeyParameters issuerPrivateKeyParams,
                                                      boolean generateCaCert,
                                                      KeyUsage usage) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, OperatorCreationException, CertificateException {
        //解析CSR
        PKCS10CertificationRequest request = new PKCS10CertificationRequest(csr);
        byte[] publicKeyEncoded = request.getSubjectPublicKeyInfo().toASN1Primitive().getEncoded(ASN1Encoding.DER);
        //公钥
        BCECPublicKey publicKey = BaseBCAsymKeyGenerator.parseEcPublicKeyByX509(publicKeyEncoded, "EC");
        //CA公钥
        BCECPublicKey issuerPublicKey = BaseBCAsymKeyGenerator.ecPublicKeyParamsToEcPublicKey(issuerPublicKeyParams, "EC");
        //CA私钥
        BCECPrivateKey issuerPrivateKey = BaseBCAsymKeyGenerator.ecPrivateKeyParamsToEcPrivateKey(issuerPrivateKeyParams, issuerPublicKeyParams, "EC");
        //证书生成器
        X509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(
                //issuer dn
                new X500Name(REVERSED_BC_STYLE, issuerDn),
                //serial
                BigInteger.probablePrime(32, BaseKeyGenerator.getSystemSecureRandom()),
                //start date
                new Date(),
                //expire date
                new Date(System.currentTimeMillis() + validity * 24L * 60L * 60L * 1000L),
                //subject dn
                request.getSubject(),
                //public key
                publicKey);
        //扩展工具
        JcaX509ExtensionUtils extensionUtils = new JcaX509ExtensionUtils();
        //扩展参数
        certificateBuilder.addExtension(
                Extension.subjectKeyIdentifier,
                false,
                extensionUtils.createSubjectKeyIdentifier(SubjectPublicKeyInfo.getInstance(publicKey.getEncoded())));
        certificateBuilder.addExtension(
                Extension.authorityKeyIdentifier,
                false,
                extensionUtils.createAuthorityKeyIdentifier(SubjectPublicKeyInfo.getInstance(issuerPublicKey.getEncoded())));
        certificateBuilder.addExtension(
                Extension.basicConstraints,
                false,
                new BasicConstraints(generateCaCert));
        certificateBuilder.addExtension(
                Extension.keyUsage,
                false,
                usage);
        //签名器
        JcaContentSignerBuilder contentSignerBuilder = new JcaContentSignerBuilder("SM3withSM2");
        contentSignerBuilder.setProvider(BouncyCastleProvider.PROVIDER_NAME);
        //生成证书
        return new JcaX509CertificateConverter()
                .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .getCertificate(certificateBuilder.build(contentSignerBuilder.build(issuerPrivateKey)));
    }

    /***********************************************************************************************
     * Others
     ***********************************************************************************************/

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

    /***********************************************************************************************
     * Private
     ***********************************************************************************************/

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

}
