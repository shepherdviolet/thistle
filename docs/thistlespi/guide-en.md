# ThistleSpi User Guide

* [中文指南](https://github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/guide.md)
* ThistleSpi is a variant of SPI (Service Provider Interfaces) implementation
* `Maven/Gradle Dependencies` at the end of this article

```text
In open source libraries or framework layers, we usually want our programs to be able to: 1.Support for custom logic in user 
application (Replace the default implementation). 2.Support for extensions from third-party plugins. 
The open source or framework layer provides interfaces, user application or plug-in libraries to implement interfaces. 
By configuration, the program finally loads custom logic or plug-ins. That is SPI. 
JDK provides an SPI specification, but it does not have a filtering mechanism. When multiple implementations coexist in 
the classpath, there is no service selection/plug-in exclusion function, which is inconvenient to use.
ThistleSpi adds a filtering mechanism that supports construction parameters and is more convenient to use.
```

<br>
<br>
<br>

# Service loading mode

```text
When the open source library or framework layer (hereinafter referred to as the underlying) program only needs a unique 
implementation, you can use the service loading mode. For example: the underlying provides the interface and the default 
implementation,the user application or plugin library (hereafter referred to as the upper layer) implements custom logic, 
add a service definition file, declare the 'interface-class/ID/priority/implementation-class' info. After that, when 
the underlying layer uses the ThistleSpi to load the service, it will scan the classpath, and apply the highest priority 
one by default. When there are multiple implementations with the highest priority, we can specify which implementation 
to use with the configuration file or startup parameters.
```

* [Service loading guide](https://github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/service-loading-en.md)

<br>
<br>
<br>

# Plugin loading mode

```text
When the open source library or framework layer (hereinafter referred to as the underlying) program requires an ordered 
series of implementations, you can use plug-in loading mode, such as: filter / interceptor / processor ...
The underlying provides the interface, and the user application or plugin library (hereinafter referred to as the upper 
layer) implements the interface, add a plugin definition file, declare the `interface/priority/implementation` info. 
After that, when the underlying layer uses the ThistleSpi to load plugins, it will scan the classpath, and return a plugin 
list according to the priority. We can use the configuration file or startup parameters to exclude the specified plugin.
In the processor scenario, a method will be defined in the interface that returns the type which the processor accepts. 
```

* [Plugin loading guide](https://github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/plugin-loading-en.md)

<br>
<br>
<br>

# About log

* Keyword: `ThistleSpi`
* By Default the `System.out` prints the log
* The library `thistle-spi-logger` provides an implementation for printing logs by SLF4J. Just add dependencies:

```text
dependencies {
    compile 'com.github.shepherdviolet:thistle-spi-logger:version'
}
```

```text
    <dependency>
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>thistle-spi-logger</artifactId>
        <version>version</version>
    </dependency>
```

* When printing log by `thistle-spi-logger`, the SLF4J log level will be `INFO` and `ERROR`, package `sviolet.thistle.x.common.thistlespi`
* ThistleSpi supports customizing its own log printer
* [Log sample](https://github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/log-sample-en.md)

<br>

## Customize log level

* ThistleSpi has it's own log level concept, here's how to adjust it's own log level, not SLF4J level
* Add a startup parameter (three choices one), debug output the most logs, error only outputs the error log, info is the default log level

> `-Dthistle.spi.loglv=error` <br>
> `-Dthistle.spi.loglv=info` <br>
> `-Dthistle.spi.loglv=debug` <br>

<br>

## Customize logger implementation

* Read [Service loading guide](https://github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/service-loading-en.md) first
* The following examples are for reference only

### Implement the logger

```text
package sample.spi.logger;

public class CustomSpiLogger implements SpiLogger {

    @Override
    public void print(String msg) {
        // print message
    }

    @Override
    public void print(String msg, Throwable throwable) {
        // print message and throwable
    }

}
```

### Declare in definition file

* Create a file `META-INF/thistle-spi-logger/service.properties`
* Edit:

```text
sviolet.thistle.x.common.thistlespi.SpiLogger>sample-app>application=sample.spi.logger.CustomSpiLogger
```

* Value of ID (`sample-app`) and priority (`application`) refer to document [Service loading guide](https://github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/service-loading-en.md)

<br>

## Specify the implementation of logger (Optional)

* If the loaded logger is not what you want, or if the same priority logger has a conflict, we can use the following method to specify which one to use.
* Read [Service loading guide](https://github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/service-loading-en.md) first

### By definition file

* Create a file `META-INF/thistle-spi-logger/service-apply.properties`
* Edit:

```text
sviolet.thistle.x.common.thistlespi.SpiLogger=sample-app
```

* The ID (`sample-app`) is the logger's ID you want to apply, You can adjust log-level to debug and observe the log to get the ID.

### By startup parameter

* Add a startup parameter

```text
-Dthistle.spi.apply.sviolet.thistle.x.common.thistlespi.SpiLogger=sample-app
```

* The ID (`sample-app`) is the logger's ID you want to apply, You can adjust log-level to debug and observe the log to get the ID.

<br>
<br>
<br>

# Exclude definition file (Temporarily)

```text
When you find that there is a problem with the definition file in a dependency package (jar), you cannot change it, you 
must rely on this package, and you can't solve the problem by using the apply/ignore way. We provide a temporary workaround 
that can specify the hash value of the definition file in the startup parameter to exclude it (the runtime will not load 
any definitions of the file). But this way also has disadvantages, because it is excluded according to the hash value 
of the definition file. If the dependency package is upgraded (modified), the hash value may change, resulting in the 
exclusion of invalidation. In the end, you still have to try to contact the dependency package provider to resolve it.
```

* Adjust log-level to debug: Add a startup parameter `-Dthistle.spi.loglv=debug`
* When printing log by `thistle-spi-logger`, you should adjust SLF4J log-level `sviolet.thistle.x.common.thistlespi` to `INFO`+
* Run the program, observe the log

```text
...SlfSpiLogger : 0 ThistleSpi | -------------------------------------------------------------
...SlfSpiLogger : 0 ThistleSpi | Loading services from META-INF/thistle-spi/, DOC: github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/guide.md
...SlfSpiLogger : 0 ThistleSpi | Loading config file:/E:/C_Workspace/j2ee-3-test/beet-scrunchy-incubator/beet-scrunchy/out/production/resources/META-INF/thistle-spi/service.properties <hash> 3cf41de0f0d4bdc24970c7b834e2b5c7
...SlfSpiLogger : 0 ThistleSpi | Loading config file:/E:/C_Workspace/j2ee-3-test/beet-scrunchy-incubator/beet-common/out/production/resources/META-INF/thistle-spi/service.properties <hash> 8e3d1cbd3f828ba2a7822581416e97e0
...SlfSpiLogger : 0 ThistleSpi | Loading config jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.1-SNAPSHOT/slate-common-11.1-20181012.155613-8.jar!/META-INF/thistle-spi/service.properties <hash> ec8a407a18cb9646b2330dc66bc294b5
```

* Pay attention to `Loading config ... <hash> ... `
* Find the definition file you need to exclude and record its hash value
* Add a startup parameter, format: -Dthistle.spi.file.exclusion=`hash1`,`hash2`....

```text
-Dthistle.spi.file.exclusion=3cf41de0f0d4bdc24970c7b834e2b5c7,8e3d1cbd3f828ba2a7822581416e97e0
```

* Restart the program, observe the log

```text
...SlfSpiLogger : 0 ThistleSpi | -------------------------------------------------------------
...SlfSpiLogger : 0 ThistleSpi | Loading services from META-INF/thistle-spi/, DOC: github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/guide.md
...SlfSpiLogger : 0 ThistleSpi | !!! Exclude config file:/E:/C_Workspace/j2ee-3-test/beet-scrunchy-incubator/beet-scrunchy/out/production/resources/META-INF/thistle-spi/service.properties by -Dthistle.spi.file.exclusion
...SlfSpiLogger : 0 ThistleSpi | !!! Exclude config file:/E:/C_Workspace/j2ee-3-test/beet-scrunchy-incubator/beet-common/out/production/resources/META-INF/thistle-spi/service.properties by -Dthistle.spi.file.exclusion
...SlfSpiLogger : 0 ThistleSpi | Loading config jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.1-SNAPSHOT/slate-common-11.1-20181012.155613-8.jar!/META-INF/thistle-spi/service.properties <hash> ec8a407a18cb9646b2330dc66bc294b5
```

* Done

<br>
<br>
<br>

# Dependencies

* gradle

```gradle
//Replace `version` with actual value
dependencies {
    compile 'com.github.shepherdviolet:thistle:version'
}
```

* gradle(Least dependencies)

```gradle
//Replace `version` with actual value
dependencies {
    compile ('com.github.shepherdviolet:thistle:version') {
        transitive = false
    }
}
```

* maven

```maven
    <!--Replace `version` with actual value-->
    <dependency>
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>thistle</artifactId>
        <version>version/version>
    </dependency>
```

* maven(Least dependencies)

```maven
    <!--Replace `version` with actual value-->
    <dependency>
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>thistle</artifactId>
        <version>version</version>
        <exclusions>
             <exclusion>
                 <groupId>*</groupId>
                 <artifactId>*</artifactId>
             </exclusion>
        </exclusions>
    </dependency>
```
