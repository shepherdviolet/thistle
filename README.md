# Thistle 14.4

* Comprehensive Java common library (Java7+)
* https://github.com/shepherdviolet/thistle

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
    compile 'com.github.shepherdviolet:thistle-common:version'
    //thistle-crypto-plus
    compile 'com.github.shepherdviolet:thistle-crypto-plus:version'
}

```

```maven
    <!-- thistle -->
    <dependency>    
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>thistle-common</artifactId>
        <version>version</version> 
    </dependency>
    <!-- thistle-crypto-plus -->
    <dependency>    
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>thistle-crypto-plus</artifactId>
        <version>version</version> 
    </dependency>
```

* [Dependencies exclusion](https://github.com/shepherdviolet/thistle/blob/master/docs/dependencies-exclusion.md)
