# ThistleSpi 使用指南

* [English Guide](https://github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/guide-en.md)
* ThistleSpi是一种SPI(Service Provider Interfaces)的变种实现
* `Maven/Gradle依赖配置`在本文最后

```text
在我们开发开源库或框架层的时候, 通常会希望我们的程序能够: 1.支持用户工程自定义部分逻辑(替换默认实现); 2.支持第三方插件的扩展; 
开源库或框架层提供接口, 用户工程或插件库实现接口, 通过配置使得程序最终加载自定义的逻辑或插件, 这就是SPI. 
Java本身提供了一个SPI规范, 但它没有过滤机制, 当多个实现在classpath中共存时, 没有提供服务选择/插件排除的功能, 使用上有所不便. 
ThistleSpi增加了过滤机制, 支持构造参数, 在使用上更为便捷. 
```

<br>
<br>
<br>

# 服务加载模式

```text
当开源库或框架层(以下简称底层)的程序中, 只需要唯一的一个实现时, 可以使用服务加载模式, 例如: 底层提供接口和默认实现, 
允许用户工程或插件库(以下简称上层)替换为自定义实现的场景. 上层实现接口后, 添加服务定义文件, 声明服务的接口/ID/优先级/实现. 
底层使用ThistleSpi加载服务时, 会扫描classpath, 默认根据服务优先级决定使用哪个实现(优先级最高的一个), 当最高优先级存在多个实现时, 
用户可以使用配置文件或启动参数指定用哪个实现. 
```

* [服务加载指南](https://github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/service-loading.md)

<br>
<br>
<br>

# 插件加载模式

```text
当开源库或框架层(以下简称底层)的程序中, 需要有序的一系列实现时, 可以使用插件加载模式, 例如: 过滤器 / 拦截器 / 处理器等场景. 
底层定义接口, 用户工程或插件库(以下简称上层)实现接口后, 添加插件定义文件, 声明插件的接口/优先度/实现, 底层使用ThistleSpi加载
插件时, 会扫描classpath, 默认根据插件的优先度排序后返回一个插件列表, 用户可以使用配置文件或启动参数排除指定的插件. 
在处理器场景时, 接口中定义一个方法, 返回处理器接收的数据类型, 底层可以根据这个方法决定处理器何时使用. 
```

* [插件加载指南](https://github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/plugin-loading.md)

<br>
<br>
<br>

# 关于日志

* 日志关键字: `ThistleSpi`
* 默认使用`System.out`输出日志
* 开源库`slate-common`提供了使用SLF4J输出日志的实现, 添加依赖即可:

```text
dependencies {
    compile 'com.github.shepherdviolet:slate-common:version'
}
```

```text
    <dependency>
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>slate-common</artifactId>
        <version>version</version>
    </dependency>
```

* 使用`slate-common`库输出日志时, SLF4J日志级别`INFO`和`ERROR`, 日志包路径`sviolet.slate.common.x.common.thistlespi`
* ThistleSpi支持自定义自身日志打印器
* [日志样例](https://github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/log-sample.md)

<br>

## 调整日志级别

* ThistleSpi自身有日志级别的概念, 这个方法是调整自身的级别, 非SLF4J的级别
* 添加启动参数(三选一), debug输出最多日志, error只输出错误日志, info为默认日志级别

> `-Dthistle.spi.loglv=error` <br>
> `-Dthistle.spi.loglv=info` <br>
> `-Dthistle.spi.loglv=debug` <br>

<br>

## 自定义日志打印器

* 请先阅读[服务加载指南](https://github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/service-loading.md)
* 以下示例仅供参考, 具体细节按[服务加载指南](https://github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/service-loading.md)文档操作

### 实现类

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

### 服务定义

* 创建文件`META-INF/thistle-spi-logger/service.properties`
* 编辑文件:

```text
sviolet.thistle.x.common.thistlespi.SpiLogger>sample-app>application=sample.spi.logger.CustomSpiLogger
```

* 其中ID`sample-app`和优先级`application`的设置请参考[服务加载指南](https://github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/service-loading.md)

<br>

## 指定应用的日志打印器

* 如果自动加载的日志打印器不是你想要的, 或者相同优先级的日志打印器发生了冲突, 我们可以使用以下方法指定用哪个
* 请先阅读[服务加载指南](https://github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/service-loading.md)

### 配置文件方式

* 创建文件`META-INF/thistle-spi-logger/service-apply.properties`
* 编辑文件:

```text
sviolet.thistle.x.common.thistlespi.SpiLogger=sample-app
```

* 其中ID`sample-app`为你想应用的日志打印器ID, 可以把日志级别开到debug, 观察日志获得ID

### 启动参数方式

* 添加启动参数

```text
-Dthistle.spi.apply.sviolet.thistle.x.common.thistlespi.SpiLogger=sample-app
```

* 其中ID`sample-app`为你想应用的日志打印器ID, 可以把日志级别开到debug, 观察日志获得ID

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

* 开启debug级别日志: 添加启动参数`-Dthistle.spi.loglv=debug`
* 如果使用SLF4J打印日志, 还需要确保包路径`sviolet.slate.common.x.common.thistlespi`日志级别在`INFO`级以上
* 运行程序, 观察日志

```text
...SlfSpiLogger : 0 ThistleSpi | -------------------------------------------------------------
...SlfSpiLogger : 0 ThistleSpi | Loading services from META-INF/thistle-spi/, DOC: github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/guide.md
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
...SlfSpiLogger : 0 ThistleSpi | Loading services from META-INF/thistle-spi/, DOC: github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/guide.md
...SlfSpiLogger : 0 ThistleSpi | !!! Exclude config file:/E:/C_Workspace/j2ee-3-test/beet-scrunchy-incubator/beet-scrunchy/out/production/resources/META-INF/thistle-spi/service.properties by -Dthistle.spi.file.exclusion
...SlfSpiLogger : 0 ThistleSpi | !!! Exclude config file:/E:/C_Workspace/j2ee-3-test/beet-scrunchy-incubator/beet-common/out/production/resources/META-INF/thistle-spi/service.properties by -Dthistle.spi.file.exclusion
...SlfSpiLogger : 0 ThistleSpi | Loading config jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.1-SNAPSHOT/slate-common-11.1-20181012.155613-8.jar!/META-INF/thistle-spi/service.properties <hash> ec8a407a18cb9646b2330dc66bc294b5
```

* 指定配置文件已被排除

<br>
<br>
<br>

# 依赖

* gradle

```gradle
//version替换为具体版本
dependencies {
    compile 'com.github.shepherdviolet:thistle:version'
}
```

* gradle(最少依赖)

```gradle
//version替换为具体版本
dependencies {
    compile ('com.github.shepherdviolet:thistle:version') {
        transitive = false
    }
}
```

* maven

```maven
    <!--version替换为具体版本-->
    <dependency>
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>thistle</artifactId>
        <version>version/version>
    </dependency>
```

* maven(最少依赖)

```maven
    <!--version替换为具体版本-->
    <dependency>
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>thistle</artifactId>
        <version>version</version>
        <exclusions>
             <exclusion>
                 <groupId>*</groupId>
                 <artifactId>*</artifactId>
             </exclusion>
        </exclusions>
    </dependency>
```
