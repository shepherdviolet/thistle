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

import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509CertificateStructure;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.operator.OperatorCreationException;
import sviolet.thistle.util.conversion.Base64Utils;
import sviolet.thistle.util.crypto.base.BaseBCAsymKeyGenerator;
import sviolet.thistle.util.crypto.base.BaseBCCertificateUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

/**
 * <p>高级证书工具</p>
 *
 * <p>基本功能见thistle-common的{@link CertificateUtils}</p>
 *
 * @author S.Violet
 */
public class AdvancedCertificateUtils extends CertificateUtils {

    /***********************************************************************************************
     * RSA
     ***********************************************************************************************/

    /**
     * 密钥类型:RSA
     */
    public static final String KEK_ALGORITHM_RSA = "RSA";

    /**
     * 签名算法:MD5withRSA
     */
    public static final String SIGN_ALGORITHM_RSA_MD5 = "MD5withRSA";

    /**
     * 签名算法:SHA1withRSA
     */
    public static final String SIGN_ALGORITHM_RSA_SHA1 = "SHA1withRSA";

    /**
     * 签名算法:SHA256withRSA
     */
    public static final String SIGN_ALGORITHM_RSA_SHA256 = "SHA256withRSA";

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
     * @param signAlgorithm 签名算法, CertificateUtils.SIGN_ALGORITHM_RSA_SHA256
     *
     */
    public static X509Certificate generateRSAX509RootCertificate(String dn, RSAPublicKey publicKey, RSAPrivateKey privateKey, int validity, String signAlgorithm) throws IOException, CertificateException, OperatorCreationException {
        return BaseBCCertificateUtils.generateRSAX509RootCertificate(dn, publicKey, privateKey, validity, signAlgorithm);
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
     * @param signAlgorithm 签名算法, CertificateUtils.SIGN_ALGORITHM_RSA_SHA256
     * @param caCertificate CA的证书
     * @param caPrivateKey CA的RSA私钥
     */
    public static X509Certificate generateRSAX509Certificate(String subjectDn, RSAPublicKey subjectPublicKey, int subjectValidity, String signAlgorithm, X509Certificate caCertificate, RSAPrivateKey caPrivateKey) throws IOException, CertificateException, OperatorCreationException {
        return BaseBCCertificateUtils.generateRSAX509Certificate(subjectDn, subjectPublicKey, subjectValidity, signAlgorithm, caCertificate, caPrivateKey);
    }

    /***********************************************************************************************
     * SM2
     ***********************************************************************************************/

    /**
     * 密钥类型:SM2
     */
    public static final String KEK_ALGORITHM_SM2 = "SM2";
    public static final String EC_KEY_ALGORITHM = "EC";

    /**
     * 签名算法:SM3withSM2
     */
    public static final String SIGN_ALGORITHM_SM2_SM3 = "SM3withSM2";

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
        return BaseBCCertificateUtils.generateSm2Csr(subjectDn, publicKeyParams, privateKeyParams);
    }

    /**
     * 生成SM2根证书(顶级CA)
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
     * @param caDn CA的DN信息, 例:CN=Test CA, OU=IT Dept, O=My Company, L=Ningbo, ST=Zhejiang, C=CN
     * @param validity 申请证书的有效期(天), 例:3650
     * @param issuerPublicKeyParams 证书颁发者(CA)的公钥
     * @param issuerPrivateKeyParams 证书颁发者(CA)的私钥
     */
    public static X509Certificate generateSm2X509RootCertificate(String caDn,
                                                      int validity,
                                                      ECPublicKeyParameters issuerPublicKeyParams,
                                                      ECPrivateKeyParameters issuerPrivateKeyParams) throws IOException, OperatorCreationException, CertificateException, InvalidKeySpecException, NoSuchAlgorithmException {
        byte[] csr = BaseBCCertificateUtils.generateSm2Csr(caDn, issuerPublicKeyParams, issuerPrivateKeyParams);
        return BaseBCCertificateUtils.generateSm2X509Certificate(
                csr,
                validity,
                caDn,
                issuerPublicKeyParams,
                issuerPrivateKeyParams,
                true,
                new KeyUsage(KeyUsage.digitalSignature | KeyUsage.dataEncipherment | KeyUsage.keyCertSign | KeyUsage.cRLSign));
    }

    /**
     * 生成SM2二级CA证书
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
     * @param issuerCertificate 证书颁发者(CA)的证书
     * @param issuerPrivateKeyParams 证书颁发者(CA)的私钥
     */
    public static X509Certificate generateSm2X509CaCertificate(byte[] csr,
                                                             int validity,
                                                             X509Certificate issuerCertificate,
                                                             ECPrivateKeyParameters issuerPrivateKeyParams) throws InvalidKeySpecException, OperatorCreationException, CertificateException, NoSuchAlgorithmException, IOException {
        return BaseBCCertificateUtils.generateSm2X509Certificate(
                csr,
                validity,
                issuerCertificate.getSubjectX500Principal().toString(),
                //这里性能不是太好, 重新解析数据获取公钥, 要追求性能可以直接用CA的DN+公私钥生成
                BaseBCAsymKeyGenerator.ecPublicKeyToEcPublicKeyParams(
                        BaseBCAsymKeyGenerator.parseEcPublicKeyByX509(issuerCertificate.getPublicKey().getEncoded(), EC_KEY_ALGORITHM)),
                issuerPrivateKeyParams,
                true,
                new KeyUsage(KeyUsage.digitalSignature | KeyUsage.dataEncipherment | KeyUsage.keyCertSign | KeyUsage.cRLSign));
    }

    /**
     * 生成SM2用户证书
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
     * @param issuerCertificate 证书颁发者(CA)的证书
     * @param issuerPrivateKeyParams 证书颁发者(CA)的私钥
     */
    public static X509Certificate generateSm2X509Certificate(byte[] csr,
                                                               int validity,
                                                               X509Certificate issuerCertificate,
                                                               ECPrivateKeyParameters issuerPrivateKeyParams) throws InvalidKeySpecException, OperatorCreationException, CertificateException, NoSuchAlgorithmException, IOException {
        return BaseBCCertificateUtils.generateSm2X509Certificate(
                csr,
                validity,
                issuerCertificate.getSubjectX500Principal().toString(),
                //这里性能不是太好, 重新解析数据获取公钥, 要追求性能可以直接用CA的DN+公私钥生成
                BaseBCAsymKeyGenerator.ecPublicKeyToEcPublicKeyParams(
                        BaseBCAsymKeyGenerator.parseEcPublicKeyByX509(issuerCertificate.getPublicKey().getEncoded(), EC_KEY_ALGORITHM)),
                issuerPrivateKeyParams,
                false,
                new KeyUsage(KeyUsage.digitalSignature | KeyUsage.dataEncipherment));
    }

    /***********************************************************************************************
     * Others
     ***********************************************************************************************/

    /**
     * <p>解析ASN.1编码的X509证书数据</p>
     *
     * @param certBase64 X509证书数据, ASN.1编码, Base64编码
     */
    @SuppressWarnings("deprecation")
    public static X509CertificateStructure parseX509ToStructure(String certBase64) throws IOException {
        return parseX509ToStructure(Base64Utils.decode(certBase64));
    }

    /**
     * <p>解析ASN.1编码的X509证书数据</p>
     *
     * @param certData X509证书数据, ASN.1编码, 非Base64编码
     */
    @SuppressWarnings("deprecation")
    public static X509CertificateStructure parseX509ToStructure(byte[] certData) throws IOException {
        return parseX509ToStructure(new ByteArrayInputStream(certData));
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
        return BaseBCCertificateUtils.parseX509ToStructure(inputStream);
    }

}
