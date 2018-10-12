# ThistleSpi 使用说明

* Java Service Provider Interfaces (SPI) 变种实现
* 支持`服务装载`和`插件装载`两种方式
* `服务装载`:根据启动参数/优先级, 从Classpath下声明的多个服务实现中选择唯一的一个进行装载
* `插件装载`:装载Classpath下声明的全部插件实现, 并根据优先级排序(数字越小优先级越高), 允许通过启动参数和配置排除部分实现

## 日志

* 日志前缀:`ThistleSpi`
* 日志配置:见本文档`Log Config`章节
* [日志样例](https://github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/log-sample.md)

# 服务装载

## 定义接口类

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

## 加载服务

```text
    private AService aService;
    private BService bService;
    
    public void init(){
        /*
         * 获取服务加载器, 第一次获取会有创建过程, 后续从缓存中获得.<br>
         * 1.尽量用同一个加载器加载服务和插件, 不要反复创建加载器.<br>
         * 2.创建过程会加载所有jar包中的相关配置文件, 根据策略决定每个服务的实现类, 决定每个插件的实现列表.<br>
         * 3.配置文件解析出错时会抛出RuntimeException异常.<br>
         * 4.若设置启动参数-Dthistle.spi.cache=false, 则每次都会重新创建加载器.<br>
         * 5.如果有需要(动态类加载/Jar包热插拔/多ClassLoader/自定义ClassLoader), 请使用newLoader方法创建并自行维护加载器.<br>
         *
         * 支持:
         * newLoader方法:能够自定义配置文件路径和类加载器<br>
         * getLoader方法:能够自定义配置文件路径, getLoader方法第一次创建加载器, 后续会从缓存中获取<br>
         */
        ThistleSpi.ServiceLoader serviceLoader = ThistleSpi.getLoader();
        /*
         * 加载服务, 每次都会重新实例化, 请自行持有插件对象
         * 若服务未定义会返回空, 实例化失败会抛出RuntimeException异常
         */
        aService = serviceLoader.loadService(AService.class);
        bService = serviceLoader.loadService(BService.class);
        // 还可以用同一个加载器加载插件
        // ......
    }
```

## 服务实现类

* 实现类支持`无参构造器`
* 实现类支持`只有一个参数, 且参数类型为String的构造器`(简称`有参构造器`), 注意该参数可能传入空值
* 服务声明中无`构造参数`时, 实例化时优先调用`无参构造器`, 若不存在则调用`有参构造器`
* 服务声明中有`构造参数`时, 实例化时优先调用`有参构造器`, 若不存在则调用`无参构造器`
* 有无`构造参数`见`声明服务的实现`章节

```text
package sample.spi.impl;

public class AServiceImpl1 implements AService {
    /**
     * 无参构造器
     */
    //public AServiceImpl1() {
    //
    //}
    
    /**
     * 只有一个参数, 且参数类型为String的构造器
     * 注意该参数可能为空
     */
    //public AServiceImpl1(String arg) {
    //
    //}
    
    @Override
    public String invoke(String input) {
        // do something
    }
}
```

```text
package sample.spi.impl;

public class AServiceImpl2 implements AService {
    @Override
    public String invoke(String input) {
        // do something
    }
}
```

## 声明服务的实现

* 创建文件`META-INF/thistle-spi/service.properties`
* 编辑文件:

```text
sample.spi.facade.AService>sample-lib-1>library=sample.spi.impl.AServiceImpl1(yyyy-MM-dd HH:mm:ss.SSS)
sample.spi.facade.AService>sample-lib-2>library=sample.spi.impl.AServiceImpl2
```

* 格式(无构造参数): `服务接口名`>`ID`>`级别`=`服务实现类名`
* 格式(有构造参数): `服务接口名`>`ID`>`级别`=`服务实现类名`(`构造参数`)
* `服务接口名`: 服务的接口类全限定名
* `ID`: 实现ID

> 字母加横线, 切勿与其他服务提供库重复, 实现ID重名会抛出异常<br>
> 在一个服务有多个实现时, 可以通过`ID`指定用哪个实现<br>

* `级别`: 服务级别

> 优先级: `application`>`platform`>`library`>`default`<br>
> 在一个服务有多个实现时, 程序会使用优先级高的实现<br>
> 默认实现请使用`default`级别, 扩展实现请勿使用该级别<br>
> 开源扩展库(开源插件库)请使用`library`级别, 使用户能够用`application`/`platform`两个级别覆盖实现<br>
> 用户项目的基础工程(平台框架)建议使用`platform`级别, 基础工程会被应用工程依赖, 因此存在被覆盖实现的需求<br>
> 用户项目的应用工程建议使用`application`级别, 应用工程最终用于部署投产, 不会有被覆盖实现的需求<br>

* `服务实现类名`: 服务实现类全限定名

> 服务实现类必须实现服务接口<br>
> 每个服务允许有多个实现, 但要求有不同的ID<br>
> 若最高级别的实现不止一个, 则会抛出异常, 称为实现冲突<br>
> 冲突解决方法1: 将不需要的实现提供库(jar)排除依赖<br>
> 冲突解决方法2: 使用`service-apply.properties`配置指定服务实现ID<br>
> 冲突解决方法3: 使用`-Dthistle.spi.apply.<interface>=<id>`指定服务实现ID<br>

* `构造参数`: 服务实例化时会传入构造方法

> 只支持一个构造参数, 无需用""包裹, 有参构造方法会获得括号内的值<br>
> 如果实现类没有有参构造器(只有一个参数, 且类型为String), 则会调用无参构造器实例化服务<br>
> 如果实现类没有无参构造器, 但声明中无构造参数, 程序会尝试调用有参构造器实例化服务, 并传入null值<br>

## 指定服务实现

* 默认情况下, 程序会采用优先级最高的实现, 无需进行指定
* 若最高级别的实现不止一个(发生冲突), 或想要采用低优先级的实现时, 可以通过两种方法指定
* `启动参数方式`优先级高于`配置文件方式`

### 配置文件方式

* 创建文件`META-INF/thistle-spi/service-apply.properties`
* 编辑文件:

```text
sample.spi.facade.AService=sample-lib-1
```

* 格式:`服务接口名`=`ID`

> 指定服务使用哪个实现(指定ID)<br>
> 注意:不建议开源库和用户项目的基础工程使用该配置, 一般用于用户项目的应用工程<br>
> 注意:若一个服务被指定使用两个不同的ID, 会抛出异常<br>

### 启动参数方式

* 添加启动参数

```text
-Dthistle.spi.apply.sample.spi.facade.AService=sample-lib-1
```

* 格式: -Dthistle.spi.apply.`服务接口名`=`ID`

> 指定服务使用哪个实现(指定ID)<br>

<br>
<br>
<br>

# 插件装载

## 定义接口类

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

## 加载插件

```text
    private List<APlugin> aPlugins;
    private List<BPlugin> bPlugins;
    
    public void init(){
        /*
         * 获取服务加载器, 第一次获取会有创建过程, 后续从缓存中获得.<br>
         * 1.尽量用同一个加载器加载服务和插件, 不要反复创建加载器.<br>
         * 2.创建过程会加载所有jar包中的相关配置文件, 根据策略决定每个服务的实现类, 决定每个插件的实现列表.<br>
         * 3.配置文件解析出错时会抛出RuntimeException异常.<br>
         * 4.若设置启动参数-Dthistle.spi.cache=false, 则每次都会重新创建加载器.<br>
         * 5.如果有需要(动态类加载/Jar包热插拔/多ClassLoader/自定义ClassLoader), 请使用newLoader方法创建并自行维护加载器.<br>
         *
         * 支持:
         * newLoader方法:能够自定义配置文件路径和类加载器<br>
         * getLoader方法:能够自定义配置文件路径, getLoader方法第一次创建加载器, 后续会从缓存中获取<br>
         */
        ThistleSpi.ServiceLoader serviceLoader = ThistleSpi.getLoader();
        /*
         * 加载插件, 每次都会重新实例化, 请自行持有插件对象
         * 若插件未定义会返回空列表, 实例化失败会抛出RuntimeException异常
         */
        aPlugins = serviceLoader.loadPlugins(APlugin.class);
        bPlugins = serviceLoader.loadPlugins(BPlugin.class);
        // 还可以用同一个加载器加载服务
        // ......
    }
```

## 插件实现类

* 实现类支持`无参构造器`
* 实现类支持`只有一个参数, 且参数类型为String的构造器`(简称`有参构造器`), 注意该参数可能传入空值
* 插件声明中无`构造参数`时, 实例化时优先调用`无参构造器`, 若不存在则调用`有参构造器`
* 插件声明中有`构造参数`时, 实例化时优先调用`有参构造器`, 若不存在则调用`无参构造器`
* 有无`构造参数`见`声明插件的实现`章节

```text
package sample.spi.impl;

public class APluginImpl1 implements APlugin {
    /**
     * 无参构造器
     */
    //public APluginImpl1() {
    //
    //}
    
    /**
     * 只有一个参数, 且参数类型为String的构造器
     * 注意该参数可能为空
     */
    //public APluginImpl1(String arg) {
    //
    //}
    
    @Override
    public String invoke(String input) {
        // do something
    }
}
```

```text
package sample.spi.impl;

public class APluginImpl2 implements APlugin {
    @Override
    public String invoke(String input) {
        // do something
    }
}
```

## 声明插件的实现

* 创建文件`META-INF/thistle-spi/plugin.properties`
* 编辑文件:

```text
sample.spi.facade.APlugin>64=sample.spi.impl.APluginImpl1(yyyy-MM-dd HH:mm:ss.SSS)
sample.spi.facade.APlugin>128=sample.spi.impl.APluginImpl2
```

* 格式(无构造参数): `插件接口名`>`优先级`=`插件实现类名`
* 格式(有构造参数): `插件接口名`>`优先级`=`插件实现类名`(`构造参数`)
* `插件接口名`: 插件的接口类全限定名
* `优先级`: 插件优先级

> 整数, 数字越小优先级越高, loadPlugins返回的List中第一个元素优先级最高<br>

* `插件实现类名`: 插件实现类全限定名

> 插件实现类必须实现插件接口<br>
> 每个插件接口允许有多个实现, 且不会进行去重<br>
> 若同一个插件实现被声明了多次, loadPlugins返回的List中也会存在多个相同的插件实例<br>

* `构造参数`: 服务实例化时会传入构造方法

> 只支持一个构造参数, 无需用""包裹, 有参构造方法会获得括号内的值<br>
> 如果实现类没有有参构造器(只有一个参数, 且类型为String), 则会调用无参构造器实例化插件<br>
> 如果实现类没有无参构造器, 但声明中无构造参数, 程序会尝试调用有参构造器实例化插件, 并传入null值<br>

* 注意:

> 同一个配置文件中, 不允许出现`插件接口名`与`优先级`都相同的配置, 会发生配置丢失. 
> 如果需要在一个配置文件中给一个`插件接口名`配置多个实现时, 请编写不同的优先级, 避免冲突.

## 排除插件实现

* 默认情况下, 程序会应用所有声明了的实现
* 可以通过两种方法排除指定的实现

### 配置文件方式

#### 排除任意构造参数的全部实现

* 创建文件`META-INF/thistle-spi/plugin-ignore.properties`
* 编辑文件:

```text
sample.spi.facade.APlugin=sample.spi.impl.APluginImpl1,sample.spi.impl.APluginImpl1
```

* 格式:`插件接口名`=`插件实现类名1`,`插件实现类名2`...

> 以上面为例<br>
> 将任意构造参数的`sample.spi.impl.APluginImpl1`实现全部排除<br>
> 将任意构造参数的`sample.spi.impl.APluginImpl2`实现全部排除<br>

#### 排除指定构造参数的实现

* 创建文件`META-INF/thistle-spi/plugin-ignore.properties`
* 编辑文件:

```text
sample.spi.facade.APlugin=sample.spi.impl.APluginImpl1(true),sample.spi.impl.APluginImpl2(yyyy-MM-dd HH:mm:ss.SSS)
```

* 格式:`插件接口名`=`插件实现类名1`(`指定构造参数1`),`插件实现类名2`(`指定构造参数2`)...

> 以上面为例<br>
> 将构造参数为`true`的`sample.spi.impl.APluginImpl1`实现排除<br>
> 将构造参数为`yyyy-MM-dd HH:mm:ss.SSS`的`sample.spi.impl.APluginImpl2`实现排除<br>

### 启动参数方式

#### 排除任意构造参数的全部实现

* 添加启动参数

```text
-Dthistle.spi.ignore.sample.spi.facade.APlugin=sample.spi.impl.APluginImpl1,sample.spi.impl.APluginImpl2
```

* 格式: -Dthistle.spi.ignore.`插件接口名`=`插件实现类名1`,`插件实现类名2`...

> 以上面为例<br>
> 将任意构造参数的`sample.spi.impl.APluginImpl1`实现全部排除<br>
> 将任意构造参数的`sample.spi.impl.APluginImpl2`实现全部排除<br>

#### 排除指定构造参数的实现

* 添加启动参数

```text
-Dthistle.spi.ignore.sample.spi.facade.APlugin=sample.spi.impl.APluginImpl1(true),sample.spi.impl.APluginImpl2(yyyy-MM-dd HH:mm:ss.SSS)
```

* 格式: -Dthistle.spi.ignore.`插件接口名`=`插件实现类名1`(`指定构造参数1`),`插件实现类名2`(`指定构造参数2`)...

> 以上面为例<br>
> 将构造参数为`true`的`sample.spi.impl.APluginImpl1`实现排除<br>
> 将构造参数为`yyyy-MM-dd HH:mm:ss.SSS`的`sample.spi.impl.APluginImpl2`实现排除<br>

<br>
<br>
<br>

# 加载自定义路径下的配置

* 默认情况下, ThistleSpi的加载器会加载`META-INF/thistle-spi/`路径下的配置文件(service.properties/service-apply.properties/plugin.properties/plugin-ignore.properties)
* 我们可以通过如下方法指定自定义的配置路径

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

* 注意:`META-INF/thistle-spi-logger/`路径无法修改

<br>
<br>
<br>

# Log Config

* Change log level (info by default)

> `-Dthistle.spi.loglv=error` <br>
> `-Dthistle.spi.loglv=info` <br>
> `-Dthistle.spi.loglv=debug` <br>

* Use SLF4J to print log

> Add dependency `com.github.shepherdviolet:slate-common:<version>` to your application <br>

## Customize logger implementation

### Implementation

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

### Config

* Create file `META-INF/thistle-spi-logger/service.properties`
* Edit:

```text
sviolet.thistle.x.common.thistlespi.SpiLogger>sample-app>application=sample.spi.logger.CustomSpiLogger
```

### Specify logger id if you want (Non-essential)

* If the auto-loaded logger is not what you want, you can specify logger id as follows

#### By config file

* Create file `META-INF/thistle-spi-logger/service-apply.properties`
* Edit:

```text
sviolet.thistle.x.common.thistlespi.SpiLogger=sample-app
```

#### By JVM argument

* Add argument in your startup shell

```text
-Dthistle.spi.apply.sviolet.thistle.x.common.thistlespi.SpiLogger=sample-app
```
