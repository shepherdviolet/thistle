# thistle 7.1
* Java common library for private use
* https://github.com/shepherdviolet/thistle

### Import dependencies from maven repository

```gradle

repositories {
    // maven central or jitpack.io
    mavenCentral()
    maven { url 'https://jitpack.io' }
}
dependencies {
    compile 'com.github.shepherdviolet:thistle:7.1'
}

```

### Import dependencies from local repository

```gradle

repositories {
    //replace by your path
    maven { url 'file:C:/m2repository/repository' }
}

```

# How to exclude dependencies (optional)

```gradle
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
    dependencies {
        compile ('com.github.shepherdviolet:thistle:7.1') {
            exclude group:'org.jetbrains.kotlin', module:'kotlin-stdlib-jre7'
            exclude group:'com.google.code.gson'
        }
    }
```