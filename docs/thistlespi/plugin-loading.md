# ThistleSpi 插件加载指南

```text
当开源库或框架层(以下简称底层)的程序中, 需要有序的一系列实现时, 可以使用插件加载模式, 例如: 过滤器 / 拦截器 / 处理器等场景. 
底层定义接口, 用户工程或插件库(以下简称上层)实现接口后, 添加插件定义文件, 声明插件的接口/优先度/实现, 底层使用ThistleSpi加载
插件时, 会扫描classpath, 默认根据插件的优先度排序后返回一个插件列表, 用户可以使用配置文件或启动参数排除指定的插件. 
在处理器场景时, 接口中定义一个方法, 返回处理器接收的数据类型, 底层可以根据这个方法决定处理器何时使用. 
```

* 本指南分三个章节`开源库或框架层(底层)加载插件` `用户工程或插件库(上层)实现插件` `用户排除不需要的插件实现`
* 开源库或框架层开发者请阅读全部内容
* 用户工程或插件库开发者请阅读`用户工程或插件库(上层)实现插件` `用户排除不想要的插件`
* 单纯想了解排除插件的方法请阅读`用户排除不需要的插件实现`

<br>
<br>
<br>

# 开源库或框架层(底层)加载插件

* 本章节供 开源库或框架层开发者 阅读

## 定义插件接口

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

## 加载自定义路径下的插件

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

# 用户工程或插件库(上层)实现插件

* 本章节供 开源库或框架层开发者 和 用户工程或插件库开发者 阅读

## 实现插件接口

* 实现类只能有一个public构造器(构造方法), 否则在插件加载时会报错
* 未编写构造器的类, 编译器会自动生成一个public无参构造器
* 目前构造器支持的参数为: 无构造参数 / 一个String构造参数 / 一个Properties构造参数, 拥有其他构造参数会报错

### 实现插件接口(无构造参数)

* 无显式声明的构造器

```text
package sample.spi.impl;

public class APluginImpl implements APlugin {
    @Override
    public String invoke(String input) {
        // do something
    }
}
```

* 有显式声明的无参构造器, 插件实例化时会调用该构造器

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

### 实现插件接口(一个String构造参数)

* 只有一个参数, 且参数类型为String的构造器
* 插件实例化时, 会将插件定义文件中的构造参数传入这个构造器
* 注意: 这个构造参数可能为空(如果插件定义文件中未设置构造参数)

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

### 实现插件接口(一个Properties构造参数)

* 只有一个参数, 且参数类型为java.util.Properties的构造器
* 插件实例化时, 会根据插件定义文件中的构造参数的值, 作为配置文件名, 找到插件定义文件所在路径下的配置文件, 加载其中的配置, 最后将Properties传入这个构造器
* 注意这个构造参数可能为空(如果插件定义文件中未设置构造参数)

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

<br>

## 插件定义

### 无构造参数的定义

* 创建定义文件`META-INF/thistle-spi/plugin.properties`, 并编辑

```text
sample.spi.facade.APlugin>64=sample.spi.impl.APluginImpl
```

* 格式: `插件接口名`>`优先度`=`插件实现类名`

### 有构造参数的定义(普通字符串构造参数)

* 创建定义文件`META-INF/thistle-spi/plugin.properties`, 并编辑

```text
sample.spi.facade.APlugin>128=sample.spi.impl.APluginImpl(yyyy-MM-dd HH:mm:ss.SSS)
```

* 格式: `插件接口名`>`优先度`=`插件实现类名`(`构造参数`)
* 插件实现类构造器能够获得这里定义的构造参数(构造器有且仅有一个String入参时)

### 有构造参数的定义(引用配置文件名)

* 创建定义文件`META-INF/thistle-spi/plugin.properties`, 并编辑

```text
sample.spi.facade.APlugin>256=sample.spi.impl.APluginImpl(mypluginconfig.properties)
```

* 格式: `插件接口名`>`优先度`=`插件实现类名`(`构造参数`)
* 在定义文件所在路径`META-INF/thistle-spi/`下创建目录`parameter/`, 然后在目录中创建配置文件`mypluginconfig.properties`
* 编辑配置文件: 

```text
# 添加插件所需配置参数
parameter1=value1
parameter2=value2
```

* 最终目录结构如下: 

```text
    myproject/module1/src/main/resources/META-INF/thistle-spi/plugin.properties
    myproject/module1/src/main/resources/META-INF/thistle-spi/parameter/mypluginconfig.properties
```

* 配置文件`META-INF/thistle-spi/parameter/mypluginconfig.properties`必须创建, 找不到会报错
* 配置文件与定义文件必须在同一个路径下, 因为插件加载时只查找定义文件所在路径下的 `parameter/`目录

```text
    # 错误示例!!! 定义文件与配置文件在不同的路径下(module1和module2), 这样会报找不到配置文件!!!
    myproject/module1/src/main/resources/META-INF/thistle-spi/plugin.properties
    myproject/module2/src/main/resources/META-INF/thistle-spi/parameter/mypluginconfig.properties
```

* 定义完成后, 插件实现类构造器能够获得配置文件中的配置(构造器有且仅有一个Properties入参时)

### 字段说明

* `插件接口名`: 插件的接口类全限定名
* `优先度`: 插件优先度

> 整数, 数字越小优先度越高, loadPlugins返回的List中第一个元素优先度最高<br>

* `插件实现类名`: 插件实现类全限定名

> 插件实现类必须实现插件接口<br>
> 每个插件接口允许有多个实现, 且不会进行去重<br>
> 若同一个插件实现被声明了多次, loadPlugins返回的List中也会存在多个相同的插件实例<br>

* `构造参数`: 插件实例化时会将该值作为构造参数传入构造器

> 以构造参数(yyyy-MM-dd HH:mm:ss.SSS)为例:<br>
> 1.插件实现类构造器有且仅有一个String构造参数时, 构造器会获得字符串"yyyy-MM-dd HH:mm:ss.SSS"<br>
> 2.插件实现类构造器没有构造参数时, 插件能够实例化, 但构造器无从获得字符串"yyyy-MM-dd HH:mm:ss.SSS"<br>
> 3.插件实现类构造器有且仅有一个Properties构造参数时, 插件实例化报错! 因为定义文件所在路径下的parameter/目录中不存在名为yyyy-MM-dd HH:mm:ss.SSS的配置文件<br>

> 以构造参数(mypluginconfig.properties)为例:<br>
> 1.插件实现类构造器有且仅有一个Properties构造参数时, 会在定义文件所在路径下的parameter/目录中寻找mypluginconfig.properties配置文件并加载, 构造器会获得配置(Properties)<br>
> 2.插件实现类构造器没有构造参数时, 插件能够实例化, 但构造器无从获得配置(Properties)<br>
> 3.插件实现类构造器有且仅有一个String构造参数时, 插件能够实例化, 但构造器会获得字符串"mypluginconfig.properties"<br>

* 注意:

> 同一个配置文件中, 不允许出现`插件接口名`与`优先度`都相同的配置, 会发生配置丢失. 
> 如果需要在一个配置文件中给一个`插件接口名`配置多个实现时, 请编写不同的优先度, 避免冲突.

<br>
<br>
<br>

# 用户排除不需要的插件实现

* 默认情况下, ThistleSpi会加载所有定义的实现, 并按优先级排序(数值从小到大)
* 可以通过两种方法排除指定的实现

## 配置文件方式

### 排除插件实现(任何构造参数)

* 创建文件`META-INF/thistle-spi/plugin-ignore.properties`
* 编辑文件:

```text
sample.spi.facade.APlugin=sample.spi.impl.APluginImpl1,sample.spi.impl.APluginImpl2
```

* 格式:`插件接口名`=`插件实现类名1`,`插件实现类名2`...

> 以上面为例<br>
> 将`sample.spi.impl.APluginImpl1`插件实现排除(任何构造参数)<br>
> 将`sample.spi.impl.APluginImpl2`插件实现排除(任何构造参数)<br>

### 排除插件实现(指定构造参数)

* 创建文件`META-INF/thistle-spi/plugin-ignore.properties`
* 编辑文件:

```text
sample.spi.facade.APlugin=sample.spi.impl.APluginImpl1(true),sample.spi.impl.APluginImpl2(yyyy-MM-dd HH:mm:ss.SSS)
```

* 格式:`插件接口名`=`插件实现类名1`(`指定构造参数1`),`插件实现类名2`(`指定构造参数2`)...

> 以上面为例<br>
> 将构造参数定义为`true`的`sample.spi.impl.APluginImpl1`插件实现排除<br>
> 将构造参数定义为`yyyy-MM-dd HH:mm:ss.SSS`的`sample.spi.impl.APluginImpl2`插件实现排除<br>

## 启动参数方式

### 排除插件实现(任何构造参数)

* 添加启动参数

```text
-Dthistle.spi.ignore.sample.spi.facade.APlugin=sample.spi.impl.APluginImpl1,sample.spi.impl.APluginImpl2
```

* 格式: -Dthistle.spi.ignore.`插件接口名`=`插件实现类名1`,`插件实现类名2`...

> 以上面为例<br>
> 将`sample.spi.impl.APluginImpl1`插件实现排除(任何构造参数)<br>
> 将`sample.spi.impl.APluginImpl2`插件实现排除(任何构造参数)<br>

### 排除插件实现(指定构造参数)

* 添加启动参数

```text
-Dthistle.spi.ignore.sample.spi.facade.APlugin=sample.spi.impl.APluginImpl1(true),sample.spi.impl.APluginImpl2(yyyy-MM-dd HH:mm:ss.SSS)
```

* 格式: -Dthistle.spi.ignore.`插件接口名`=`插件实现类名1`(`指定构造参数1`),`插件实现类名2`(`指定构造参数2`)...

> 以上面为例<br>
> 将构造参数为`true`的`sample.spi.impl.APluginImpl1`插件实现排除<br>
> 将构造参数为`yyyy-MM-dd HH:mm:ss.SSS`的`sample.spi.impl.APluginImpl2`插件实现排除<br>

## 如何查看哪些插件被应用, 哪些被排除? 

* 开启debug级别日志: 添加启动参数`-Dthistle.spi.loglv=debug`
* 如果使用SLF4J打印日志, 还需要确保包路径`sviolet.slate.common.x.common.thistlespi`日志级别在`INFO`级以上
* 运行程序, 观察日志


