# ThistleSpi Plugin loading guide

```text
When the open source library or framework layer (hereinafter referred to as the underlying) program requires an ordered 
series of implementations, you can use plug-in loading mode, such as: filter / interceptor / processor ...
The underlying provides the interface, and the user application or plugin library (hereinafter referred to as the upper 
layer) implements the interface, add a plugin definition file, declare the `interface/priority/implementation` info. 
After that, when the underlying layer uses the ThistleSpi to load plugins, it will scan the classpath, and return a plugin 
list according to the priority. We can use the configuration file or startup parameters to exclude the specified plugin.
In the processor scenario, a method will be defined in the interface that returns the type which the processor accepts. 
```

* This guide is divided into three chapters `Loading plugin (Bottom layer)` `Implements plugin (Upper layer)` `Ignore plugins (User)`
* Open source library or framework layer developers please read the entire content
* Application or plugin library developers please read the `Implements plugin (Upper layer)` `Ignore plugins (User)`
* Just want to know how to ignore plugins. Please read `Ignore plugins (User)`

<br>
<br>
<br>

# Loading plugin (Bottom layer)

* This section is for open source library / framework layer developers

## Define plugin interface

```text
package sample.spi.facade;

public interface APlugin {

    String invoke(String input);

}
```

```text
package sample.spi.facade;

public interface BPlugin {

    String invoke(String input);

}
```

## Loading plugin instances

```text
    private List<APlugin> aPlugins;
    private List<BPlugin> bPlugins;
    
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
         * Load the plugins list, re-instantiate each time, please hold the plugins object yourself!!!
         * If the plugins is undefined, it will return empty list. If the instantiation fails, a RuntimeException will be thrown.
         */
        aPlugins = serviceLoader.loadPlugins(APlugin.class);
        bPlugins = serviceLoader.loadPlugins(BPlugin.class);
        // You can also load service with the same loader.
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

# Implements plugin (Upper layer)

* This section is for open source library / framework layer / extension library / application developers.

## Implements plugin interface

* The implementation class can only have one public constructor, otherwise it will report an error when the plugin is loaded.
* The class that does not write the constructor, the compiler will automatically generate a public no-argument constructor.
* The constructor parameters currently supported are: No parameter / Single String parameter / Single Properties parameter, with other constructor parameters will give an error

### Implements (With no constructor parameter)

* No explicit constructor

```text
package sample.spi.impl;

public class APluginImpl implements APlugin {
    @Override
    public String invoke(String input) {
        // do something
    }
}
```

* Has no-param constructor, which will be called in instantiation

```text
package sample.spi.impl;

public class APluginImpl implements APlugin {
    public APluginImpl() {
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

public class APluginImpl implements APlugin {
    public APluginImpl(String arg) {
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
* When instantiated, according to the `construct-param` in definition file (as the properties file name), find the properties file (under the path where the plugin definition file is located), load it as Properties instance, then pass to the constructor.

```text
package sample.spi.impl;
import java.util.Properties;

public class APluginImpl implements APlugin {
    public APluginImpl(Properties arg) {
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
    public APluginImpl(Properties parameter) {
        //get and remove _PROPERTIES_URL_
        String propertiesUrl = (String) parameter.remove(ThistleSpi.PROPERTIES_URL);
        // do init
    }
```

<br>

## Definition

### Definition (With no `construct-param`)

* Create and edit file `META-INF/thistle-spi/plugin.properties`

```text
sample.spi.facade.APlugin>64=sample.spi.impl.APluginImpl
```

* Format: `interface-class`>`priority`=`implementation-class`

### Definition (With String `construct-param`)

* Create and edit file `META-INF/thistle-spi/plugin.properties`

```text
sample.spi.facade.APlugin>128=sample.spi.impl.APluginImpl(yyyy-MM-dd HH:mm:ss.SSS)
```

* Format: `interface-class`>`priority`=`implementation-class`(`construct-param`)
* The implementation class constructor can get the `construct-param` defined here (when the constructor has one and only one String parameter)

### Definition (With properties reference `construct-param`)

* Create and edit file `META-INF/thistle-spi/plugin.properties`

```text
sample.spi.facade.APlugin>256=sample.spi.impl.APluginImpl(mypluginconfig.properties)
```

* Format: `interface-class`>`priority`=`implementation-class`(`construct-param`)
* Create a directory `parameter/` under the path `META-INF/thistle-spi/` where the definition file is located, and create a file `mypluginconfig.properties` in the directory.
* Edit `mypluginconfig.properties`: 

```text
parameter1=value1
parameter2=value2
```

* The final directory structure is as follows: 

```text
    myproject/module1/src/main/resources/META-INF/thistle-spi/plugin.properties
    myproject/module1/src/main/resources/META-INF/thistle-spi/parameter/mypluginconfig.properties
```

* `META-INF/thistle-spi/parameter/mypluginconfig.properties` must be created. If it is not found, an error will be reported.
* The properties file and the definition file must be in the same path, because it only loads the `parameter/` directory under the path where the definition file is located.

```text
    # Error example!!! The properties file and the definition file are in different paths (module1 and module2), so that the configuration file will not be found!!!
    myproject/module1/src/main/resources/META-INF/thistle-spi/plugin.properties
    myproject/module2/src/main/resources/META-INF/thistle-spi/parameter/mypluginconfig.properties
```

* After that, the implementation class constructor can get a Properties instance which parsed from the properties file (the constructor must has one and only one Properties parameter)

### Glossary of definition

* `interface-class`: Class name of plugin interface
* `priority`: Priority of implementation

> Integer, the smaller the number, the higher the priority. The first element in the list returned from loadPlugins() method has the highest priority<br>

* `implementation-class`: Class name of plugin implementation

> The implementation class must implement the plugin interface<br>
> Multiple implementations are allowed per plugin, and it will not remove duplicates<br>
> if an implementation is declared multiple times, there will be duplicate instances in the List returned from the loadPlugins() method<br>

* `construct-param`: Constructor parameter, this value will passed as a parameter to the constructor when plugin instantiating

> Take `yyyy-MM-dd HH:mm:ss.SSS` as an example:<br>
> 1.If the implementation class has a String parameter constructor, the constructor will get a string "yyyy-MM-dd HH:mm:ss.SSS"<br>
> 2.If the implementation class has a no parameter constructor, it can be instantiated, but it does not get the string "yyyy-MM-dd HH:mm:ss.SSS"<br>
> 3.If the implementation class has a Properties parameter constructor, an exception will be thrown, because the properties file named yyyy-MM-dd HH:mm:ss.SSS does not exists<br>

> Take `mypluginconfig.properties` as an example:<br>
> 1.If the implementation class has a Properties parameter constructor, it will loads the mypluginconfig.properties file in the `parameter/` directory under the path where the definition file is located. And constructor can get a Properties instance which parsed from it<br>
> 2.If the implementation class has a no parameter constructor, it can be instantiated, but it does not get Properties instance which parsed from it<br>
> 3.If the implementation class has a String parameter constructor, it can be instantiated, but the constructor will get a string "mypluginconfig.properties"<br>

* Note:

> ThistleSpi uses a special way to load the definition file, allowing to have the same key in the same definition file, 
> (the definition with the same `interface-class` and `priority` is allowed). 
> However, except for the properties file referenced by the `construct-param` (the property with the same key is not allowed). 

<br>
<br>
<br>

# Ignore plugins (User)

* By default, ThistleSpi loads all defined implementations and sorts by priority (From small to large)
* There are two ways to exclude the specified implementation.

## By definition file

### Ignore any `construct-param` plugins

* Create a file `META-INF/thistle-spi/plugin-ignore.properties`
* Edit:

```text
sample.spi.facade.APlugin=sample.spi.impl.APluginImpl1,sample.spi.impl.APluginImpl2
```

* Format:`interface-class`=`implementation-class-1`,`implementation-class-2`...

> Take the above as an example<br>
> Exclude implementation `sample.spi.impl.APluginImpl1` (include any `construct-param`)<br>
> Exclude implementation `sample.spi.impl.APluginImpl2` (include any `construct-param`)<br>

### Ignore specified `construct-param` plugins

* Create a file `META-INF/thistle-spi/plugin-ignore.properties`
* Edit:

```text
sample.spi.facade.APlugin=sample.spi.impl.APluginImpl1(true),sample.spi.impl.APluginImpl2(yyyy-MM-dd HH:mm:ss.SSS)
```

* Format:`interface-class`=`implementation-class-1`(`construct-param-1`),`implementation-class-2`(`construct-param-2`)...

> Take the above as an example<br>
> Exclude implementation `sample.spi.impl.APluginImpl1` if `construct-param` = "true"<br>
> Exclude implementation `sample.spi.impl.APluginImpl2` if `construct-param` = "yyyy-MM-dd HH:mm:ss.SSS"<br>

## By startup parameter

### Ignore any `construct-param` plugins

* Add a startup parameter

```text
-Dthistle.spi.ignore.sample.spi.facade.APlugin=sample.spi.impl.APluginImpl1,sample.spi.impl.APluginImpl2
```

* Format: -Dthistle.spi.ignore.`interface-class`=`implementation-class-1`,`implementation-class-2`...

> Take the above as an example<br>
> Exclude implementation `sample.spi.impl.APluginImpl1` (include any `construct-param`)<br>
> Exclude implementation `sample.spi.impl.APluginImpl2` (include any `construct-param`)<br>

### Ignore specified `construct-param` plugins

* Add a startup parameter

```text
-Dthistle.spi.ignore.sample.spi.facade.APlugin=sample.spi.impl.APluginImpl1(true),sample.spi.impl.APluginImpl2(yyyy-MM-dd HH:mm:ss.SSS)
```

* Format: -Dthistle.spi.ignore.`interface-class`=`implementation-class-1`(`construct-param-1`),`implementation-class-2`(`construct-param-2`)...

> Take the above as an example<br>
> Exclude implementation `sample.spi.impl.APluginImpl1` if `construct-param` = "true"<br>
> Exclude implementation `sample.spi.impl.APluginImpl2` if `construct-param` = "yyyy-MM-dd HH:mm:ss.SSS"<br>

## How do I see which plugins are being applied and which ones are being excluded?

* Adjust log-level to debug: Add a startup parameter `-Dthistle.spi.loglv=debug`
* When printing log by `slate-common`, you should adjust SLF4J log-level `sviolet.slate.common.x.common.thistlespi` to `INFO`+
* Run the program, observe the log

```text
...SlfSpiLogger : 0 ThistleSpi | -------------------------------------------------------------
...SlfSpiLogger : 0 ThistleSpi | Plugin Applied:
...SlfSpiLogger : 0 ThistleSpi |   type: sviolet.slate.common.x.conversion.beanutil.PropMapper
...SlfSpiLogger : 0 ThistleSpi |   implements:
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=101001, impl=sviolet.slate.common.x.conversion.beanutil.safe.num.SBUMapperAllNumber2String}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=101002, impl=sviolet.slate.common.x.conversion.beanutil.safe.num.SBUMapperAllNumber2BigDecimal}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=101003, impl=sviolet.slate.common.x.conversion.beanutil.safe.num.SBUMapperAllInteger2BigInteger}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=101004, impl=sviolet.slate.common.x.conversion.beanutil.safe.num.SBUMapperLowlevelNum2Double}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=101005, impl=sviolet.slate.common.x.conversion.beanutil.safe.num.SBUMapperLowlevelNum2Float}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=101006, impl=sviolet.slate.common.x.conversion.beanutil.safe.num.SBUMapperLowlevelNum2Long}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=101007, impl=sviolet.slate.common.x.conversion.beanutil.safe.num.SBUMapperLowlevelNum2Integer}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=102001, impl=sviolet.slate.common.x.conversion.beanutil.safe.date.SBUMapperAllDate2String(yyyy-MM-dd HH:mm:ss.SSS)}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=102002, impl=sviolet.slate.common.x.conversion.beanutil.safe.date.SBUMapperAllDate2SqlDate}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=102003, impl=sviolet.slate.common.x.conversion.beanutil.safe.date.SBUMapperAllDate2SqlTimestamp}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=102004, impl=sviolet.slate.common.x.conversion.beanutil.safe.date.SBUMapperAllDate2UtilDate}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=102005, impl=sviolet.slate.common.x.conversion.beanutil.safe.date.SBUMapperString2SqlDate}
...SlfSpiLogger : 0 ThistleSpi | All Configurations:
...SlfSpiLogger : 0 ThistleSpi |   - Plugin{priority=102007, impl=sviolet.slate.common.x.conversion.beanutil.safe.date.SBUMapperString2UtilDate, disable by -Dthistle.spi.ignore.sviolet.slate.common.x.conversion.beanutil.PropMapper=sviolet.slate.common.x.conversion.beanutil.safe.date.SBUMapperString2UtilDate,sviolet.slate.common.x.conversion.beanutil.safe.date.SBUMapperString2SqlTimestamp, url=jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.2-SNAPSHOT/slate-common-11.2-20181018.120813-5.jar!/META-INF/thistle-spi/plugin.properties}
...SlfSpiLogger : 0 ThistleSpi |   - Plugin{priority=102006, impl=sviolet.slate.common.x.conversion.beanutil.safe.date.SBUMapperString2SqlTimestamp, disable by -Dthistle.spi.ignore.sviolet.slate.common.x.conversion.beanutil.PropMapper=sviolet.slate.common.x.conversion.beanutil.safe.date.SBUMapperString2UtilDate,sviolet.slate.common.x.conversion.beanutil.safe.date.SBUMapperString2SqlTimestamp, url=jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.2-SNAPSHOT/slate-common-11.2-20181018.120813-5.jar!/META-INF/thistle-spi/plugin.properties}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=102005, impl=sviolet.slate.common.x.conversion.beanutil.safe.date.SBUMapperString2SqlDate, url=jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.2-SNAPSHOT/slate-common-11.2-20181018.120813-5.jar!/META-INF/thistle-spi/plugin.properties}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=102004, impl=sviolet.slate.common.x.conversion.beanutil.safe.date.SBUMapperAllDate2UtilDate, url=jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.2-SNAPSHOT/slate-common-11.2-20181018.120813-5.jar!/META-INF/thistle-spi/plugin.properties}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=102003, impl=sviolet.slate.common.x.conversion.beanutil.safe.date.SBUMapperAllDate2SqlTimestamp, url=jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.2-SNAPSHOT/slate-common-11.2-20181018.120813-5.jar!/META-INF/thistle-spi/plugin.properties}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=102002, impl=sviolet.slate.common.x.conversion.beanutil.safe.date.SBUMapperAllDate2SqlDate, url=jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.2-SNAPSHOT/slate-common-11.2-20181018.120813-5.jar!/META-INF/thistle-spi/plugin.properties}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=101007, impl=sviolet.slate.common.x.conversion.beanutil.safe.num.SBUMapperLowlevelNum2Integer, url=jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.2-SNAPSHOT/slate-common-11.2-20181018.120813-5.jar!/META-INF/thistle-spi/plugin.properties}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=102001, impl=sviolet.slate.common.x.conversion.beanutil.safe.date.SBUMapperAllDate2String(yyyy-MM-dd HH:mm:ss.SSS), url=jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.2-SNAPSHOT/slate-common-11.2-20181018.120813-5.jar!/META-INF/thistle-spi/plugin.properties}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=101006, impl=sviolet.slate.common.x.conversion.beanutil.safe.num.SBUMapperLowlevelNum2Long, url=jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.2-SNAPSHOT/slate-common-11.2-20181018.120813-5.jar!/META-INF/thistle-spi/plugin.properties}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=101005, impl=sviolet.slate.common.x.conversion.beanutil.safe.num.SBUMapperLowlevelNum2Float, url=jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.2-SNAPSHOT/slate-common-11.2-20181018.120813-5.jar!/META-INF/thistle-spi/plugin.properties}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=101004, impl=sviolet.slate.common.x.conversion.beanutil.safe.num.SBUMapperLowlevelNum2Double, url=jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.2-SNAPSHOT/slate-common-11.2-20181018.120813-5.jar!/META-INF/thistle-spi/plugin.properties}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=101003, impl=sviolet.slate.common.x.conversion.beanutil.safe.num.SBUMapperAllInteger2BigInteger, url=jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.2-SNAPSHOT/slate-common-11.2-20181018.120813-5.jar!/META-INF/thistle-spi/plugin.properties}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=101002, impl=sviolet.slate.common.x.conversion.beanutil.safe.num.SBUMapperAllNumber2BigDecimal, url=jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.2-SNAPSHOT/slate-common-11.2-20181018.120813-5.jar!/META-INF/thistle-spi/plugin.properties}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=101001, impl=sviolet.slate.common.x.conversion.beanutil.safe.num.SBUMapperAllNumber2String, url=jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.2-SNAPSHOT/slate-common-11.2-20181018.120813-5.jar!/META-INF/thistle-spi/plugin.properties}
```

* In the example above we can see:

> Plugin interface is: sviolet.slate.common.x.conversion.beanutil.PropMapper<br>
> `implements:` is a list of plugins currently in effect (sorted)<br>
> `All Configurations:` is all plugin definitions, `-` the beginning is not valid, `disable by` shows why it was disabled, `url` is the plugin definition file path<br>
