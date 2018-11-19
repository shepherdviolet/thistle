# How to exclude dependencies (optional)

```gradle
    repositories {
    	//Thistle in mavenCentral
        mavenCentral()
    }
    dependencies {
        compile ('com.github.shepherdviolet:thistle-common:version') {
        }
        compile ('com.github.shepherdviolet:thistle-crypto-plus:version') {
            exclude group:'org.bouncycastle'
        }
    }
```

```maven
    <dependency>
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>thistle-common</artifactId>
        <version>version</version>
    </dependency>
    <dependency>
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>thistle-crypto-plus</artifactId>
        <version>version</version>
        <exclusions>
             <exclusion>
                 <groupId>org.bouncycastle</groupId>
                 <artifactId>*</artifactId>
             </exclusion>
        </exclusions>
    </dependency>
```
