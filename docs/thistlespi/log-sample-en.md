# ThistleSpi Log Sample

### -Dthistle.spi.loglv=info

* `... is trying to load ...`: Show which class method is using the ThistleSpi to load services and plugins, which class loader to use
* `Loading services / plugins from`: Show which path to load now
* `Service Applied` / `Plugin applied`: Show those services and plugins will be applied (just show they will be applied, but have not been loaded yet)
* `Service ... loaded` / `Plugin ... loaded`: Shows that those services and plugins have been successfully loaded

```text
... 0 ThistleSpi ServiceLoader | Service sviolet.thistle.x.common.thistlespi.SpiLogger (sviolet.thistle.x.common.thistlespi.SlfSpiLogger) loaded successfully
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

* At the debug level, it prints the loading process of the inner-logger first, prints by System.out. And then prints the loading process of the user services/plugins, prints by inner-logger.
* `... is trying to load ...`: Show which class method is using the ThistleSpi to load services and plugins, which class loader to use
* `Loading services / plugins from`: Show which path to load now, also shows the file path
* `Service Applied` / `Plugin applied`: Show those services and plugins will be applied (just show they will be applied, but have not been loaded yet), also shows the URL / Reason / All Configurations(+ means effective, - means not effective)
* `Service ... loaded` / `Plugin ... loaded`: Shows that those services and plugins have been successfully loaded

```text
... 0 ThistleSpi | -------------------------------------------------------------
... 0 ThistleSpi | Loading services from META-INF/thistle-spi-logger/, DOC: github.com/shepherdviolet/thistle/blob/master/docs/thistlespi/guide.md
... 0 ThistleSpi | Loading config jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.1-SNAPSHOT/slate-common-11.1-20181012.155613-8.jar!/META-INF/thistle-spi-logger/service.properties <hash> b05f449d4be99370fd62b6f4ccf21cba
... 0 ThistleSpi | -------------------------------------------------------------
... 0 ThistleSpi | Service Applied:
... 0 ThistleSpi |   type: sviolet.thistle.x.common.thistlespi.SpiLogger
... 0 ThistleSpi |   implement: sviolet.thistle.x.common.thistlespi.SlfSpiLogger
... 0 ThistleSpi |   url: jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.1-SNAPSHOT/slate-common-11.1-20181012.155613-8.jar!/META-INF/thistle-spi-logger/service.properties
... 0 ThistleSpi |   reason: Applied by level (application > platform > library > default)
... 0 ThistleSpi | All Configurations:
... 0 ThistleSpi |   + Service{id=slate-common, level=DEFAULT, impl=sviolet.thistle.x.common.thistlespi.SlfSpiLogger, arg=null, url=jar:file:/C:/m2repository/repository/com/github/shepherdviolet/slate-common/11.1-SNAPSHOT/slate-common-11.1-20181012.155613-8.jar!/META-INF/thistle-spi-logger/service.properties}
... 0 ThistleSpi | -------------------------------------------------------------
... 0 ThistleSpi ServiceLoader | Service sviolet.thistle.x.common.thistlespi.SpiLogger (sviolet.thistle.x.common.thistlespi.SlfSpiLogger) loaded successfully
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
