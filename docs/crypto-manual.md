# 加解密/加解签手册

* 包路径:sviolet.thistle.util.crypto

# 小知识

TODO......................

# 依赖

* gradle

```gradle

//依赖
dependencies {
    compile 'com.github.shepherdviolet:thistle:9.7'
}
```

* gradle(最少依赖)

```gradle
dependencies {
    compile ('com.github.shepherdviolet:thistle:9.7') {
        transitive = false
    }
    compile 'org.bouncycastle:bcpkix-jdk15on:1.59'
}
```

* maven

```maven
    <dependency>
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>thistle</artifactId>
        <version>9.7</version>
    </dependency>
```

* maven(最少依赖)

```maven
    <dependency>
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>thistle</artifactId>
        <version>9.7</version>
        <exclusions>
             <exclusion>
                 <groupId>*</groupId>
                 <artifactId>*</artifactId>
             </exclusion>
        </exclusions>
    </dependency>
    <dependency>
        <groupId>org.bouncycastle</groupId>
        <artifactId>bcpkix-jdk15on</artifactId>
        <version>1.59</version>
    </dependency>
```

# RSA 加解密/加解签

### RSAKeyGenerator

* RSA密钥生成
* PKCS8/X509格式密钥解析

* 解析密钥示例

```gradle
    RSAPublicKey publicKey = RSAKeyGenerator.generatePublicKeyByX509(publicKeyData);
    RSAPrivateKey privateKey = RSAKeyGenerator.generatePrivateKeyByPKCS8(privateKeyData);
```

### RSACipher

* RSA加解签
* RSA加解密

* 签名示例

```gradle
    //支持byte[]数据和File文件的签名
    byte[] sign = RSACipher.sign(data, privateKey, RSACipher.SIGN_ALGORITHM_RSA_SHA256);
```

* 验签示例

```gradle
    //支持byte[]数据和File文件的验签
    boolean valid = RSACipher.verify(data, sign, publicKey, RSACipher.SIGN_ALGORITHM_RSA_SHA256);
```

# AES 加解密

### AESKeyGenerator

* AES密钥生成

* 密钥生成示例

```gradle
    byte[] key = AESKeyGenerator.generateAes128();
```

### AESCipher

* AES加解密
* 注意, CBC填充算法需使用encryptCBC/decryptCBC, 并送入iv量

* 加解密示例

```gradle
    byte[] encrypted = AESKeyGenerator.encrypt(raw, key, AESKeyGenerator.CRYPTO_ALGORITHM_AES_ECB_PKCS5PADDING);
    byte[] decrypted = AESKeyGenerator.encrypt(encrypted, key, AESKeyGenerator.CRYPTO_ALGORITHM_AES_ECB_PKCS5PADDING);
```

# DES DESede(3DES) 加解密

### DESKeyGenerator

* DES密钥生成
* DESede(3DES)密钥生成

### DESCipher

* DES加解密

### DESEdeCipher

* DESede(3DES)加解密

# ECDSA 加解签

### ECDSAKeyGenerator

* ECDSA密钥生成
* PKCS8/X509格式密钥解析

### ECDSACipher

* ECDSA加解签

# 证书

### CertificateUtils

* X509格式证书解析
* 证书/根证书签发

### PKCS12KeyStoreUtils

* 从p12/pfx文件中读取证书和私钥
* 将证书和私钥写入到p12/pfx文件中

* 读取示例

```gradle
    PKCS12KeyStoreUtils.CertificateChainAndKey certificateChainAndKey = PKCS12KeyStoreUtils.loadCertificateAndKey(
        //文件输入流
        getClassLoader().getResourceAsStream("cert/cert.p12"),
        //文件密钥
        "000000",
        //证书别名
        "alias"
        );
```
