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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

/**
 * <p>PKCS12密钥文件工具(p12/pfx)</p>
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
public class PKCS12KeyStoreUtils {

    private static final String ALGORITHM = "PKCS12";

    /**
     * 将证书和私钥保存到p12/pfx文件中
     *
     * <p>{@code
     *      //output user p12
     *      PKCS12KeyStoreUtils.storeCertificateAndKey(
     *          "user-cert.p12",
     *          "000000",
     *          "Thistle test user cert",
     *          userPrivateKey,
     *          userCertificate);
     *
     *      ////output root p12
     *      PKCS12KeyStoreUtils.storeCertificateAndKey(
     *          "root-cert.p12",
     *          "000000",
     *          "Thistle test ca cert",
     *          null,
     *          caCertificate);
     * }</p>
     *
     * @param keyStorePath keyStore的文件路径
     * @param keyStorePassword keyStore的密码
     * @param alias 证书和私钥的别名
     * @param privateKey 证书对应的私钥(如果为空, 则仅保存证书)
     * @param certificateChain 证书链, 通常传入一个证书即可, 用户证书/CA证书/根证书一般会分别导出独立的文件. 如果需要一次性导出整个
     *                         证书链到一个文件, 也可以传入多个证书, 顺序是个人证书->二级CA证书->根证书, {userCertificate, subCaCertificate, rootCertificate}
     */
    public static void storeCertificateAndKey(String keyStorePath, String keyStorePassword, String alias, PrivateKey privateKey, Certificate... certificateChain) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        File keyStoreFile = new File(keyStorePath);
        File dirFile = keyStoreFile.getParentFile();
        if (dirFile != null && !dirFile.exists()){
            if (!dirFile.mkdirs()){
                throw new IOException("Can not make directory for keyStore, path:" + dirFile.getAbsolutePath());
            }
        }
        storeCertificateAndKey(new FileOutputStream(keyStoreFile), keyStorePassword, alias, privateKey, certificateChain);
    }

    /**
     * 将证书和私钥保存到p12/pfx文件中
     *
     * <p>{@code
     *      //output user p12
     *      PKCS12KeyStoreUtils.storeCertificateAndKey(
     *          outputStream1,
     *          "000000",
     *          "Thistle test user cert",
     *          userPrivateKey,
     *          userCertificate);
     *
     *      ////output root p12
     *      PKCS12KeyStoreUtils.storeCertificateAndKey(
     *          outputStream2,
     *          "000000",
     *          "Thistle test ca cert",
     *          null,
     *          caCertificate);
     * }</p>
     *
     * @param keyStoreOutputStream keyStore的输出流
     * @param keyStorePassword keyStore的密码
     * @param alias 证书和私钥的别名
     * @param privateKey 证书对应的私钥(如果为空, 则仅保存证书)
     * @param certificateChain 证书链, 通常传入一个证书即可, 用户证书/CA证书/根证书一般会分别导出独立的文件. 如果需要一次性导出整个
     *                         证书链到一个文件, 也可以传入多个证书, 顺序是个人证书->二级CA证书->根证书, {userCertificate, subCaCertificate, rootCertificate}
     */
    public static void storeCertificateAndKey(OutputStream keyStoreOutputStream, String keyStorePassword, String alias, PrivateKey privateKey, Certificate... certificateChain) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        if (certificateChain == null || certificateChain.length <= 0){
            throw new IllegalAccessError("Null or empty certificateChain");
        }
        try {
            KeyStore keyStore = KeyStore.getInstance(ALGORITHM);
            keyStore.load(null, null);
            if (privateKey != null) {
                keyStore.setKeyEntry(alias, privateKey, keyStorePassword != null ? keyStorePassword.toCharArray() : null, certificateChain);
            } else {
                for (Certificate certificate : certificateChain){
                    keyStore.setCertificateEntry(alias, certificate);
                }
            }
            keyStore.store(keyStoreOutputStream, keyStorePassword != null ? keyStorePassword.toCharArray() : null);
        } finally {
            if (keyStoreOutputStream != null){
                try {
                    keyStoreOutputStream.close();
                } catch (Throwable ignore){
                }
            }
        }
    }

}
