# thistle 10.0
* Java common library for private use
* https://github.com/shepherdviolet/thistle

# Import dependencies from maven repository

```gradle

repositories {
	//Thistle in mavenCentral
    mavenCentral()
}
dependencies {
    compile 'com.github.shepherdviolet:thistle:10.0'
}

```

```maven
    <dependency>    
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>thistle</artifactId>
        <version>10.0</version> 
    </dependency>
```

# How to exclude dependencies (optional)

```gradle
    repositories {
    	//Thistle in mavenCentral
        mavenCentral()
    }
    dependencies {
        compile ('com.github.shepherdviolet:thistle:10.0') {
            exclude group:'org.jetbrains.kotlin', module:'kotlin-stdlib-jre7'
            exclude group:'com.google.code.gson'
            exclude group:'org.bouncycastle'
        }
    }
```

```maven
    <dependency>
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>thistle</artifactId>
        <version>10.0</version>
        <exclusions>
             <exclusion>
                 <groupId>org.jetbrains.kotlin</groupId>
                 <artifactId>kotlin-stdlib-jre7</artifactId>
             </exclusion>
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

* [ThistleSpi](https://github.com/shepherdviolet/thistle/blob/master/docs/spi-manual.md)
* [Crypto](https://github.com/shepherdviolet/thistle/blob/master/docs/crypto-manual.md)
* [Utils](https://github.com/shepherdviolet/thistle/tree/master/src/main/java/sviolet/thistle/util)