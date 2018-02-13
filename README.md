# thistle 9.1
* Java common library for private use
* https://github.com/shepherdviolet/thistle

### Import dependencies from maven repository

```gradle

repositories {
    //local repository
    //maven { url 'file:C:/m2repository/repository' }
    //maven central or jitpack.io
    mavenCentral()
    maven { url 'https://jitpack.io' }
}
dependencies {
    compile 'com.github.shepherdviolet:thistle:9.1'
}

```

```maven
    <dependency>    
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>thistle</artifactId>
        <version>9.1</version> 
    </dependency>
```

# How to exclude dependencies (optional)

```gradle
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
    dependencies {
        compile ('com.github.shepherdviolet:thistle:9.1') {
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
        <version>9.1</version>
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