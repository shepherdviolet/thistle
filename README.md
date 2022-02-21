# Thistle 22.0.1

[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/shepherdviolet/thistle.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/shepherdviolet/thistle/context:java)

* Comprehensive Java common library (Java7+)
* [Github Home](https://github.com/shepherdviolet/thistle)
* [Search in Maven Central](https://search.maven.org/search?q=g:com.github.shepherdviolet)
* [PGP Key](http://pool.sks-keyservers.net/pks/lookup?op=vindex&fingerprint=on&search=0x90998B78AABD6E96)

<br>
<br>

## Module 'thistle-common'

[![Depends](https://img.shields.io/badge/Depends-glaciion--api-dc143c.svg?style=flat)](https://github.com/shepherdviolet/glaciion)

> Core module of thistle

### Data structure

* [Bitmap / Bloom filter](https://github.com/shepherdviolet/thistle/tree/master/thistle-common/src/main/java/sviolet/thistle/model/bitmap)
* [Sliding window](https://github.com/shepherdviolet/thistle/tree/master/thistle-common/src/main/java/sviolet/thistle/model/statistic)

### Crypto utils

* [Crypto utils : RSA ECDSA AES DES / SHA MD5 / PEM p12 ...](https://github.com/shepherdviolet/thistle/blob/master/docs/crypto/guide.md)

### Reflect utils

* [BeanInfoUtils : Get property information of Java Bean](https://github.com/shepherdviolet/thistle/tree/master/thistle-common/src/main/java/sviolet/thistle/util/reflect/BeanInfoUtils.java)
* [GenericClassUtils : Get actual types of generic class](https://github.com/shepherdviolet/thistle/tree/master/thistle-common/src/main/java/sviolet/thistle/util/reflect/GenericClassUtils.java)
* [MethodCaller : Get caller information of a method](https://github.com/shepherdviolet/thistle/tree/master/thistle-common/src/main/java/sviolet/thistle/util/reflect/MethodCaller.java)
* [ClassPrinter : Print all information for a class / object](https://github.com/shepherdviolet/thistle/tree/master/thistle-common/src/main/java/sviolet/thistle/util/reflect/ClassPrinter.java)

### Misc utils

* [SimpleKeyValueEncoder : Convert between simple Key-Value and String](https://github.com/shepherdviolet/thistle/blob/master/docs/kvencoder/guide.md)
* [ThreadPoolExecutorUtils : Create thread pool](https://github.com/shepherdviolet/thistle/tree/master/thistle-common/src/main/java/sviolet/thistle/util/concurrent/ThreadPoolExecutorUtils.java)
* [...](https://github.com/shepherdviolet/thistle/tree/master/thistle-common/src/main/java/sviolet/thistle/util)

<br>

## Module 'thistle-crypto-plus'

[![Depends](https://img.shields.io/badge/Depends-thistle--common-6a5acd.svg?style=flat)](https://github.com/shepherdviolet/thistle)
[![Depends](https://img.shields.io/badge/Depends-bcpkix--jdk15on-dc143c.svg?style=flat)](https://search.maven.org/search?q=g:org.bouncycastle%20a:bcpkix-jdk15on)

> The module has more crypto features (depends on bouncy-castle)

### Crypto utils

* [Advanced crypto utils : SM2 SM4 / SM3 ...](https://github.com/shepherdviolet/thistle/blob/master/docs/crypto/guide.md)

<br>

## Module 'thistle-trace'

[![Depends](https://img.shields.io/badge/Depends-thistle--common-6a5acd.svg?style=flat)](https://github.com/shepherdviolet/thistle)
[![Depends](https://img.shields.io/badge/Depends-glaciion--core-dc143c.svg?style=flat)](https://github.com/shepherdviolet/glaciion)

> The module for tracing

### Tracing utils

* [Trace : Help to trace invocation across thread or process](https://github.com/shepherdviolet/thistle/blob/master/docs/trace/guide.md)

<br>
<br>

# Import dependencies from maven repository

* [Search `thistle-common` in Maven Central](https://search.maven.org/search?q=g:com.github.shepherdviolet%20a:thistle-common)
* [Search `thistle-crypto-plus` in Maven Central](https://search.maven.org/search?q=g:com.github.shepherdviolet%20a:thistle-crypto-plus)
* [Search `thistle-trace` in Maven Central](https://search.maven.org/search?q=g:com.github.shepherdviolet%20a:thistle-trace)

```gradle

repositories {
    //Thistle in mavenCentral
    mavenCentral()
}
dependencies {
    compile 'com.github.shepherdviolet:thistle-common:?'
    compile 'com.github.shepherdviolet:thistle-crypto-plus:?'
    compile 'com.github.shepherdviolet:thistle-trace:?'
}

```

```maven
    <dependency>    
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>thistle-common</artifactId>
        <version>?</version> 
    </dependency>
    <dependency>    
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>thistle-crypto-plus</artifactId>
        <version>?</version> 
    </dependency>
    <dependency>    
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>thistle-trace</artifactId>
        <version>?</version> 
    </dependency>
```
