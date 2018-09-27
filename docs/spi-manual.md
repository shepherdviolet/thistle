# ThistleSpi 使用说明

* Java Service Provider Interfaces (SPI) 变种实现
* 支持`服务装载`和`插件装载`两种方式
* `服务装载`:根据启动参数/优先级, 从Classpath下声明的多个服务实现中选择唯一的一个进行装载
* `插件装载`:装载Classpath下声明的全部插件实现, 并根据优先级排序(数字越小优先级越高), 允许通过启动参数和配置排除部分实现

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
         * [非线程安全]<br>
         * 创建一个新的服务加载器(无缓存).<br>
         * 1.尽量用同一个加载器加载服务和插件, 不要反复创建加载器.<br>
         * 2.创建过程会加载所有jar包中的相关配置文件, 根据策略决定每个服务的实现类, 决定每个插件的实现列表.<br>
         * 3.如果有动态类加载的需要, 可以重新创建一个新的服务加载器, 新的类加载器会重新加载配置.<br>
         * 4.配置文件解析出错时会抛出RuntimeException异常.<br>
         *
         * 其他:
         * newLoader方法:能够自定义配置文件路径和类加载器<br>
         * getLoader方法:getLoader方法第一次创建加载器, 后续会从缓存中获取<br>
         * getLoader方法:能够自定义配置文件路径<br>
         */
        ThistleSpi.ServiceLoader serviceLoader = ThistleSpi.newLoader();
        /*
         * 加载服务, 若服务未定义会返回空, 实例化失败会抛出RuntimeException异常
         */
        aService = serviceLoader.loadService(AService.class);
        bService = serviceLoader.loadService(BService.class);
        // 还可以用同一个加载器加载插件
        // ......
    }
```

## 服务实现类

```text
package sample.spi.impl;

public class AServiceImpl1 implements AService {
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
sample.spi.facade.AService>sample-lib-1>library=sample.spi.impl.AServiceImpl1
sample.spi.facade.AService>sample-lib-2>library=sample.spi.impl.AServiceImpl2
```

* 格式: `服务接口名`>`ID`>`级别`=`服务实现类名`
* `服务接口名`: 服务的接口类全限定名
* `ID`: 实现ID

> 字母加横线, 切勿与其他服务提供库重复, 实现ID重名会抛出异常<br>
> 在一个服务有多个实现时, 可以通过`ID`指定用哪个实现<br>

* `级别`: 服务级别

> 三选一: `application`/`platform`/`library`<br>
> 优先级: `application`>`platform`>`library`<br>
> 在一个服务有多个实现时, 程序会使用优先级高的实现<br>
> 开源库请使用`library`级别, 使用户能够用`application`/`platform`两个级别覆盖实现<br>
> 用户项目的基础工程建议使用`platform`级别, 基础工程会被应用工程依赖, 因此存在被覆盖实现的需求<br>
> 用户项目的应用工程建议使用`application`级别, 应用工程最终用于部署投产, 不会有被覆盖实现的需求<br>

* `服务实现类名`: 服务实现类全限定名

> 服务实现类必须实现服务接口<br>
> 每个服务允许有多个实现, 但要求有不同的ID<br>
> 若最高级别的实现不止一个, 则会抛出异常, 称为实现冲突<br>
> 冲突解决方法1: 将不需要的实现提供库(jar)排除依赖<br>
> 冲突解决方法2: 使用`service-apply.properties`配置指定服务实现ID<br>
> 冲突解决方法3: 使用`-Dthistle.spi.apply.<interface>=<id>`指定服务实现ID<br>

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
         * [非线程安全]<br>
         * 创建一个新的服务加载器(无缓存).<br>
         * 1.尽量用同一个加载器加载服务和插件, 不要反复创建加载器.<br>
         * 2.创建过程会加载所有jar包中的相关配置文件, 根据策略决定每个服务的实现类, 决定每个插件的实现列表.<br>
         * 3.如果有动态类加载的需要, 可以重新创建一个新的服务加载器, 新的类加载器会重新加载配置.<br>
         * 4.配置文件解析出错时会抛出RuntimeException异常.<br>
         * 
         * 其他:
         * newLoader方法:能够自定义配置文件路径和类加载器<br>
         * getLoader方法:getLoader方法第一次创建加载器, 后续会从缓存中获取<br>
         * getLoader方法:能够自定义配置文件路径<br>
         */
        ThistleSpi.ServiceLoader serviceLoader = ThistleSpi.newLoader();
        /*
         * 加载插件, 若插件未定义会返回空, 实例化失败会抛出RuntimeException异常
         */
        aPlugins = serviceLoader.loadPlugins(APlugin.class);
        bPlugins = serviceLoader.loadPlugins(BPlugin.class);
        // 还可以用同一个加载器加载服务
        // ......
    }
```

## 插件实现类

```text
package sample.spi.impl;

public class APluginImpl1 implements APlugin {
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
sample.spi.facade.APlugin>64=sample.spi.impl.APluginImpl1
sample.spi.facade.APlugin>128=sample.spi.impl.APluginImpl2
```

* 格式: `插件接口名`>`优先级`=`插件实现类名`
* `插件接口名`: 插件的接口类全限定名
* `优先级`: 插件优先级

> 整数, 数字越小优先级越高, loadPlugins返回的List中第一个元素优先级最高<br>

* `插件实现类名`: 插件实现类全限定名

> 插件实现类必须实现插件接口<br>
> 每个插件允许有多个实现, 且不会进行去重<br>
> 若存在接口/优先级/实现类都相同声明, loadPlugins返回的List中也会存在复数个相同的插件<br>

## 排除插件实现

* 默认情况下, 程序会应用所有声明了的实现
* 可以通过两种方法排除指定的实现

### 配置文件方式

* 创建文件`META-INF/thistle-spi/plugin-ignore.properties`
* 编辑文件:

```text
sample.spi.facade.APlugin=sample.spi.impl.APluginImpl1,sample.spi.impl.APluginImpl2
```

* 格式:`插件接口名`=`插件实现类名1`,`插件实现类名2`...

> 以上面为例, 将`sample.spi.facade.APlugin`插件的`sample.spi.impl.APluginImpl1`和`sample.spi.impl.APluginImpl2`实现排除<br>

### 启动参数方式

* 添加启动参数

```text
-Dthistle.spi.ignore.sample.spi.facade.APlugin=sample.spi.impl.APluginImpl1,sample.spi.impl.APluginImpl2
```

* 格式: -Dthistle.spi.ignore.`插件接口名`=`插件实现类名1`,`插件实现类名2`...

> 以上面为例, 将`sample.spi.facade.APlugin`插件的`sample.spi.impl.APluginImpl1`和`sample.spi.impl.APluginImpl2`实现排除<br>

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

# Log

* Change log level (debug by default)

> `-Dthistle.spi.loglv=error` <br>
> `-Dthistle.spi.loglv=debug` <br>
> `-Dthistle.spi.loglv=verbose` <br>

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
sviolet.thistle.util.spi.SpiLogger>sample-app>application=sample.spi.logger.CustomSpiLogger
```

### Specify logger id if you want (Non-essential)

* If the auto-loaded logger is not what you want, you can specify logger id as follows

#### By config file

* Create file `META-INF/thistle-spi-logger/service-apply.properties`
* Edit:

```text
sviolet.thistle.util.spi.SpiLogger=sample-app
```

#### By JVM argument

* Add argument in your startup shell

```text
-Dthistle.spi.apply.sviolet.thistle.util.spi.SpiLogger=sample-app
```
