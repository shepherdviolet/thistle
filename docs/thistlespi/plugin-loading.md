# ThistleSpi 插件装载

# 定义接口类

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

# 加载插件

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

# 插件实现类

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

# 声明插件的实现

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

# 排除插件实现

* 默认情况下, 程序会应用所有声明了的实现
* 可以通过两种方法排除指定的实现

## 配置文件方式

### 排除任意构造参数的全部实现

* 创建文件`META-INF/thistle-spi/plugin-ignore.properties`
* 编辑文件:

```text
sample.spi.facade.APlugin=sample.spi.impl.APluginImpl1,sample.spi.impl.APluginImpl1
```

* 格式:`插件接口名`=`插件实现类名1`,`插件实现类名2`...

> 以上面为例<br>
> 将任意构造参数的`sample.spi.impl.APluginImpl1`实现全部排除<br>
> 将任意构造参数的`sample.spi.impl.APluginImpl2`实现全部排除<br>

### 排除指定构造参数的实现

* 创建文件`META-INF/thistle-spi/plugin-ignore.properties`
* 编辑文件:

```text
sample.spi.facade.APlugin=sample.spi.impl.APluginImpl1(true),sample.spi.impl.APluginImpl2(yyyy-MM-dd HH:mm:ss.SSS)
```

* 格式:`插件接口名`=`插件实现类名1`(`指定构造参数1`),`插件实现类名2`(`指定构造参数2`)...

> 以上面为例<br>
> 将构造参数为`true`的`sample.spi.impl.APluginImpl1`实现排除<br>
> 将构造参数为`yyyy-MM-dd HH:mm:ss.SSS`的`sample.spi.impl.APluginImpl2`实现排除<br>

## 启动参数方式

### 排除任意构造参数的全部实现

* 添加启动参数

```text
-Dthistle.spi.ignore.sample.spi.facade.APlugin=sample.spi.impl.APluginImpl1,sample.spi.impl.APluginImpl2
```

* 格式: -Dthistle.spi.ignore.`插件接口名`=`插件实现类名1`,`插件实现类名2`...

> 以上面为例<br>
> 将任意构造参数的`sample.spi.impl.APluginImpl1`实现全部排除<br>
> 将任意构造参数的`sample.spi.impl.APluginImpl2`实现全部排除<br>

### 排除指定构造参数的实现

* 添加启动参数

```text
-Dthistle.spi.ignore.sample.spi.facade.APlugin=sample.spi.impl.APluginImpl1(true),sample.spi.impl.APluginImpl2(yyyy-MM-dd HH:mm:ss.SSS)
```

* 格式: -Dthistle.spi.ignore.`插件接口名`=`插件实现类名1`(`指定构造参数1`),`插件实现类名2`(`指定构造参数2`)...

> 以上面为例<br>
> 将构造参数为`true`的`sample.spi.impl.APluginImpl1`实现排除<br>
> 将构造参数为`yyyy-MM-dd HH:mm:ss.SSS`的`sample.spi.impl.APluginImpl2`实现排除<br>

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
