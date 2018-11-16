# 国际算法加解密/加解签手册

* 这些加密工具`线程安全`
* 国密算法请参考https://github.com/shepherdviolet/smcrypto
* `Maven/Gradle依赖配置`在本文最后

# 小知识

* 常见的加密算法分为`对称算法`和`非对称算法`, 对称算法通常用于加解密, 非对称算法通常用于签名验签

### 对称算法
* 约定对称算法时, 要约定`加密算法`/`密钥长度`/`填充算法`, 例如:AES, 128位密钥(16bytes), ECB/PKCS5Padding填充算法
* `填充算法`:数据加密时, 通常要求数据长度符合块(block)的倍数, 因此需要一种算法将数据填充到规定长度. 银联有自定义的填充方式, 另外还有全补0的填充方式, 这些需要自行实现.
* AES算法密钥长度通常为128位(16bytes)
* DES算法密钥长度通常为64位(8bytes)
* 3DES(DESede)算法秘钥长度通常为128位(16bytes)和192位(24bytes)
* 对称密钥通常转为Base64传输

### 非对称算法
* 约定非对称算法时, 要约定`签名算法(包含摘要方式)`/`密钥长度`, 例如:SHA256withRSA(RSA算法, SHA256摘要), 2048位密钥(注意RSA的密钥长度不等于密钥字符串的长度)
* RSA算法除了签名验签, 还能进行数据加解密, 但效率较低, 通常用于交换对称密钥(加密AES密钥等)
* RSA用到数据加解密时, 还需要约定`填充算法`, 例如:ECB/PKCS1Padding
* 国际非对称算法除了RSA, 还有ECC算法(椭圆曲率算法), JDK中实现了ECDSA签名验签, 未提供加解密
* ECDSA对比RSA, 拥有更短的密钥长度, 更快的签名速度, 和理论上更高的安全性
* RSA算法密钥长度通常为1024位和2048位, `私钥`自己保管, `公钥`提供给对方
* RSA签名方式有`MD5withRSA`/`SHA1withRSA`/`SHA256withRSA`, 建议使用SHA256withRSA

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
* 注意, CBC填充算法需使用encryptCBC/decryptCBC方法, 并送入iv量

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

<br>
<br>
<br>

# 依赖

* gradle

```gradle
//version替换为具体版本
dependencies {
    compile 'com.github.shepherdviolet:thistle-crypto:version'
}
```

* gradle(最少依赖)

```gradle
//version替换为具体版本
dependencies {
    compile ('com.github.shepherdviolet:thistle:version') {
        transitive = false
    }
    compile ('com.github.shepherdviolet:thistle-crypto:version') {
        transitive = false
    }
    compile 'org.bouncycastle:bcpkix-jdk15on:1.59'
}
```

* maven

```maven
    <!--version替换为具体版本-->
    <dependency>
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>thistle-crypto</artifactId>
        <version>version</version>
    </dependency>
```

* maven(最少依赖)

```maven
    <!--version替换为具体版本-->
    <dependency>
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>thistle</artifactId>
        <version>version</version>
        <exclusions>
             <exclusion>
                 <groupId>*</groupId>
                 <artifactId>*</artifactId>
             </exclusion>
        </exclusions>
    </dependency>
    <dependency>
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>thistle-crypto</artifactId>
        <version>version</version>
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
