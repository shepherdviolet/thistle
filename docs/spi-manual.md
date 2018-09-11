# Thistle SPI 使用说明

* Disable log

> `-Dthistle.spi.debug=false`

* Custom logger implementation (System.out by default), should implement `sviolet.thistle.util.common.ThistleSpi.Logger` interface

> `-Dthistle.spi.logger=sample.base.LoggerImpl`

# 服务定义与加载

## 定义接口类

```text
package sample.spi.facade;

public interface IService {

    String invoke(String input);

}
```

## 加载服务

```text
    private IService service;
    private IPlugin plugin;
    private IUtil util;
    
    public void init(){
        /*
         * 创建服务加载器: 
         * 创建过程会加载所有jar包中的相关配置文件, 根据策略决定每个服务的实现类.
         * 加载器会持有加载过的所有服务和ClassLoader, 重复调用loadService方法会返回同一个实例.
         * 加载多个服务时, 建议使用同一个加载器(避免重复加载相关配置).
         * 如果有动态类加载的需要, 可以在重新加载时, 创建一个新的服务加载器, 新的类加载器会重新加载配置和类.
         */
        ThistleSpi.ServiceLoader serviceLoader = ThistleSpi.newLoader();
        /*
         * 加载服务
         */
        service = serviceLoader.loadService(IService.class);
        plugin = serviceLoader.loadService(IPlugin.class);
        util = serviceLoader.loadService(IUtil.class);
    }
```

# 服务实现与声明

## 编写实现类

```text
package sample.spi.impl;

public class LibService implements IService {
    @Override
    public String invoke(String input) {
        // do something
    }
}
```

## 声明服务实现

* 创建文件`META-INF/thistle.spi.service`
* 编辑文件:

```text
id=mylib
level=library
sample.spi.facade.IService=sample.spi.impl.LibService
sample.spi.facade.IPlugin=sample.spi.impl.LibPlugin
sample.spi.facade.IUtil=sample.spi.impl.LibUtil
```

* 参数:`id`

> 服务ID, 任意字符串, 不要与其他服务提供库重复<br>
> `必须配置`, 作用于该配置文件中声明的所有服务实现<br>
> 在一个服务有多个实现的场合, 我们可以通过`服务ID`指定使用哪个实现<br>

* 参数:`level`

> 服务级别, 三选一:`application`/`platform`/`library`<br>
> `必须配置`, 作用于该配置文件中声明的所有服务实现<br>
> 在一个服务有多个实现的场合, 程序会使用优先级高的实现<br>
> 优先级:`application`>`platform`>`library`<br>
> 开源库请使用`library`级别, 使用户能够用`application`/`platform`两个级别覆盖实现<br>
> 用户项目的基础工程建议使用`platform`级别, 基础工程会被应用工程依赖, 因此存在被覆盖实现的需求<br>
> 用户项目的应用工程建议使用`application`级别, 应用工程最终用于部署投产, 不会有被覆盖实现的需求<br>

* 参数:`接口`=`实现`

> 服务实现声明, 等号左边为服务接口类全限定名, 等号右边为服务实现类全限定名<br>
> 可配置多个服务的实现类, 同一个配置文件中声明的所有服务实现, `id`和`level`都相同<br>
> 服务实现类必须实现了服务接口<br>

# 指定服务实现

* 默认情况下, 程序会采用优先级最高的实现, 无需进行指定
* 当多种实现发生冲突, 或想要采用低优先级的实现时, 可以通过两种方法指定
* 启动参数方式优先级高于配置文件方式

## 配置文件方式

* 创建文件`META-INF/thistle.spi.apply`
* 编辑文件:

```text
sample.spi.facade.IService=mylib
sample.spi.facade.IPlugin=mylib
sample.spi.facade.IUtil=mylib
```

* 参数:`接口`=`ID`

> 指定服务使用哪个实现, 等号左边为服务接口类全限定名, 等号右边为服务实现的ID<br>
> 注意:不建议开源库和用户项目的基础工程使用该配置, 一般用于用户项目的应用工程<br>
> 注意:若同一个服务配置了多个不同的ID(即使在不同的配置文件中), 会抛出异常(程序崩溃)<br>

## 启动参数方式

* 添加启动参数

```text
-Dthistle.spi.apply.sample.spi.facade.IService=mylib
```

* 参数:-Dthistle.spi.apply.`接口`=`ID`

> 指定服务使用哪个实现, 等号左边为`-Dthistle.spi.apply.`加上服务接口类全限定名, 等号右边为服务实现的ID<br>
