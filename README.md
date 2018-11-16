# Thistle 11.4
* Comprehensive Java common library (Java7+)
* https://github.com/shepherdviolet/thistle

## thistle

* [ThistleSpi | Enhanced SPI (Service Provider Interfaces)](https://github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/guide.md)
* [SimpleKeyValueEncoder | Simple Key-Value to String Encoder](https://github.com/shepherdviolet/thistle/blob/master/docs/kvencoder/guide.md)
* [Utils | Other utils are here](https://github.com/shepherdviolet/thistle/tree/master/src/main/java/sviolet/thistle/util)

## thistle-crypto

* [Crypto | Crypto utils guide](https://github.com/shepherdviolet/thistle/blob/master/docs/crypto/guide.md)

# Import dependencies from maven repository

```gradle

repositories {
	//Thistle in mavenCentral
    mavenCentral()
}
dependencies {
    //thistle
    compile 'com.github.shepherdviolet:thistle:version'
    //thistle crypto
    compile 'com.github.shepherdviolet:thistle-crypto:version'
}

```

```maven
    <!-- thistle -->
    <dependency>    
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>thistle</artifactId>
        <version>version</version> 
    </dependency>
    <!-- thistle-crypto -->
    <dependency>    
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>thistle-crypto</artifactId>
        <version>version</version> 
    </dependency>
```

# How to exclude dependencies (optional)

```gradle
    repositories {
    	//Thistle in mavenCentral
        mavenCentral()
    }
    dependencies {
        compile ('com.github.shepherdviolet:thistle:version') {
        }
        compile ('com.github.shepherdviolet:thistle-crypto:version') {
            exclude group:'org.bouncycastle'
        }
    }
```

```maven
    <dependency>
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>thistle</artifactId>
        <version>version</version>
    </dependency>
    <dependency>
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>thistle-crypto</artifactId>
        <version>version</version>
        <exclusions>
             <exclusion>
                 <groupId>org.bouncycastle</groupId>
                 <artifactId>*</artifactId>
             </exclusion>
        </exclusions>
    </dependency>
```
