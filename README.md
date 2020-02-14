# Thistle 20.0.1

* Comprehensive Java common library (Java7+)
* [Github Home](https://github.com/shepherdviolet/thistle)
* [Search in Maven Central](https://search.maven.org/search?q=g:com.github.shepherdviolet.thistle20)
* [PGP Key](http://pool.sks-keyservers.net/pks/lookup?op=vindex&fingerprint=on&search=0x90998B78AABD6E96)

# thistle-common

> Core module of thistle

* [Crypto | Common crypto utils](https://github.com/shepherdviolet/thistle/blob/master/docs/crypto/guide.md)
* [SimpleKeyValueEncoder | Simple Key-Value to String Encoder](https://github.com/shepherdviolet/thistle/blob/master/docs/kvencoder/guide.md)
* [Trace Utils | Help to trace transactions](https://github.com/shepherdviolet/thistle/blob/master/docs/trace/guide.md)
* [Various Utils | Various utils are here](https://github.com/shepherdviolet/thistle/tree/master/src/main/java/sviolet/thistle/util)

# thistle-crypto-plus

> The module has more crypto features (depends on bouncy-castle)

* [Crypto | Advanced crypto utils with bouncy-castle](https://github.com/shepherdviolet/thistle/blob/master/docs/crypto/guide.md)

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
