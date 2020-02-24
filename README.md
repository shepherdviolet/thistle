# Thistle 20.0.1

* Comprehensive Java common library (Java7+)
* [Github Home](https://github.com/shepherdviolet/thistle)
* [Search in Maven Central](https://search.maven.org/search?q=g:com.github.shepherdviolet.thistle20)
* [PGP Key](http://pool.sks-keyservers.net/pks/lookup?op=vindex&fingerprint=on&search=0x90998B78AABD6E96)

## Module 'thistle-common'

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

* [Trace : Help to trace invocation across thread or process](https://github.com/shepherdviolet/thistle/blob/master/docs/trace/guide.md)
* [SimpleKeyValueEncoder : Convert between simple Key-Value and String](https://github.com/shepherdviolet/thistle/blob/master/docs/kvencoder/guide.md)
* [ThreadPoolExecutorUtils : Create thread pool](https://github.com/shepherdviolet/thistle/tree/master/thistle-common/src/main/java/sviolet/thistle/util/concurrent/ThreadPoolExecutorUtils.java)
* [...](https://github.com/shepherdviolet/thistle/tree/master/thistle-common/src/main/java/sviolet/thistle/util)

## Module 'thistle-crypto-plus'

> The module has more crypto features (depends on bouncy-castle)

### Crypto utils

* [Advanced crypto utils : SM2 SM4 / SM3 ...](https://github.com/shepherdviolet/thistle/blob/master/docs/crypto/guide.md)

# Import dependencies from maven repository

```gradle

repositories {
    //Thistle in mavenCentral
    mavenCentral()
}
dependencies {
    //thistle
    compile 'com.github.shepherdviolet.thistle20:thistle-common:version'
    //thistle-crypto-plus
    compile 'com.github.shepherdviolet.thistle20:thistle-crypto-plus:version'
}

```

```maven
    <!-- thistle -->
    <dependency>    
        <groupId>com.github.shepherdviolet.thistle20</groupId>
        <artifactId>thistle-common</artifactId>
        <version>?</version> 
    </dependency>
    <!-- thistle-crypto-plus -->
    <dependency>    
        <groupId>com.github.shepherdviolet.thistle20</groupId>
        <artifactId>thistle-crypto-plus</artifactId>
        <version>?</version> 
    </dependency>
```
