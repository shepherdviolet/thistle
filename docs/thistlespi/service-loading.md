# ThistleSpi 服务加载指南

```text
当开源库或框架层(以下简称底层)的程序中, 只需要唯一的一个实现时, 可以使用服务加载模式, 例如: 底层提供接口和默认实现, 
允许用户工程或插件库(以下简称上层)替换为自定义实现的场景. 上层实现接口后, 添加服务定义文件, 声明服务的接口/ID/优先级/实现. 
底层使用ThistleSpi加载服务时, 会扫描classpath, 默认根据服务优先级决定使用哪个实现(优先级最高的一个), 当最高优先级存在多个实现时, 
用户可以使用配置文件或启动参数指定用哪个实现. 
```

* 本指南分三个章节`加载服务(底层)` `实现服务(上层)` `指定使用哪个实现(用户)`
* 开源库或框架层开发者请阅读全部内容
* 用户工程或扩展库开发者请阅读`实现服务(上层)` `指定使用哪个实现(用户)`
* 单纯想了解如何指定实现/解决冲突的方法请阅读`指定使用哪个实现(用户)`

<br>
<br>
<br>

# 加载服务(底层)

* 本章节供 开源库或框架层开发者 阅读

## 定义服务接口

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
         * newLoader方法:能够自定义配置文件路径和类加载器<br>
         * getLoader方法:能够自定义配置文件路径, getLoader方法第一次创建加载器, 后续会从缓存中获取<br>
         */
        ThistleSpi.ServiceLoader serviceLoader = ThistleSpi.getLoader();
        /*
         * 加载服务, 每次都会重新实例化, 请自行持有服务对象!!!
         * 若服务未定义会返回空, 实例化失败会抛出RuntimeException异常
         */
        aService = serviceLoader.loadService(AService.class);
        bService = serviceLoader.loadService(BService.class);
        // 还可以用同一个加载器加载插件
        // ......
    }
```

## 加载自定义路径下的服务

* 默认情况下, 加载器会加载`META-INF/thistle-spi/`路径下的:service.properties, service-apply.properties, plugin.properties, plugin-ignore.properties, parameter/*.properties
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

* 注意:ThistleSpi自身日志打印器的`META-INF/thistle-spi-logger/`路径无法修改

<br>
<br>
<br>

# 实现服务(上层)

* 本章节供 开源库或框架层开发者 和 用户工程或扩展库开发者 阅读

## 实现服务接口

* 实现类只能有一个public构造器(构造方法), 否则在服务加载时会报错
* 未编写构造器的类, 编译器会自动生成一个public无参构造器
* 目前构造器支持的参数为: 无构造参数 / 一个String构造参数 / 一个Properties构造参数, 拥有其他构造参数会报错

### 实现服务接口(无构造参数)

* 无显式声明的构造器

```text
package sample.spi.impl;

public class AServiceImpl implements AService {
    @Override
    public String invoke(String input) {
        // do something
    }
}
```

* 有显式声明的无参构造器, 服务实例化时会调用该构造器

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

### 实现服务接口(一个String构造参数)

* 只有一个参数, 且参数类型为String的构造器
* 服务实例化时, 会将服务定义文件中的构造参数传入这个构造器
* 注意: 这个构造参数可能为空(如果服务定义文件中未设置构造参数)

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

### 实现服务接口(一个Properties构造参数)

* 只有一个参数, 且参数类型为java.util.Properties的构造器
* 服务实例化时, 会根据服务定义文件中的构造参数的值, 作为配置文件名, 找到服务定义文件所在路径下的配置文件, 加载为Properties传入这个构造器

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

* 构造器获得的`Properties`对象中会有一个key为`_PROPERTIES_URL_`的参数, 该参数的值为配置文件的路径. 当构造器中发生异常时, 建议将该值打印到日志中便于定位问题, 如果需要遍历Properties, 请先移除这个参数.

```text
    public AServiceImpl(Properties parameter) {
        //get and remove _PROPERTIES_URL_
        String propertiesUrl = (String) parameter.remove(ThistleSpi.PROPERTIES_URL);
        // do init
    }
```

<br>

## 服务定义

### 无构造参数的定义

* 创建定义文件`META-INF/thistle-spi/service.properties`, 并编辑

```text
sample.spi.facade.AService>sample-lib-1>library=sample.spi.impl.AServiceImpl
```

* 格式: `服务接口名`>`ID`>`级别`=`服务实现类名`

### 有构造参数的定义(普通字符串构造参数)

* 创建定义文件`META-INF/thistle-spi/service.properties`, 并编辑

```text
sample.spi.facade.AService>sample-lib-2>library=sample.spi.impl.AServiceImpl(yyyy-MM-dd HH:mm:ss.SSS)
```

* 格式: `服务接口名`>`ID`>`级别`=`服务实现类名`(`构造参数`)
* 服务实现类构造器能够获得这里定义的构造参数(构造器有且仅有一个String入参时)

### 有构造参数的定义(引用配置文件名)

* 创建定义文件`META-INF/thistle-spi/service.properties`, 并编辑

```text
sample.spi.facade.AService>sample-lib-2>library=sample.spi.impl.AServiceImpl(myconfig.properties)
```

* 格式: `服务接口名`>`ID`>`级别`=`服务实现类名`(`构造参数`)
* 在定义文件所在路径`META-INF/thistle-spi/`下创建目录`parameter/`, 然后在目录中创建配置文件`myconfig.properties`
* 编辑配置文件: 

```text
# 添加服务所需配置参数
parameter1=value1
parameter2=value2
```

* 最终目录结构如下: 

```text
    myproject/module1/src/main/resources/META-INF/thistle-spi/service.properties
    myproject/module1/src/main/resources/META-INF/thistle-spi/parameter/myconfig.properties
```

* 配置文件`META-INF/thistle-spi/parameter/myconfig.properties`必须创建, 找不到会报错
* 配置文件与定义文件必须在同一个路径下, 因为服务加载时只查找定义文件所在路径下的 `parameter/`目录

```text
    # 错误示例!!! 定义文件与配置文件在不同的路径下(module1和module2), 这样会报找不到配置文件!!!
    myproject/module1/src/main/resources/META-INF/thistle-spi/service.properties
    myproject/module2/src/main/resources/META-INF/thistle-spi/parameter/myconfig.properties
```

* 定义完成后, 服务实现类构造器能够获得配置文件中的配置(构造器有且仅有一个Properties入参时)

### 字段说明

* `服务接口名`: 服务的接口类全限定名
* `ID`: 实现ID

> 字母加横线, 切勿与其他服务提供库重复, 实现ID重名会抛出异常<br>
> 在一个服务有多个实现时, 可以通过`ID`指定用哪个实现<br>

* `级别`: 扩展级别

> 优先级: `application`>`platform`>`library`>`default`<br>
> 在一个服务有多个实现时, 程序会使用优先级高的实现<br>
> 默认实现请使用`default`级别, 扩展实现请勿使用该级别<br>
> 开源扩展库请使用`library`级别, 使用户能够用`application`/`platform`两个级别覆盖实现<br>
> 用户项目的基础工程(平台框架)建议使用`platform`级别, 基础工程会被应用工程依赖, 因此存在被覆盖实现的需求<br>
> 用户项目的应用工程建议使用`application`级别, 应用工程最终用于部署投产, 不会有被覆盖实现的需求<br>

* `服务实现类名`: 服务实现类全限定名

> 服务实现类必须实现服务接口<br>
> 每个服务允许有多个实现, 但要求有不同的ID<br>
> 若最高级别的实现不止一个, 则会抛出异常, 称为实现冲突<br>
> 冲突解决方法1: 将不需要的实现提供库(jar)排除依赖<br>
> 冲突解决方法2: 使用`service-apply.properties`配置指定服务实现ID, 见后面的章节<br>
> 冲突解决方法3: 使用`-Dthistle.spi.apply.<interface>=<id>`指定服务实现ID, 见后面的章节<br>

* `构造参数`: 服务实例化时会将该值作为构造参数传入构造器

> 以构造参数(yyyy-MM-dd HH:mm:ss.SSS)为例:<br>
> 1.服务实现类构造器有且仅有一个String构造参数时, 构造器会获得字符串"yyyy-MM-dd HH:mm:ss.SSS"<br>
> 2.服务实现类构造器没有构造参数时, 服务能够实例化, 但构造器无从获得字符串"yyyy-MM-dd HH:mm:ss.SSS"<br>
> 3.服务实现类构造器有且仅有一个Properties构造参数时, 服务实例化报错, 因为定义文件所在路径下的parameter/目录中不存在名为yyyy-MM-dd HH:mm:ss.SSS的配置文件<br>

> 以构造参数(myconfig.properties)为例:<br>
> 1.服务实现类构造器有且仅有一个Properties构造参数时, 会在定义文件所在路径下的parameter/目录中寻找myconfig.properties配置文件并加载, 构造器会获得配置(Properties)<br>
> 2.服务实现类构造器没有构造参数时, 服务能够实例化, 但构造器无从获得配置(Properties)<br>
> 3.服务实现类构造器有且仅有一个String构造参数时, 服务能够实例化, 但构造器会获得字符串"myconfig.properties"<br>

<br>
<br>
<br>

# 指定使用哪个实现(用户)

* 默认情况下, ThistleSpi会加载优先级最高的实现, 无需进行指定 (优先级 `application`>`platform`>`library`>`default`)
* 若最高级别的实现不止一个(发生冲突), 或想要采用低优先级的实现时, 可以通过两种方式指定
* `启动参数方式`优先级高于`配置文件方式`

## 配置文件方式

* 创建文件`META-INF/thistle-spi/service-apply.properties`
* 编辑文件:

```text
sample.spi.facade.AService=sample-lib-1
```

* 格式:`服务接口名`=`ID`

> 指定服务使用哪个实现(指定ID)<br>
> 注意:不建议开源库和用户项目的基础工程使用该配置, 一般用于用户项目的应用工程(最终用户)<br>
> 注意:若一个服务被指定使用两个不同的ID, 会抛出异常<br>

## 启动参数方式

* 添加启动参数

```text
-Dthistle.spi.apply.sample.spi.facade.AService=sample-lib-1
```

* 格式: -Dthistle.spi.apply.`服务接口名`=`ID`

> 指定服务使用哪个实现(指定ID)<br>
> 注意:若一个服务被指定使用两个不同的ID, 会抛出异常<br>

## 如何查看一个实现为何被应用? 还有那些备选实现?

* 开启debug级别日志: 添加启动参数`-Dthistle.spi.loglv=debug`
* 如果使用SLF4J打印日志, 还需要确保包路径`sviolet.slate.common.x.common.thistlespi`日志级别在`INFO`级以上
* 运行程序, 观察日志

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

* 上面的示例中我们可以看到:

> 服务接口为: sviolet.slate.common.x.monitor.txtimer.TxTimerProvider<br>
> 当前应用的实现为: sample.spi.txtimer.CustomTxTimerProvider<br>
> 当前应用的实现定义在: jar:file:/C:/m2repository/repository/com/github/shepherdviolet/sample-project/11.1-SNAPSHOT/sample-project-1.0.jar!/META-INF/thistle-spi/service.properties<br>
> 该实现被应用的原因: 优先级策略<br>
> 所有关于该接口的实现有两个, 见日志中`All Configurations:`下面的列表, +开头的是现在被应用的实现, id是服务id, level是优先级, impl是实现类<br>

* 我们可以使用`service-apply.properties`定义文件或启动参数, 在备选实现中选择一个实现
