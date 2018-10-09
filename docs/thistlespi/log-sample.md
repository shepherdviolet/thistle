# ThistleSpi 日志样例

* 在使用`slate-common`库的`SlfSpiLogger`输出日志时, SLF4J日志级别`INFO`, 日志包路径`sviolet.slate.common.x.common.thistlespi`
* ThistleSpi可以改变自身日志打印器的实现, 详见[ThistleSpi文档的`Log Config`章节](https://github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/guide.md)

# -Dthistle.spi.loglv=info

* `... is trying to load ...`: 哪个类的方法在利用ThistleSpi加载服务和插件, 使用哪个类加载器
* `Loading services / plugins from`: 加载了哪个类路径下的配置文件
* `Service Applied` / `Plugin applied`: 那些服务和插件会被应用(仅仅是说明它们会被应用, 但还没有被加载)
* `Service ... loaded` / `Plugin ... loaded`: 成功加载了那些服务和插件

```text
SlfSpiLogger : 1 ThistleSpi | sviolet.slate.common.x.monitor.txtimer.TxTimer#<clinit> is trying to load services or plugins. With classloader jdk.internal.loader.ClassLoaders$AppClassLoader
SlfSpiLogger : 1 ThistleSpi | -------------------------------------------------------------
SlfSpiLogger : 1 ThistleSpi | Loading services from META-INF/thistle-spi/, DOC: https://github.com/shepherdviolet/thistle
SlfSpiLogger : 1 ThistleSpi | -------------------------------------------------------------
SlfSpiLogger : 1 ThistleSpi | Service Applied:
SlfSpiLogger : 1 ThistleSpi |   type: sviolet.slate.common.x.monitor.txtimer.TxTimerProvider
SlfSpiLogger : 1 ThistleSpi |   implement: sviolet.slate.common.x.monitor.txtimer.def.DefaultTxTimerProvider
SlfSpiLogger : 1 ThistleSpi | -------------------------------------------------------------
SlfSpiLogger : 1 ThistleSpi | Service Applied:
SlfSpiLogger : 1 ThistleSpi |   type: beet.scrunchy.core.BeanNameResolver
SlfSpiLogger : 1 ThistleSpi |   implement: beet.scrunchy.core.DefaultBeanNameResolver
SlfSpiLogger : 1 ThistleSpi | -------------------------------------------------------------
SlfSpiLogger : 1 ThistleSpi | Service Applied:
SlfSpiLogger : 1 ThistleSpi |   type: beet.common.util.trace.TraceProvider
SlfSpiLogger : 1 ThistleSpi |   implement: beet.common.util.trace.DefaultTraceProvider
SlfSpiLogger : 1 ThistleSpi | -------------------------------------------------------------
SlfSpiLogger : 1 ThistleSpi | Loading plugins from META-INF/thistle-spi/, DOC: https://github.com/shepherdviolet/thistle
SlfSpiLogger : 1 ThistleSpi | -------------------------------------------------------------
SlfSpiLogger : 1 ThistleSpi ServiceLoader | Service sviolet.slate.common.x.monitor.txtimer.TxTimerProvider (sviolet.slate.common.x.monitor.txtimer.def.DefaultTxTimerProvider) loaded successfully
```

# -Dthistle.spi.loglv=debug

* debug级别下, 日志先打印ThistleSpi加载自身日志打印器的过程, 使用System.out打印, 然后再打印加载用户服务的过程
* `... is trying to load ...`: 哪个类的方法在利用ThistleSpi加载服务和插件, 使用哪个类加载器
* `Loading services / plugins from`: 加载了哪个类路径下的配置文件, 还会输出具体加载了那些配置文件
* `Service Applied` / `Plugin applied`: 那些服务和插件会被应用(仅仅是说明它们会被应用, 但还没有被加载), 还会输出url(配置路径), reason(应用原因), All Configurations(该服务/插件的所有备选配置, +开头表示生效, -开头表示未生效)
* `Service ... loaded` / `Plugin ... loaded`: 成功加载了那些服务和插件

```text
1 ThistleSpi | -------------------------------------------------------------
1 ThistleSpi | Loading services from META-INF/thistle-spi-logger/, DOC: https://github.com/shepherdviolet/thistle
1 ThistleSpi | Loading jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/10.2-SNAPSHOT/slate-common-10.2-20180930.081144-1.jar!/META-INF/thistle-spi-logger/service.properties
1 ThistleSpi | -------------------------------------------------------------
1 ThistleSpi | Service Applied:
1 ThistleSpi |   type: sviolet.thistle.x.common.thistlespi.SpiLogger
1 ThistleSpi |   implement: sviolet.slate.common.x.common.thistlespi.SlfSpiLogger
1 ThistleSpi |   url: jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/10.2-SNAPSHOT/slate-common-10.2-20180930.081144-1.jar!/META-INF/thistle-spi-logger/service.properties
1 ThistleSpi |   reason: Applied by level (application > platform > library)
1 ThistleSpi | All Configurations:
1 ThistleSpi |   + Service{id=slate-common, level=LIBRARY, impl=sviolet.slate.common.x.common.thistlespi.SlfSpiLogger, url=jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/10.2-SNAPSHOT/slate-common-10.2-20180930.081144-1.jar!/META-INF/thistle-spi-logger/service.properties}
1 ThistleSpi | -------------------------------------------------------------
1 ThistleSpi ServiceLoader | Service sviolet.thistle.x.common.thistlespi.SpiLogger (sviolet.slate.common.x.common.thistlespi.SlfSpiLogger) loaded successfully
SlfSpiLogger: 1 ThistleSpi | sviolet.slate.common.x.monitor.txtimer.TxTimer#<clinit> is trying to load services or plugins. With classloader jdk.internal.loader.ClassLoaders$AppClassLoader
SlfSpiLogger: 1 ThistleSpi | -------------------------------------------------------------
SlfSpiLogger: 1 ThistleSpi | Loading services from META-INF/thistle-spi/, DOC: https://github.com/shepherdviolet/thistle
SlfSpiLogger: 1 ThistleSpi | Loading file:/E:/C_Workspace/j2ee-3-test/beet-scrunchy-incubator/beet-scrunchy/out/production/resources/META-INF/thistle-spi/service.properties
SlfSpiLogger: 1 ThistleSpi | Loading file:/E:/C_Workspace/j2ee-3-test/beet-scrunchy-incubator/beet-common/out/production/resources/META-INF/thistle-spi/service.properties
SlfSpiLogger: 1 ThistleSpi | Loading jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/10.2-SNAPSHOT/slate-common-10.2-20180930.081144-1.jar!/META-INF/thistle-spi/service.properties
SlfSpiLogger: 1 ThistleSpi | -------------------------------------------------------------
SlfSpiLogger: 1 ThistleSpi | Service Applied:
SlfSpiLogger: 1 ThistleSpi |   type: sviolet.slate.common.x.monitor.txtimer.TxTimerProvider
SlfSpiLogger: 1 ThistleSpi |   implement: sviolet.slate.common.x.monitor.txtimer.def.DefaultTxTimerProvider
SlfSpiLogger: 1 ThistleSpi |   url: jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/10.2-SNAPSHOT/slate-common-10.2-20180930.081144-1.jar!/META-INF/thistle-spi/service.properties
SlfSpiLogger: 1 ThistleSpi |   reason: Applied by level (application > platform > library)
SlfSpiLogger: 1 ThistleSpi | All Configurations:
SlfSpiLogger: 1 ThistleSpi |   + Service{id=slate-common, level=LIBRARY, impl=sviolet.slate.common.x.monitor.txtimer.def.DefaultTxTimerProvider, url=jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/10.2-SNAPSHOT/slate-common-10.2-20180930.081144-1.jar!/META-INF/thistle-spi/service.properties}
SlfSpiLogger: 1 ThistleSpi | -------------------------------------------------------------
SlfSpiLogger: 1 ThistleSpi | Service Applied:
SlfSpiLogger: 1 ThistleSpi |   type: beet.scrunchy.core.BeanNameResolver
SlfSpiLogger: 1 ThistleSpi |   implement: beet.scrunchy.core.DefaultBeanNameResolver
SlfSpiLogger: 1 ThistleSpi |   url: file:/E:/C_Workspace/j2ee-3-test/beet-scrunchy-incubator/beet-scrunchy/out/production/resources/META-INF/thistle-spi/service.properties
SlfSpiLogger: 1 ThistleSpi |   reason: Applied by level (application > platform > library)
SlfSpiLogger: 1 ThistleSpi | All Configurations:
SlfSpiLogger: 1 ThistleSpi |   + Service{id=beet-root-scrunchies, level=LIBRARY, impl=beet.scrunchy.core.DefaultBeanNameResolver, url=file:/E:/C_Workspace/j2ee-3-test/beet-scrunchy-incubator/beet-scrunchy/out/production/resources/META-INF/thistle-spi/service.properties}
SlfSpiLogger: 1 ThistleSpi | -------------------------------------------------------------
SlfSpiLogger: 1 ThistleSpi | Service Applied:
SlfSpiLogger: 1 ThistleSpi |   type: beet.common.util.trace.TraceProvider
SlfSpiLogger: 1 ThistleSpi |   implement: beet.common.util.trace.DefaultTraceProvider
SlfSpiLogger: 1 ThistleSpi |   url: file:/E:/C_Workspace/j2ee-3-test/beet-scrunchy-incubator/beet-common/out/production/resources/META-INF/thistle-spi/service.properties
SlfSpiLogger: 1 ThistleSpi |   reason: Applied by level (application > platform > library)
SlfSpiLogger: 1 ThistleSpi | All Configurations:
SlfSpiLogger: 1 ThistleSpi |   + Service{id=beet-root-common, level=LIBRARY, impl=beet.common.util.trace.DefaultTraceProvider, url=file:/E:/C_Workspace/j2ee-3-test/beet-scrunchy-incubator/beet-common/out/production/resources/META-INF/thistle-spi/service.properties}
SlfSpiLogger: 1 ThistleSpi | -------------------------------------------------------------
SlfSpiLogger: 1 ThistleSpi | Loading plugins from META-INF/thistle-spi/, DOC: https://github.com/shepherdviolet/thistle
SlfSpiLogger: 1 ThistleSpi | No META-INF/thistle-spi/plugin.properties found in classpath
SlfSpiLogger: 1 ThistleSpi | -------------------------------------------------------------
SlfSpiLogger: 1 ThistleSpi ServiceLoader | Service sviolet.slate.common.x.monitor.txtimer.TxTimerProvider (sviolet.slate.common.x.monitor.txtimer.def.DefaultTxTimerProvider) loaded successfully
```
