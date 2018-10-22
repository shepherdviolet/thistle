# thistle 11.2
* Java common library (Java 7)
* https://github.com/shepherdviolet/thistle

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

# Contents

* [ThistleSpi | Services and plugins loader (Custom SPI)](https://github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/guide.md)
* [Crypto | Crypto utils guide](https://github.com/shepherdviolet/thistle/blob/master/docs/crypto/guide.md)
* [Utils | Other utils are here](https://github.com/shepherdviolet/thistle/tree/master/src/main/java/sviolet/thistle/util)