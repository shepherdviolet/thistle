/*
 * Copyright (C) 2015-2017 S.Violet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Project GitHub: https://github.com/shepherdviolet/thistle
 * Email: shepherdviolet@163.com
 */

package sviolet.thistle.util.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * 反射工具
 *
 * @author S.Violet
 */
public class ReflectUtils {

    /**
     * <p>获取一个类的父类/接口类的泛型实际类型, 返回值可能为空.</p>
     *
     * <p>例如, 有个类A, 继承了类B(可以是多层), 类B定义了一个泛型, 而类A继承类B时指定了泛型, 获取这个泛型类型的代码如下:</p>
     *
     * <pre>
     *     ReflectUtils.getGenericClass(A.class, B.class);
     * </pre>
     *
     * <p>例如, 有个类A, 实现了接口C(可以是多层), 类C定义了一个泛型, 而类A实现接口C时指定了泛型, 获取这个泛型类型的代码如下:</p>
     *
     * <pre>
     *     ReflectUtils.getGenericClass(A.class, C.class);
     * </pre>
     *
     * <p>例如, 有个父类P, 会被其他类继承, 父类P定义了一个泛型, P想要获得自己的泛型最终被指定成了什么, 代码如下:</p>
     *
     * <pre>
     *     ReflectUtils.getGenericClass(this.getClass(), P.class);
     * </pre>
     *
     * <p>在Spring中使用时, 对象有可能是个代理类, 在获取泛型前先获取代理类持有的实际类:</p>
     *
     * <pre>
     *     Class<?> rawClass = AopProxyUtils.ultimateTargetClass(bean);
     * </pre>
     *
     * @param clazz 类
     * @param targetGenericClass 指定要获取的泛型声明在哪个类/接口
     * @return Nullable, 类型:Class, 返回null表示未找到指定的声明泛型的类
     */
    public static Class[] getGenericClasses(Class clazz, Class targetGenericClass) {
        Type[] genericTypes = getGenericTypes(clazz, targetGenericClass);
        if (genericTypes == null) {
            return null;
        }
        Class[] genericClasses = new Class[genericTypes.length];
        for (int i = 0 ; i < genericTypes.length ; i++) {
            Type type = genericTypes[i];
            if (type instanceof ParameterizedType) {
                genericClasses[i] = (Class) ((ParameterizedType) type).getRawType();
            } else {
                genericClasses[i] = (Class) type;
            }
        }
        return genericClasses;
    }

    /**
     * <p>获取一个类的父类/接口类的泛型实际类型, 返回值可能为空.</p>
     *
     * <p>例如, 有个类A, 继承了类B(可以是多层), 类B定义了一个泛型, 而类A继承类B时指定了泛型, 获取这个泛型类型的代码如下:</p>
     *
     * <pre>
     *     ReflectUtils.getGenericType(A.class, B.class);
     * </pre>
     *
     * <p>例如, 有个类A, 实现了接口C(可以是多层), 类C定义了一个泛型, 而类A实现接口C时指定了泛型, 获取这个泛型类型的代码如下:</p>
     *
     * <pre>
     *     ReflectUtils.getGenericType(A.class, C.class);
     * </pre>
     *
     * <p>例如, 有个父类P, 会被其他类继承, 父类P定义了一个泛型, P想要获得自己的泛型最终被指定成了什么, 代码如下:</p>
     *
     * <pre>
     *     ReflectUtils.getGenericType(this.getClass(), P.class);
     * </pre>
     *
     * <p>在Spring中使用时, 对象有可能是个代理类, 在获取泛型前先获取代理类持有的实际类:</p>
     *
     * <pre>
     *     Class<?> rawClass = AopProxyUtils.ultimateTargetClass(bean);
     * </pre>
     *
     * @param clazz 类
     * @param targetGenericClass 指定要获取的泛型声明在哪个类/接口
     * @return Nullable, 类型:Class/ParameterizedType, 返回null表示未找到指定的声明泛型的类
     */
    public static Type[] getGenericTypes(Class clazz, Class targetGenericClass) {
        if (clazz == null) {
            throw new NullPointerException("clazz is null");
        }
        if (targetGenericClass == null) {
            throw new NullPointerException("targetGenericClass is null");
        }
        if (targetGenericClass.isInterface()) {
            return getGenericTypesOfInterface(clazz, targetGenericClass);
        } else {
            return getGenericTypesOfClass(clazz, targetGenericClass);
        }
    }

    private static Type[] getGenericTypesOfClass(Class clazz, Class targetGenericClass) {
        Type matchedGenericSuperClass = null;
        while (clazz != null) {
            Type genericSuperClass = clazz.getGenericSuperclass();
            if (genericSuperClass instanceof ParameterizedType && targetGenericClass.equals(((ParameterizedType) genericSuperClass).getRawType())) {
                matchedGenericSuperClass = genericSuperClass;
                break;
            }
            clazz = clazz.getSuperclass();
        }
        if (matchedGenericSuperClass == null) {
            return null;
        }
        return ((ParameterizedType) matchedGenericSuperClass).getActualTypeArguments();
    }

    private static Type[] getGenericTypesOfInterface(Class clazz, Class targetGenericInterface) {
        Type matchedGenericInterface = null;
        while (clazz != null) {
            Type[] genericInterfaces = clazz.getGenericInterfaces();
            if (genericInterfaces != null) {
                for (Type genericInterface : genericInterfaces) {
                    if (genericInterface instanceof ParameterizedType && targetGenericInterface.equals(((ParameterizedType) genericInterface).getRawType())) {
                        matchedGenericInterface = genericInterface;
                        break;
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        if (matchedGenericInterface == null) {
            return null;
        }
        return ((ParameterizedType) matchedGenericInterface).getActualTypeArguments();
    }

    /**
     * <p>获取方法调用者信息(类名/方法名)</p>
     *
     * <p>在A方法内调用ReflectUtils.getMethodCaller(null, null)方法时, 可以得到A方法的调用者(是谁调用了A方法). </p>
     *
     * <p>使用equalSkips参数跳过一些类: 假如A方法还会被B/C两个方法调用(封装), 我们想要知道是谁调用了A/B/C方法, 可以在A方法中
     * 调用ReflectUtils.getMethodCaller(equalSkips, null)获取调用者, equalSkips配置B/C方法所在类的类名. 示例:</p>
     *
     * <pre>
     * //推荐用HashSet, 性能较好
     * private final Set<String> equalSkips = new HashSet<>(Arrays.asList(
     *         AClass.class.getName(),
     *         BClass.class.getName()
     * ));
     *
     * public void method(){
     *     String callerClassName = Utils.getMethodCaller(equalSkips, null).getClassName();
     * }
     * </pre>
     *
     * <p>使用startsWithSkips参数跳过一些类: 假如A方法还会被B/C两个方法调用(封装), 我们想要知道是谁调用了A/B/C方法, 可以在A方法中
     * 调用ReflectUtils.getMethodCaller(null, startsWithSkips)获取调用者, startsWithSkips配置B/C方法所在类的类名的前缀. 示例:</p>
     *
     * <pre>
     * //推荐用ArrayList, 性能较好
     * private final Collection<String> startsWithSkips = Arrays.asList(
     *         "org.springframework.cglib.proxy.Proxy$ProxyImpl$$",
     *         "com.company.packagename."
     * );
     *
     * public void method(){
     *     String callerClassName = Utils.getMethodCaller(null, startsWithSkips).getClassName();
     * }
     * </pre>
     *
     * <p>另外, equalSkips和startsWithSkips参数可以同时使用. </p>
     *
     * <p>关于性能, new Throwable().getStackTrace()性能较差(和抛出一个异常的开销差不多),
     * 但微微比Thread.currentThread().getStackTrace()好一点点, 实测性能如下: <br>
     * 3.31GHz CPU 单线程 <br>
     * 线程堆栈15层时 10000次 耗时112.33ms <br>
     * 线程堆栈13层时 10000次 耗时93.37ms <br>
     * 线程堆栈 7层时 10000次 耗时62.29ms <br>
     * 线程堆栈 3层时 10000次 耗时37.60ms <br>
     * </p>
     *
     * @param equalSkips      要跳过的类(equals方式匹配), 可为空, 推荐HashSet
     * @param startsWithSkips 要跳过的类(startsWith方式匹配), 可为空, 推荐ArrayList
     * @return 调用者信息, Nullable, 若返回空则表示找不到调用者
     */
    public static MethodCaller getMethodCaller(Set<String> equalSkips, Collection<String> startsWithSkips) {
        //获取当前堆栈
        StackTraceElement[] elements = new Throwable().getStackTrace();
        //跳过头两个元素(getCaller方法和调用getCaller的方法)
        if (elements != null && elements.length > 2) {
            //遍历堆栈
            int i = 2;
            for (; i < elements.length; i++) {
                //当前元素
                StackTraceElement element = elements[i];
                //如果类名与skipEquals中的一个相同, 则跳过该元素查找下一个
                if (equalSkips != null && equalSkips.contains(element.getClassName())) {
                    continue;
                }
                //如果类名是skipStarts中的任意一个作为开头的, 则跳过该元素查找下一个
                if (startsWithSkips != null && isStartsWithSkips(startsWithSkips, element)) {
                    continue;
                }
                //如果未跳过则当前元素就是调用者
                return new MethodCaller(element, i, elements);
            }
        }
        //找不到调用者
        return null;
    }

    private static boolean isStartsWithSkips(Collection<String> startsWithSkips, StackTraceElement element) {
        String className = element.getClassName();
        for (String skipStart : startsWithSkips) {
            if (skipStart != null && className.startsWith(skipStart)) {
                return true;
            }
        }
        return false;
    }

    public static class MethodCaller {

        private String callerClass;
        private String callerMethodName;
        private String callerFileName;
        private int callerLineNumber;
        private int callerIndex;
        private StackTraceElement[] stackTraces;

        private MethodCaller(StackTraceElement caller, int callerIndex, StackTraceElement[] stackTraces){
            this.callerClass = caller.getClassName();
            this.callerMethodName = caller.getMethodName();
            this.callerFileName = caller.getFileName();
            this.callerLineNumber = caller.getLineNumber();
            this.callerIndex = callerIndex;
            this.stackTraces = stackTraces;
        }

        /**
         * 调用点所在的类, 不为空
         */
        public String getCallerClass() {
            return callerClass;
        }

        /**
         * 调用点的方法名, 不为空
         */
        public String getCallerMethodName() {
            return callerMethodName;
        }

        /**
         * 调用点所在类对应的文件, 可为空
         */
        public String getCallerFileName() {
            return callerFileName;
        }

        /**
         * 调用点所在行数
         */
        public int getCallerLineNumber() {
            return callerLineNumber;
        }

        /**
         * 调用点在线程堆栈中的位置
         */
        public int getCallerIndex() {
            return callerIndex;
        }

        /**
         * 调用线程堆栈
         */
        public StackTraceElement[] getStackTraces() {
            return stackTraces;
        }

    }

}
