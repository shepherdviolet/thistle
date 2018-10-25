# Thistle 11.2
* Comprehensive Java common library (Java7+)
* https://github.com/shepherdviolet/thistle

# Contents

* [ThistleSpi | Enhanced SPI (Service Provider Interfaces)](https://github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/guide.md)
* [SimpleKeyValueEncoder | Simple Key-Value to String Encoder](https://github.com/shepherdviolet/thistle/blob/master/docs/kvencoder/guide.md)
* [Crypto | Crypto utils guide](https://github.com/shepherdviolet/thistle/blob/master/docs/crypto/guide.md)
* [Utils | Other utils are here](https://github.com/shepherdviolet/thistle/tree/master/src/main/java/sviolet/thistle/util)

# Import dependencies from maven repository

```gradle

repositories {
	//Thistle in mavenCentral
    mavenCentral()
}
dependencies {
    compile 'com.github.shepherdviolet:thistle:version'
}

```

```maven
    <dependency>    
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>thistle</artifactId>
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
            exclude group:'com.google.code.gson'
            exclude group:'org.bouncycastle'
        }
    }
```

```maven
    <dependency>
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>thistle</artifactId>
        <version>version</version>
        <exclusions>
             <exclusion>
                 <groupId>com.google.code.gson</groupId>
                 <artifactId>*</artifactId>
             </exclusion>
             <exclusion>
                 <groupId>org.bouncycastle</groupId>
                 <artifactId>*</artifactId>
             </exclusion>
        </exclusions>
    </dependency>
```
