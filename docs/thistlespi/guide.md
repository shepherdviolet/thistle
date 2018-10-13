# ThistleSpi 使用说明

* Java Service Provider Interfaces (SPI) 变种实现
* 支持`服务装载`和`插件装载`两种方式
* `服务装载`:根据启动参数/优先级, 从Classpath下声明的多个服务实现中选择唯一的一个进行装载
* `插件装载`:装载Classpath下声明的全部插件实现, 并根据优先级排序(数字越小优先级越高), 允许通过启动参数和配置排除部分实现

## 日志

* 日志前缀:`ThistleSpi`
* 日志配置:见本文档`Log Config`章节
* [日志样例](https://github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/log-sample.md)

<br>
<br>
<br>

# 用法

* [服务装载(根据启动参数/优先级, 从Classpath下声明的多个服务实现中选择唯一的一个进行装载)](https://github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/service-loading.md)
* [插件装载(装载Classpath下声明的全部插件实现, 并根据优先级排序(数字越小优先级越高), 允许通过启动参数和配置排除部分实现)](https://github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/plugin-loading.md)

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
