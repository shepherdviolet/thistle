# ThistleSpi 使用说明

* Java Service Provider Interfaces (SPI) 变种实现
* 支持`服务装载`和`插件装载`两种方式
* `服务装载`:根据启动参数/优先级, 从Classpath下声明的多个服务实现中选择唯一的一个进行装载
* `插件装载`:装载Classpath下声明的全部插件实现, 并根据优先级排序(数字越小优先级越高), 允许通过启动参数和配置排除部分实现

## 日志信息

* 日志前缀:`ThistleSpi`
* 日志配置:见本文档`Log Config`章节
* [日志样例](https://github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/log-sample.md)

<br>
<br>
<br>

# 用法

* [服务装载](https://github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/service-loading.md)

```text
根据启动参数/优先级, 从Classpath下声明的多个服务实现中选择唯一的一个进行装载
```

* [插件装载](https://github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/plugin-loading.md)

```text
装载Classpath下声明的全部插件实现, 并根据优先级排序(数字越小优先级越高), 允许通过启动参数和配置排除部分实现
```

<br>
<br>
<br>

# Log Config | 日志配置

* Change log level (info by default)

> `-Dthistle.spi.loglv=error` <br>
> `-Dthistle.spi.loglv=info` <br>
> `-Dthistle.spi.loglv=debug` <br>

* Use SLF4J to print log

> Add dependency `com.github.shepherdviolet:slate-common:<version>` to your application <br>

## Customize logger implementation

### Reading the [Service loading manual](https://github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/service-loading.md) before using it !

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

<br>
<br>
<br>

# 临时排除配置文件

```text
当你发现一个依赖包(jar包)内的ThistleSpi配置文件有问题, 无法修改, 又必须依赖这个包, 使用apply/ignore方法也解决不了问题.
我们提供一个临时的解决办法, 可以在启动参数中指定配置文件的hash值将其排除掉(运行时不加载该文件的任何配置). 但这种方法也有
缺点, 因为是根据配置文件内容的hash值排除的, 若依赖包升级(修改)时, hash值有可能发生变化, 导致排除失效. 最终还是要试图联
系依赖包提供方解决配置文件的问题.
```

* 排除方法
* 开启debug级别日志: 添加启动参数`-Dthistle.spi.loglv=debug`
* 如果使用SLF4J打印日志, 还需要确保包路径`sviolet.slate.common.x.common.thistlespi`日志级别在`INFO`级以上
* 运行程序, 观察日志

```text
...SlfSpiLogger : 0 ThistleSpi | -------------------------------------------------------------
...SlfSpiLogger : 0 ThistleSpi | Loading services from META-INF/thistle-spi/, DOC: https://github.com/shepherdviolet/thistle
...SlfSpiLogger : 0 ThistleSpi | Loading config file:/E:/C_Workspace/j2ee-3-test/beet-scrunchy-incubator/beet-scrunchy/out/production/resources/META-INF/thistle-spi/service.properties <hash> 3cf41de0f0d4bdc24970c7b834e2b5c7
...SlfSpiLogger : 0 ThistleSpi | Loading config file:/E:/C_Workspace/j2ee-3-test/beet-scrunchy-incubator/beet-common/out/production/resources/META-INF/thistle-spi/service.properties <hash> 8e3d1cbd3f828ba2a7822581416e97e0
...SlfSpiLogger : 0 ThistleSpi | Loading config jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.1-SNAPSHOT/slate-common-11.1-20181012.155613-8.jar!/META-INF/thistle-spi/service.properties <hash> ec8a407a18cb9646b2330dc66bc294b5
```

* 注意`Loading config ... <hash> ... `日志, `<hash>`后面是该配置文件的hash值
* 找到需要排除的配置文件, 记录其hash值
* 添加启动参数, 格式:-Dthistle.spi.file.exclusion=`hash1`,`hash2`....

```text
-Dthistle.spi.file.exclusion=3cf41de0f0d4bdc24970c7b834e2b5c7,8e3d1cbd3f828ba2a7822581416e97e0
```

* 重启程序, 观察日志

```text
...SlfSpiLogger : 0 ThistleSpi | -------------------------------------------------------------
...SlfSpiLogger : 0 ThistleSpi | Loading services from META-INF/thistle-spi/, DOC: https://github.com/shepherdviolet/thistle
...SlfSpiLogger : 0 ThistleSpi | !!! Exclude config file:/E:/C_Workspace/j2ee-3-test/beet-scrunchy-incubator/beet-scrunchy/out/production/resources/META-INF/thistle-spi/service.properties by -Dthistle.spi.file.exclusion
...SlfSpiLogger : 0 ThistleSpi | !!! Exclude config file:/E:/C_Workspace/j2ee-3-test/beet-scrunchy-incubator/beet-common/out/production/resources/META-INF/thistle-spi/service.properties by -Dthistle.spi.file.exclusion
...SlfSpiLogger : 0 ThistleSpi | Loading config jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.1-SNAPSHOT/slate-common-11.1-20181012.155613-8.jar!/META-INF/thistle-spi/service.properties <hash> ec8a407a18cb9646b2330dc66bc294b5
```

* 指定配置文件已被排除, 程序不会装载其中的Spi配置
