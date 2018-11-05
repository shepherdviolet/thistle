# ThistleSpi Service loading guide

```text
When the open source library or framework layer (hereinafter referred to as the underlying) program only needs a unique 
implementation, you can use the service loading mode. For example: the underlying provides the interface and the default 
implementation,the user application or plugin library (hereafter referred to as the upper layer) implements custom logic, 
add a service definition file, declare the 'interface-class/ID/priority/implementation-class' info. After that, when 
the underlying layer uses the ThistleSpi to load the service, it will scan the classpath, and apply the highest priority 
one by default. When there are multiple implementations with the highest priority, we can specify which implementation 
to use with the configuration file or startup parameters.
```

* This guide is divided into three chapters `Loading service (Bottom layer)` `Implements service (Upper layer)` `Specify which implementation to apply (User)`
* Open source library or framework layer developers please read the entire content
* Application or plugin library developers please read the `Implements service (Upper layer)` `Specify which implementation to apply (User)`
* Just want to know how to specify a service impl or resolve conflicts. Please read `Specify which implementation to apply (User)`

<br>
<br>
<br>

# Loading service (Bottom layer)

* This section is for open source library / framework layer developers

## Define service interface

```text
package sample.spi.facade;

public interface AService {

    String invoke(String input);

}
```

```text
package sample.spi.facade;

public interface BService {

    String invoke(String input);

}
```

## Loading service instance

```text
    private AService aService;
    private BService bService;
    
    public void init(){
        /*
         * Get the service loader, the first acquisition will have a creation process, and the subsequent acquisition will from the cache.<br>
         * 1.The creation process loads the relevant definition files in all jar packages, determines the implementation class of each service according to the policy, and determines the implementation list of each plugin.<br>
         * 2.A RuntimeException is thrown when a configuration file parsing error occurs.<br>
         * 3.If the startup parameter -Dthistle.spi.cache=false is set, the loader will be recreated each time.<br>
         *
         * newLoader: Ability to customize config path and classloader<br>
         * getLoader: Ability to customize config path, the getLoader method first creates the loader, and subsequently gets it from the cache<br>
         */
        ThistleSpi.ServiceLoader serviceLoader = ThistleSpi.getLoader();
        /*
         * Load the service, re-instantiate each time, please hold the service object yourself!!!
         * If the service is undefined, it will return null. If the instantiation fails, a RuntimeException will be thrown.
         */
        aService = serviceLoader.loadService(AService.class);
        bService = serviceLoader.loadService(BService.class);
        // You can also load plugins with the same loader.
        // ......
    }
```

## Load the service under the custom config path

* By default, it will loads the definitions from `META-INF/thistle-spi/`. Files: service.properties, service-apply.properties, plugin.properties, plugin-ignore.properties, parameter/*.properties
* We can specify a custom path by the following way: 

```text
    private AService aService;
    private BService bService;
    private List<APlugin> aPlugins;
    private List<BPlugin> bPlugins;
    
    public void init(){
        ThistleSpi.ServiceLoader serviceLoader = ThistleSpi.newLoader(Thread.currentThread().getContextClassLoader(), "META-INF/custom-path/");
        aService = serviceLoader.loadService(AService.class);
        bService = serviceLoader.loadService(BService.class);
        aPlugins = serviceLoader.loadPlugins(APlugin.class);
        bPlugins = serviceLoader.loadPlugins(BPlugin.class);
    }
```

* Note: The `META-INF/thistle-spi-logger/` path of inner-logger cannot be modified.

<br>
<br>
<br>

# Implements service (Upper layer)

* This section is for open source library / framework layer / extension library / application developers.

## Implements service interface

* The implementation class can only have one public constructor, otherwise it will report an error when the service is loaded.
* The class that does not write the constructor, the compiler will automatically generate a public no-argument constructor.
* The constructor parameters currently supported are: No parameter / Single String parameter / Single Properties parameter, with other constructor parameters will give an error

### Implements (With no constructor parameter)

* No explicit constructor

```text
package sample.spi.impl;

public class AServiceImpl implements AService {
    @Override
    public String invoke(String input) {
        // do something
    }
}
```

* Has no-param constructor, which will be called in instantiation

```text
package sample.spi.impl;

public class AServiceImpl implements AService {
    public AServiceImpl() {
        // do init
    }
    @Override
    public String invoke(String input) {
        // do something
    }
}
```

### Implements (With single String parameter)

* A constructor with only one String parameter
* When instantiated, the `construct-param` which defined in definition file will be passed to this constructor
* Note: This constructor parameter may be null (if the `construct-param` is not defined in the definition file)

```text
package sample.spi.impl;

public class AServiceImpl implements AService {
    public AServiceImpl(String arg) {
        // do init
    }
    @Override
    public String invoke(String input) {
        // do something
    }
}
```

### Implements (With single Properties parameter)

* A constructor with only one java.util.Properties parameter
* When instantiated, according to the `construct-param` in definition file (as the properties file name), find the properties file (under the path where the service definition file is located), load it as Properties instance, then pass to the constructor.

```text
package sample.spi.impl;
import java.util.Properties;

public class AServiceImpl implements AService {
    public AServiceImpl(Properties arg) {
        // do init
    }
    @Override
    public String invoke(String input) {
        // do something
    }
}
```

* The `Properties` will have a property with key `_PROPERTIES_URL_`, it's the file path of properties
* When an exception occurs, it is recommended to print `_PROPERTIES_URL_` value to the log for easy positioning
* If you need to iterate for Properties, please remove key `_PROPERTIES_URL_` first

```text
    public AServiceImpl(Properties parameter) {
        //get and remove _PROPERTIES_URL_
        String propertiesUrl = (String) parameter.remove(ThistleSpi.PROPERTIES_URL);
        // do init
    }
```

<br>

## Definition

### Definition (With no `construct-param`)

* Create and edit file `META-INF/thistle-spi/service.properties`

```text
sample.spi.facade.AService>sample-lib-1>library=sample.spi.impl.AServiceImpl
```

* Format: `interface-class`>`ID`>`level`=`implementation-class`

### Definition (With String `construct-param`)

* Create and edit file `META-INF/thistle-spi/service.properties`

```text
sample.spi.facade.AService>sample-lib-2>library=sample.spi.impl.AServiceImpl(yyyy-MM-dd HH:mm:ss.SSS)
```

* Format: `interface-class`>`ID`>`level`=`implementation-class`(`construct-param`)
* The implementation class constructor can get the `construct-param` defined here (when the constructor has one and only one String parameter)

### Definition (With properties reference `construct-param`)

* Create and edit file `META-INF/thistle-spi/service.properties`

```text
sample.spi.facade.AService>sample-lib-2>library=sample.spi.impl.AServiceImpl(myconfig.properties)
```

* Format: `interface-class`>`ID`>`level`=`implementation-class`(`construct-param`)
* Create a directory `parameter/` under the path `META-INF/thistle-spi/` where the definition file is located, and create a file `myconfig.properties` in the directory.
* Edit `myconfig.properties`: 

```text
parameter1=value1
parameter2=value2
```

* The final directory structure is as follows: 

```text
    myproject/module1/src/main/resources/META-INF/thistle-spi/service.properties
    myproject/module1/src/main/resources/META-INF/thistle-spi/parameter/myconfig.properties
```

* `META-INF/thistle-spi/parameter/myconfig.properties` must be created. If it is not found, an error will be reported.
* The properties file and the definition file must be in the same path, because it only loads the `parameter/` directory under the path where the definition file is located.

```text
    # Error example!!! The properties file and the definition file are in different paths (module1 and module2), so that the configuration file will not be found!!!
    myproject/module1/src/main/resources/META-INF/thistle-spi/service.properties
    myproject/module2/src/main/resources/META-INF/thistle-spi/parameter/myconfig.properties
```

* After that, the implementation class constructor can get a Properties instance which parsed from the properties file (the constructor must has one and only one Properties parameter)

### Glossary of definition

* `interface-class`: Class name of service interface
* `ID`: Implementation ID

> Letters and horizontal lines, don't duplicate with other service providers, otherwise an exception will be thrown<br>
> When a service has multiple implementations, you can specify which implementation to apply by `ID`<br>

* `level`: Extension level

> Priority: `application`>`platform`>`library`>`default`<br>
> When a service has multiple implementations, the program apply a higher-priority implementation by default<br>
> Default implementation should use the `default` level, and the extension implementation should not use this level<br>
> Open source extension library uses the `library` level, Allow users to override with two levels of `application`/`platform`<br>
> Infrastructure project of user application is recommended to use the `platform` level, because application project will depend on it, user can override it by `application` level<br>
> Application project is recommended to use the `application` level. It's finally used for deployment and production<br>

* `implementation-class`: Class name of service implementation

> The implementation class must implement the service interface<br>
> Multiple implementations are allowed per service, but different IDs are required<br>
> If there is more than one implementation at the highest level, an exception will be thrown, called an `implementation conflict`<br>
> Conflict resolution 1: Exclude unwanted implementation library (JAR) from dependency<br>
> Conflict resolution 2: Specify the ID of the implementation in `service-apply.properties` file, see the following sections<br>
> Conflict resolution 3: Specify the ID of the implementation by startup parameter `-Dthistle.spi.apply.<interface-class>=<ID>`, see the following sections<br>

* `construct-param`: Constructor parameter, this value will passed as a parameter to the constructor when service instantiating

> Take `yyyy-MM-dd HH:mm:ss.SSS` as an example:<br>
> 1.If the implementation class has a String parameter constructor, the constructor will get a string "yyyy-MM-dd HH:mm:ss.SSS"<br>
> 2.If the implementation class has a no parameter constructor, it can be instantiated, but it does not get the string "yyyy-MM-dd HH:mm:ss.SSS"<br>
> 3.If the implementation class has a Properties parameter constructor, an exception will be thrown, because the properties file named yyyy-MM-dd HH:mm:ss.SSS does not exists<br>

> Take `myconfig.properties` as an example:<br>
> 1.If the implementation class has a Properties parameter constructor, it will loads the myconfig.properties file in the `parameter/` directory under the path where the definition file is located. And constructor can get a Properties instance which parsed from it<br>
> 2.If the implementation class has a no parameter constructor, it can be instantiated, but it does not get Properties instance which parsed from it<br>
> 3.If the implementation class has a String parameter constructor, it can be instantiated, but the constructor will get a string "myconfig.properties"<br>

<br>
<br>
<br>

# Specify which implementation to apply (User)

* By default, ThistleSpi loads the highest priority implementation, no need to specify (Priority `application`>`platform`>`library`>`default`)
* If the highest level of implementation is more than one (implementation conflict), or if you want to apply a low-priority implementation, you can specify it in two ways
* `Startup parameter`'s priority is higher than `definition file`

## By definition file

* Create a file `META-INF/thistle-spi/service-apply.properties`
* Edit:

```text
sample.spi.facade.AService=sample-lib-1
```

* Format:`interface-class`=`ID`

> Specify which implementation you want (by specify it's ID)<br>
> Note: It is not recommended for the Open source extension library and infrastructure project, it is used for application project (end user project)<br>
> Note: If a service is specified to apply two different IDs, an exception will be thrown<br>

## By startup parameter

* Add a startup parameter

```text
-Dthistle.spi.apply.sample.spi.facade.AService=sample-lib-1
```

* Format: -Dthistle.spi.apply.`interface-class`=`ID`

> Specify which implementation you want (by specify it's ID)<br>
> This way can be used to resolve ID conflicts in 'service-apply.properties' files
> Note: If a service is specified to apply two different IDs, an exception will be thrown<br>

## How do I see why an implementation is being applied? And what alternative implementations?

* Adjust log-level to debug: Add a startup parameter `-Dthistle.spi.loglv=debug`
* When printing log by `slate-common`, you should adjust SLF4J log-level `sviolet.slate.common.x.common.thistlespi` to `INFO`+
* Run the program, observe the log

```text
...SlfSpiLogger : 0 ThistleSpi | -------------------------------------------------------------
...SlfSpiLogger : 0 ThistleSpi | Service Applied:
...SlfSpiLogger : 0 ThistleSpi |   type: sviolet.slate.common.x.monitor.txtimer.TxTimerProvider
...SlfSpiLogger : 0 ThistleSpi |   implement: sample.spi.txtimer.CustomTxTimerProvider
...SlfSpiLogger : 0 ThistleSpi |   url: jar:file:/C:/m2repository/repository/com/github/shepherdviolet/sample-project/11.1-SNAPSHOT/sample-project-1.0.jar!/META-INF/thistle-spi/service.properties
...SlfSpiLogger : 0 ThistleSpi |   reason: Applied by level (application > platform > library > default)
...SlfSpiLogger : 0 ThistleSpi | All Configurations:
...SlfSpiLogger : 0 ThistleSpi |   + Service{id=sample, level=LIBRARY, impl=sample.spi.txtimer.CustomTxTimerProvider, arg=null, url=jar:file:/C:/m2repository/repository/com/github/shepherdviolet/sample-project/11.1-SNAPSHOT/sample-project-1.0.jar!/META-INF/thistle-spi/service.properties}
...SlfSpiLogger : 0 ThistleSpi |   - Service{id=slate-common, level=DEFAULT, impl=sviolet.slate.common.x.monitor.txtimer.def.DefaultTxTimerProvider, arg=null, url=jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.1-SNAPSHOT/slate-common-11.1-20181012.155613-8.jar!/META-INF/thistle-spi/service.properties}
...SlfSpiLogger : 0 ThistleSpi | -------------------------------------------------------------
```

* In the example above we can see:

> Service interface is: sviolet.slate.common.x.monitor.txtimer.TxTimerProvider<br>
> Applied implementation is: sample.spi.txtimer.CustomTxTimerProvider<br>
> Definition in: jar:file:/C:/m2repository/repository/com/github/shepherdviolet/sample-project/11.1-SNAPSHOT/sample-project-1.0.jar!/META-INF/thistle-spi/service.properties<br>
> Applied reason: By level<br>
> There are two implementations for this interface, See `All Configurations:`, the beginning of + is the implementation that is now applied<br>

* We can specify wanted implementation by ID.
