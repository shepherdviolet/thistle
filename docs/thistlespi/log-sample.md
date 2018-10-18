# ThistleSpi 日志样例

### -Dthistle.spi.loglv=info

* `... is trying to load ...`: 哪个类的方法在利用ThistleSpi加载服务和插件, 使用哪个类加载器
* `Loading services / plugins from`: 加载了哪个类路径下的配置文件
* `Service Applied` / `Plugin applied`: 那些服务和插件会被应用(仅仅是说明它们会被应用, 但还没有被加载)
* `Service ... loaded` / `Plugin ... loaded`: 成功加载了那些服务和插件

```text
... 0 ThistleSpi ServiceLoader | Service sviolet.thistle.x.common.thistlespi.SpiLogger (sviolet.slate.common.x.common.thistlespi.SlfSpiLogger) loaded successfully
...SlfSpiLogger : 0 ThistleSpi | beet.scrunchy.proxy.ServiceProxyInstantiator#<init> is trying to load services or plugins. With classloader jdk.internal.loader.ClassLoaders$AppClassLoader
...SlfSpiLogger : 0 ThistleSpi | -------------------------------------------------------------
...SlfSpiLogger : 0 ThistleSpi | Loading services from META-INF/thistle-spi/, DOC: github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/guide.md
...SlfSpiLogger : 0 ThistleSpi | -------------------------------------------------------------
...SlfSpiLogger : 0 ThistleSpi | Service Applied:
...SlfSpiLogger : 0 ThistleSpi |   type: sviolet.slate.common.x.monitor.txtimer.TxTimerProvider
...SlfSpiLogger : 0 ThistleSpi |   implement: sviolet.slate.common.x.monitor.txtimer.def.DefaultTxTimerProvider
...SlfSpiLogger : 0 ThistleSpi | -------------------------------------------------------------
...SlfSpiLogger : 0 ThistleSpi | Service Applied:
...SlfSpiLogger : 0 ThistleSpi |   type: beet.scrunchy.proxy.BeanNameResolver
...SlfSpiLogger : 0 ThistleSpi |   implement: beet.scrunchy.proxy.DefaultBeanNameResolver
...SlfSpiLogger : 0 ThistleSpi | -------------------------------------------------------------
...SlfSpiLogger : 0 ThistleSpi | Service Applied:
...SlfSpiLogger : 0 ThistleSpi |   type: beet.common.trace.TraceProvider
...SlfSpiLogger : 0 ThistleSpi |   implement: beet.common.trace.DefaultTraceProvider
...SlfSpiLogger : 0 ThistleSpi | -------------------------------------------------------------
...SlfSpiLogger : 0 ThistleSpi | Service Applied:
...SlfSpiLogger : 0 ThistleSpi |   type: sviolet.slate.common.x.conversion.beanutil.BeanConverter
...SlfSpiLogger : 0 ThistleSpi |   implement: sviolet.slate.common.x.conversion.beanutil.DefaultBeanConverter(logEnabled)
...SlfSpiLogger : 0 ThistleSpi | -------------------------------------------------------------
...SlfSpiLogger : 0 ThistleSpi | Loading plugins from META-INF/thistle-spi/, DOC: github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/guide.md
...SlfSpiLogger : 0 ThistleSpi | -------------------------------------------------------------
...SlfSpiLogger : 0 ThistleSpi | Plugin Applied:
...SlfSpiLogger : 0 ThistleSpi |   type: beet.common.dubbo.filter.ProviderExtFilter
...SlfSpiLogger : 0 ThistleSpi |   implements:
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=-1000, impl=beet.common.dubbo.filter.def.ProviderTraceIdExtFilter}
...SlfSpiLogger : 0 ThistleSpi | -------------------------------------------------------------
...SlfSpiLogger : 0 ThistleSpi | Plugin Applied:
...SlfSpiLogger : 0 ThistleSpi |   type: sviolet.slate.common.x.conversion.beanutil.PropMapper
...SlfSpiLogger : 0 ThistleSpi |   implements:
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=101001, impl=sviolet.slate.common.x.conversion.beanutil.safe.num.SBUMapperAllNumber2String}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=101002, impl=sviolet.slate.common.x.conversion.beanutil.safe.num.SBUMapperAllNumber2BigDecimal}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=101003, impl=sviolet.slate.common.x.conversion.beanutil.safe.num.SBUMapperAllInteger2BigInteger}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=101004, impl=sviolet.slate.common.x.conversion.beanutil.safe.num.SBUMapperLowlevelNum2Double}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=101005, impl=sviolet.slate.common.x.conversion.beanutil.safe.num.SBUMapperLowlevelNum2Float}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=101006, impl=sviolet.slate.common.x.conversion.beanutil.safe.num.SBUMapperLowlevelNum2Long}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=101007, impl=sviolet.slate.common.x.conversion.beanutil.safe.num.SBUMapperLowlevelNum2Integer}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=102001, impl=sviolet.slate.common.x.conversion.beanutil.safe.date.SBUMapperUtilDate2String(yyyy-MM-dd HH:mm:ss.SSS)}
...SlfSpiLogger : 0 ThistleSpi | -------------------------------------------------------------
...SlfSpiLogger : 0 ThistleSpi | Plugin Applied:
...SlfSpiLogger : 0 ThistleSpi |   type: beet.common.dubbo.filter.ConsumerExtFilter
...SlfSpiLogger : 0 ThistleSpi |   implements:
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=1000, impl=beet.common.dubbo.filter.def.ConsumerTraceIdExtFilter}
...SlfSpiLogger : 0 ThistleSpi | -------------------------------------------------------------
...SlfSpiLogger : 0 ThistleSpi ServiceLoader | Service beet.scrunchy.proxy.BeanNameResolver (beet.scrunchy.proxy.DefaultBeanNameResolver) loaded successfully
```

### -Dthistle.spi.loglv=debug

* debug级别下, 日志先打印ThistleSpi加载自身日志打印器的过程, 使用System.out打印, 然后再打印加载用户服务的过程
* `... is trying to load ...`: 哪个类的方法在利用ThistleSpi加载服务和插件, 使用哪个类加载器
* `Loading services / plugins from`: 加载了哪个类路径下的配置文件, 还会输出具体加载了那些配置文件
* `Service Applied` / `Plugin applied`: 那些服务和插件会被应用(仅仅是说明它们会被应用, 但还没有被加载), 还会输出url(配置路径), reason(应用原因), All Configurations(该服务/插件的所有备选配置, +开头表示生效, -开头表示未生效)
* `Service ... loaded` / `Plugin ... loaded`: 成功加载了那些服务和插件

```text
... 0 ThistleSpi | -------------------------------------------------------------
... 0 ThistleSpi | Loading services from META-INF/thistle-spi-logger/, DOC: github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/guide.md
... 0 ThistleSpi | Loading config jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.1-SNAPSHOT/slate-common-11.1-20181012.155613-8.jar!/META-INF/thistle-spi-logger/service.properties <hash> b05f449d4be99370fd62b6f4ccf21cba
... 0 ThistleSpi | -------------------------------------------------------------
... 0 ThistleSpi | Service Applied:
... 0 ThistleSpi |   type: sviolet.thistle.x.common.thistlespi.SpiLogger
... 0 ThistleSpi |   implement: sviolet.slate.common.x.common.thistlespi.SlfSpiLogger
... 0 ThistleSpi |   url: jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.1-SNAPSHOT/slate-common-11.1-20181012.155613-8.jar!/META-INF/thistle-spi-logger/service.properties
... 0 ThistleSpi |   reason: Applied by level (application > platform > library > default)
... 0 ThistleSpi | All Configurations:
... 0 ThistleSpi |   + Service{id=slate-common, level=DEFAULT, impl=sviolet.slate.common.x.common.thistlespi.SlfSpiLogger, arg=null, url=jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.1-SNAPSHOT/slate-common-11.1-20181012.155613-8.jar!/META-INF/thistle-spi-logger/service.properties}
... 0 ThistleSpi | -------------------------------------------------------------
... 0 ThistleSpi ServiceLoader | Service sviolet.thistle.x.common.thistlespi.SpiLogger (sviolet.slate.common.x.common.thistlespi.SlfSpiLogger) loaded successfully
...SlfSpiLogger : 0 ThistleSpi | beet.scrunchy.proxy.ServiceProxyInstantiator#<init> is trying to load services or plugins. With classloader jdk.internal.loader.ClassLoaders$AppClassLoader
...SlfSpiLogger : 0 ThistleSpi | -------------------------------------------------------------
...SlfSpiLogger : 0 ThistleSpi | Loading services from META-INF/thistle-spi/, DOC: github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/guide.md
...SlfSpiLogger : 0 ThistleSpi | Loading config file:/E:/C_Workspace/j2ee-3-test/beet-scrunchy-incubator/beet-scrunchy/out/production/resources/META-INF/thistle-spi/service.properties <hash> 3cf41de0f0d4bdc24970c7b834e2b5c7
...SlfSpiLogger : 0 ThistleSpi | Loading config file:/E:/C_Workspace/j2ee-3-test/beet-scrunchy-incubator/beet-common/out/production/resources/META-INF/thistle-spi/service.properties <hash> 8e3d1cbd3f828ba2a7822581416e97e0
...SlfSpiLogger : 0 ThistleSpi | Loading config jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.1-SNAPSHOT/slate-common-11.1-20181012.155613-8.jar!/META-INF/thistle-spi/service.properties <hash> ec8a407a18cb9646b2330dc66bc294b5
...SlfSpiLogger : 0 ThistleSpi | -------------------------------------------------------------
...SlfSpiLogger : 0 ThistleSpi | Service Applied:
...SlfSpiLogger : 0 ThistleSpi |   type: sviolet.slate.common.x.monitor.txtimer.TxTimerProvider
...SlfSpiLogger : 0 ThistleSpi |   implement: sviolet.slate.common.x.monitor.txtimer.def.DefaultTxTimerProvider
...SlfSpiLogger : 0 ThistleSpi |   url: jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.1-SNAPSHOT/slate-common-11.1-20181012.155613-8.jar!/META-INF/thistle-spi/service.properties
...SlfSpiLogger : 0 ThistleSpi |   reason: Applied by level (application > platform > library > default)
...SlfSpiLogger : 0 ThistleSpi | All Configurations:
...SlfSpiLogger : 0 ThistleSpi |   + Service{id=slate-common, level=DEFAULT, impl=sviolet.slate.common.x.monitor.txtimer.def.DefaultTxTimerProvider, arg=null, url=jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.1-SNAPSHOT/slate-common-11.1-20181012.155613-8.jar!/META-INF/thistle-spi/service.properties}
...SlfSpiLogger : 0 ThistleSpi | -------------------------------------------------------------
...SlfSpiLogger : 0 ThistleSpi | Service Applied:
...SlfSpiLogger : 0 ThistleSpi |   type: beet.scrunchy.proxy.BeanNameResolver
...SlfSpiLogger : 0 ThistleSpi |   implement: beet.scrunchy.proxy.DefaultBeanNameResolver
...SlfSpiLogger : 0 ThistleSpi |   url: file:/E:/C_Workspace/j2ee-3-test/beet-scrunchy-incubator/beet-scrunchy/out/production/resources/META-INF/thistle-spi/service.properties
...SlfSpiLogger : 0 ThistleSpi |   reason: Applied by level (application > platform > library > default)
...SlfSpiLogger : 0 ThistleSpi | All Configurations:
...SlfSpiLogger : 0 ThistleSpi |   + Service{id=beet-root-scrunchies, level=LIBRARY, impl=beet.scrunchy.proxy.DefaultBeanNameResolver, arg=null, url=file:/E:/C_Workspace/j2ee-3-test/beet-scrunchy-incubator/beet-scrunchy/out/production/resources/META-INF/thistle-spi/service.properties}
...SlfSpiLogger : 0 ThistleSpi | -------------------------------------------------------------
...SlfSpiLogger : 0 ThistleSpi | Service Applied:
...SlfSpiLogger : 0 ThistleSpi |   type: beet.common.trace.TraceProvider
...SlfSpiLogger : 0 ThistleSpi |   implement: beet.common.trace.DefaultTraceProvider
...SlfSpiLogger : 0 ThistleSpi |   url: file:/E:/C_Workspace/j2ee-3-test/beet-scrunchy-incubator/beet-common/out/production/resources/META-INF/thistle-spi/service.properties
...SlfSpiLogger : 0 ThistleSpi |   reason: Applied by level (application > platform > library > default)
...SlfSpiLogger : 0 ThistleSpi | All Configurations:
...SlfSpiLogger : 0 ThistleSpi |   + Service{id=beet-root-common, level=LIBRARY, impl=beet.common.trace.DefaultTraceProvider, arg=null, url=file:/E:/C_Workspace/j2ee-3-test/beet-scrunchy-incubator/beet-common/out/production/resources/META-INF/thistle-spi/service.properties}
...SlfSpiLogger : 0 ThistleSpi | -------------------------------------------------------------
...SlfSpiLogger : 0 ThistleSpi | Service Applied:
...SlfSpiLogger : 0 ThistleSpi |   type: sviolet.slate.common.x.conversion.beanutil.BeanConverter
...SlfSpiLogger : 0 ThistleSpi |   implement: sviolet.slate.common.x.conversion.beanutil.DefaultBeanConverter(logEnabled)
...SlfSpiLogger : 0 ThistleSpi |   url: jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.1-SNAPSHOT/slate-common-11.1-20181012.155613-8.jar!/META-INF/thistle-spi/service.properties
...SlfSpiLogger : 0 ThistleSpi |   reason: Applied by level (application > platform > library > default)
...SlfSpiLogger : 0 ThistleSpi | All Configurations:
...SlfSpiLogger : 0 ThistleSpi |   + Service{id=slate-common, level=DEFAULT, impl=sviolet.slate.common.x.conversion.beanutil.DefaultBeanConverter, arg=logEnabled, url=jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.1-SNAPSHOT/slate-common-11.1-20181012.155613-8.jar!/META-INF/thistle-spi/service.properties}
...SlfSpiLogger : 0 ThistleSpi | -------------------------------------------------------------
...SlfSpiLogger : 0 ThistleSpi | Loading plugins from META-INF/thistle-spi/, DOC: github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/guide.md
...SlfSpiLogger : 0 ThistleSpi | Loading config file:/E:/C_Workspace/j2ee-3-test/beet-scrunchy-incubator/beet-common/out/production/resources/META-INF/thistle-spi/plugin.properties <hash> f8efa1a54f6a5c9c02fe525aa88be71f
...SlfSpiLogger : 0 ThistleSpi | Loading config jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.1-SNAPSHOT/slate-common-11.1-20181012.155613-8.jar!/META-INF/thistle-spi/plugin.properties <hash> ddd8f2253fb4cb403ba69be7c28481a7
...SlfSpiLogger : 0 ThistleSpi | -------------------------------------------------------------
...SlfSpiLogger : 0 ThistleSpi | Plugin Applied:
...SlfSpiLogger : 0 ThistleSpi |   type: beet.common.dubbo.filter.ProviderExtFilter
...SlfSpiLogger : 0 ThistleSpi |   implements:
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=-1000, impl=beet.common.dubbo.filter.def.ProviderTraceIdExtFilter}
...SlfSpiLogger : 0 ThistleSpi | All Configurations:
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=-1000, impl=beet.common.dubbo.filter.def.ProviderTraceIdExtFilter, url=file:/E:/C_Workspace/j2ee-3-test/beet-scrunchy-incubator/beet-common/out/production/resources/META-INF/thistle-spi/plugin.properties}
...SlfSpiLogger : 0 ThistleSpi | -------------------------------------------------------------
...SlfSpiLogger : 0 ThistleSpi | Plugin Applied:
...SlfSpiLogger : 0 ThistleSpi |   type: sviolet.slate.common.x.conversion.beanutil.PropMapper
...SlfSpiLogger : 0 ThistleSpi |   implements:
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=101001, impl=sviolet.slate.common.x.conversion.beanutil.safe.num.SBUMapperAllNumber2String}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=101002, impl=sviolet.slate.common.x.conversion.beanutil.safe.num.SBUMapperAllNumber2BigDecimal}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=101003, impl=sviolet.slate.common.x.conversion.beanutil.safe.num.SBUMapperAllInteger2BigInteger}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=101004, impl=sviolet.slate.common.x.conversion.beanutil.safe.num.SBUMapperLowlevelNum2Double}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=101005, impl=sviolet.slate.common.x.conversion.beanutil.safe.num.SBUMapperLowlevelNum2Float}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=101006, impl=sviolet.slate.common.x.conversion.beanutil.safe.num.SBUMapperLowlevelNum2Long}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=101007, impl=sviolet.slate.common.x.conversion.beanutil.safe.num.SBUMapperLowlevelNum2Integer}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=102001, impl=sviolet.slate.common.x.conversion.beanutil.safe.date.SBUMapperUtilDate2String(yyyy-MM-dd HH:mm:ss.SSS)}
...SlfSpiLogger : 0 ThistleSpi | All Configurations:
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=101006, impl=sviolet.slate.common.x.conversion.beanutil.safe.num.SBUMapperLowlevelNum2Long, url=jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.1-SNAPSHOT/slate-common-11.1-20181012.155613-8.jar!/META-INF/thistle-spi/plugin.properties}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=101005, impl=sviolet.slate.common.x.conversion.beanutil.safe.num.SBUMapperLowlevelNum2Float, url=jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.1-SNAPSHOT/slate-common-11.1-20181012.155613-8.jar!/META-INF/thistle-spi/plugin.properties}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=102001, impl=sviolet.slate.common.x.conversion.beanutil.safe.date.SBUMapperUtilDate2String(yyyy-MM-dd HH:mm:ss.SSS), url=jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.1-SNAPSHOT/slate-common-11.1-20181012.155613-8.jar!/META-INF/thistle-spi/plugin.properties}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=101004, impl=sviolet.slate.common.x.conversion.beanutil.safe.num.SBUMapperLowlevelNum2Double, url=jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.1-SNAPSHOT/slate-common-11.1-20181012.155613-8.jar!/META-INF/thistle-spi/plugin.properties}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=101003, impl=sviolet.slate.common.x.conversion.beanutil.safe.num.SBUMapperAllInteger2BigInteger, url=jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.1-SNAPSHOT/slate-common-11.1-20181012.155613-8.jar!/META-INF/thistle-spi/plugin.properties}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=101002, impl=sviolet.slate.common.x.conversion.beanutil.safe.num.SBUMapperAllNumber2BigDecimal, url=jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.1-SNAPSHOT/slate-common-11.1-20181012.155613-8.jar!/META-INF/thistle-spi/plugin.properties}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=101001, impl=sviolet.slate.common.x.conversion.beanutil.safe.num.SBUMapperAllNumber2String, url=jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.1-SNAPSHOT/slate-common-11.1-20181012.155613-8.jar!/META-INF/thistle-spi/plugin.properties}
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=101007, impl=sviolet.slate.common.x.conversion.beanutil.safe.num.SBUMapperLowlevelNum2Integer, url=jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.1-SNAPSHOT/slate-common-11.1-20181012.155613-8.jar!/META-INF/thistle-spi/plugin.properties}
...SlfSpiLogger : 0 ThistleSpi | -------------------------------------------------------------
...SlfSpiLogger : 0 ThistleSpi | Plugin Applied:
...SlfSpiLogger : 0 ThistleSpi |   type: beet.common.dubbo.filter.ConsumerExtFilter
...SlfSpiLogger : 0 ThistleSpi |   implements:
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=1000, impl=beet.common.dubbo.filter.def.ConsumerTraceIdExtFilter}
...SlfSpiLogger : 0 ThistleSpi | All Configurations:
...SlfSpiLogger : 0 ThistleSpi |   + Plugin{priority=1000, impl=beet.common.dubbo.filter.def.ConsumerTraceIdExtFilter, url=file:/E:/C_Workspace/j2ee-3-test/beet-scrunchy-incubator/beet-common/out/production/resources/META-INF/thistle-spi/plugin.properties}
...SlfSpiLogger : 0 ThistleSpi | -------------------------------------------------------------
...SlfSpiLogger : 0 ThistleSpi ServiceLoader | Service beet.scrunchy.proxy.BeanNameResolver (beet.scrunchy.proxy.DefaultBeanNameResolver) loaded successfully
```
