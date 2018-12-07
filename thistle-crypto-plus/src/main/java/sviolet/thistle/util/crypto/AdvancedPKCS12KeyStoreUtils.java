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

import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.DERBMPString;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.crypto.engines.DESedeEngine;
import org.bouncycastle.crypto.engines.RC2Engine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.pkcs.*;
import org.bouncycastle.pkcs.bc.BcPKCS12MacCalculatorBuilder;
import org.bouncycastle.pkcs.bc.BcPKCS12PBEOutputEncryptorBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS12SafeBagBuilder;
import sviolet.thistle.util.common.CloseableUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

/**
 * <p>PKCS12密钥文件工具(p12/pfx), BouncyCastle版本</p>
 *
 * <p>
 *     OPENSSL查看p12文件信息:
 *     openssl pkcs12 -in pkcs12-test.p12 -info -noout
 * </p>
 *
 * <p>
 *     Windows查看证书信息:
 *     certmgr.msc
 * </p>
 *
 * @author S.Violet
 */
public class AdvancedPKCS12KeyStoreUtils extends PKCS12KeyStoreUtils {

    /**
     * 将证书和私钥保存到p12/pfx文件中, 支持国密
     *
     * @param keyStorePath keyStore的文件路径
     * @param keystorePassword keyStore的密码
     * @param alias 证书和私钥的别名
     * @param privateKey 证书对应的私钥(如果为空, 则仅保存证书)
     * @param certificateChain 证书链, 通常传入一个证书即可, 用户证书/CA证书/根证书一般会分别导出独立的文件. 如果需要一次性导出整个
     *                         证书链到一个文件, 也可以传入多个证书, 顺序是个人证书->二级CA证书->根证书, {userCertificate, subCaCertificate, rootCertificate}
     */
    public static void storeCertificateAndKeyAdvanced(String keyStorePath, String keystorePassword, String alias, PrivateKey privateKey, X509Certificate... certificateChain) throws NoSuchAlgorithmException, IOException, PKCSException {
        File keyStoreFile = new File(keyStorePath);
        File dirFile = keyStoreFile.getParentFile();
        if (dirFile != null && !dirFile.exists()){
            if (!dirFile.mkdirs()){
                throw new IOException("Can not make directory for keyStore, path:" + dirFile.getAbsolutePath());
            }
        }
        storeCertificateAndKeyAdvanced(new FileOutputStream(keyStoreFile), keystorePassword, alias, privateKey, certificateChain);
    }

    /**
     * 将证书和私钥保存到p12/pfx文件中, 支持国密
     *
     * @param outputStream keyStore的输出流, 完成后会关闭
     * @param keystorePassword keyStore的密码
     * @param alias 证书和私钥的别名
     * @param privateKey 证书对应的私钥(如果为空, 则仅保存证书)
     * @param certificateChain 证书链, 通常传入一个证书即可, 用户证书/CA证书/根证书一般会分别导出独立的文件. 如果需要一次性导出整个
     *                         证书链到一个文件, 也可以传入多个证书, 顺序是个人证书->二级CA证书->根证书, {userCertificate, subCaCertificate, rootCertificate}
     */
    public static void storeCertificateAndKeyAdvanced(OutputStream outputStream, String keystorePassword, String alias, PrivateKey privateKey, X509Certificate... certificateChain) throws NoSuchAlgorithmException, IOException, PKCSException {
        try {
            byte[] pfxData = parseCertificateAndKeyToPkcs12Advanced(keystorePassword, alias, privateKey, certificateChain);
            outputStream.write(pfxData);
        } finally {
            CloseableUtils.closeQuiet(outputStream);
        }
    }

    /**
     * 将证书和私钥转换为PKCS12格式的数据(p12/pfx的数据), 支持国密
     *
     * @param keystorePassword keyStore的密码
     * @param alias 证书和私钥的别名
     * @param privateKey 证书对应的私钥(如果为空, 则仅保存证书)
     * @param certificateChain 证书链, 通常传入一个证书即可, 用户证书/CA证书/根证书一般会分别导出独立的文件. 如果需要一次性导出整个
     *                         证书链到一个文件, 也可以传入多个证书, 顺序是个人证书->二级CA证书->根证书, {userCertificate, subCaCertificate, rootCertificate}
     */
    public static byte[] parseCertificateAndKeyToPkcs12Advanced(String keystorePassword, String alias, PrivateKey privateKey, X509Certificate... certificateChain) throws NoSuchAlgorithmException, IOException, PKCSException {
        if (certificateChain == null || certificateChain.length == 0) {
            throw new NullPointerException("certificateChain is null or empty");
        }
        PublicKey publicKey = certificateChain[0].getPublicKey();
        char[] passwordChars = keystorePassword.toCharArray();
        JcaX509ExtensionUtils extensionUtils = new JcaX509ExtensionUtils();

        //certificate chain
        PKCS12SafeBag[] bags = new PKCS12SafeBag[certificateChain.length];
        for (int i = 0; i < certificateChain.length; i++) {
            PKCS12SafeBagBuilder bagBuilder = new JcaPKCS12SafeBagBuilder(certificateChain[i]);
            bagBuilder.addBagAttribute(
                    PKCSObjectIdentifiers.pkcs_9_at_friendlyName,
                    new DERBMPString(alias));
            if (i == 0) {
                bagBuilder.addBagAttribute(
                        PKCSObjectIdentifiers.pkcs_9_at_localKeyId,
                        extensionUtils.createSubjectKeyIdentifier(publicKey));
            }
            bags[i] = bagBuilder.build();
        }
        //key
        PKCS12SafeBagBuilder keyBagBuilder = new JcaPKCS12SafeBagBuilder(
                privateKey,
                new BcPKCS12PBEOutputEncryptorBuilder(
                        PKCSObjectIdentifiers.pbeWithSHAAnd3_KeyTripleDES_CBC,
                        new CBCBlockCipher(new DESedeEngine())).build(passwordChars));
        keyBagBuilder.addBagAttribute(
                PKCSObjectIdentifiers.pkcs_9_at_friendlyName,
                new DERBMPString(alias));
        keyBagBuilder.addBagAttribute(
                PKCSObjectIdentifiers.pkcs_9_at_localKeyId,
                extensionUtils.createSubjectKeyIdentifier(publicKey));
        //pfx builder
        PKCS12PfxPduBuilder pfxPduBuilder = new PKCS12PfxPduBuilder();
        //add certificates
        pfxPduBuilder.addEncryptedData(
                new BcPKCS12PBEOutputEncryptorBuilder(
                        PKCSObjectIdentifiers.pbeWithSHAAnd40BitRC2_CBC,
                        new CBCBlockCipher(new RC2Engine())).build(passwordChars),
                bags);
        //add key
        pfxPduBuilder.addData(keyBagBuilder.build());
        //build pfx
        return pfxPduBuilder.build(new BcPKCS12MacCalculatorBuilder(), passwordChars)
                .getEncoded(ASN1Encoding.DER);
    }

}
