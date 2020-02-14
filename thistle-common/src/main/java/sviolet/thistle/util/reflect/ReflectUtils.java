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
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.Set;

/**
 * 反射工具
 *
 * @author S.Violet
 */
public class ReflectUtils {

    /**
     * <p>获取一个类的父类/接口类的泛型实际类型.</p>
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
     * @param targetGenericClass 泛型定义类, 指定要获取的泛型定义在哪个类/接口
     * @return 不为空, NonNull, 类型:Class
     * @throws TargetGenericClassNotFoundException 异常情况: 给定类的父类或接口中找不到泛型定义类
     */
    public static Class[] getGenericClasses(Class clazz, Class targetGenericClass) throws TargetGenericClassNotFoundException {
        if (clazz == null) {
            throw new NullPointerException("clazz is null");
        }
        if (targetGenericClass == null) {
            throw new NullPointerException("targetGenericClass is null");
        }
        //当前类 / 当前类对于子类的泛型对象 / 指定要获取的泛型定义在哪个类/接口 / 是否转成Class
        Type[] types = getGenericTypes0(clazz, null, targetGenericClass, true);
        if (types == null) {
            throw new TargetGenericClassNotFoundException("The targetGenericClass '" + targetGenericClass.getName() +
                    "' was not found in the super classes or interfaces of given class " + clazz.getName());
        }
        Class[] classes = new Class[types.length];
        for (int i = 0 ; i < types.length ; i++) {
            classes[i] = (Class) types[i];
        }
        return classes;
    }

    /**
     * <p>获取一个类的父类/接口类的泛型实际类型.</p>
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
     * @param targetGenericClass 泛型定义类, 指定要获取的泛型定义在哪个类/接口
     * @return 不为空, NonNull, 类型:Class/ParameterizedType, 返回null表示未找到指定的声明泛型的类
     * @throws TargetGenericClassNotFoundException 异常情况: 给定类的父类或接口中找不到泛型定义类
     */
    public static Type[] getGenericTypes(Class clazz, Class targetGenericClass) throws TargetGenericClassNotFoundException {
        if (clazz == null) {
            throw new NullPointerException("clazz is null");
        }
        if (targetGenericClass == null) {
            throw new NullPointerException("targetGenericClass is null");
        }
        //当前类 / 当前类对于子类的泛型对象 / 指定要获取的泛型定义在哪个类/接口 / 是否转成Class
        Type[] types = getGenericTypes0(clazz, null, targetGenericClass, false);
        if (types == null) {
            throw new TargetGenericClassNotFoundException("The targetGenericClass '" + targetGenericClass.getName() +
                    "' was not found in the super classes or interfaces of given class " + clazz.getName());
        }
        return types;
    }
    
    private static Type[] getGenericTypes0(Class currentClass, Type currentGenericType, Class targetGenericClass, boolean preferRawClass) {
        Type[] actualTypes = null;

        //1.检查父类泛型
        Type genericSuperClass = currentClass.getGenericSuperclass();
        //genericSuperClass只可能是ParameterizedType或Class
        if (genericSuperClass instanceof ParameterizedType &&
                targetGenericClass.equals(((ParameterizedType) genericSuperClass).getRawType())) {
            //找到指定类, 且确定了泛型
            actualTypes = ((ParameterizedType) genericSuperClass).getActualTypeArguments();
        } else if (targetGenericClass.equals(genericSuperClass)){
            //找到指定类, 未确定泛型
            TypeVariable[] targetTypeParameters = targetGenericClass.getTypeParameters();
            actualTypes = new Type[targetTypeParameters.length];
            for (int i = 0 ; i < targetTypeParameters.length ; i++) {
                actualTypes[i] = Object.class;
            }
        } else if (genericSuperClass != null && !genericSuperClass.equals(Object.class)) {
            //递归
            actualTypes = getGenericTypes0(currentClass.getSuperclass(), genericSuperClass, targetGenericClass, preferRawClass);
        }

        //2.检查接口泛型
        if (actualTypes == null) {
            Type[] genericInterfaces = currentClass.getGenericInterfaces();
            if (genericInterfaces != null && genericInterfaces.length > 0) {
                for (Type genericInterface : genericInterfaces) {

                    //genericInterface只可能是ParameterizedType或Class
                    if (genericInterface instanceof ParameterizedType &&
                            targetGenericClass.equals(((ParameterizedType) genericInterface).getRawType())) {
                        //找到指定类, 且确定了泛型
                        actualTypes = ((ParameterizedType) genericInterface).getActualTypeArguments();
                        break;
                    } else if (targetGenericClass.equals(genericInterface)){
                        //找到指定类, 未确定泛型
                        TypeVariable[] targetTypeParameters = targetGenericClass.getTypeParameters();
                        actualTypes = new Type[targetTypeParameters.length];
                        for (int i = 0 ; i < targetTypeParameters.length ; i++) {
                            actualTypes[i] = Object.class;
                        }
                        break;
                    } else {
                        //递归
                        Class interface0 = null;
                        if (genericInterface instanceof Class) {
                            interface0 = (Class) genericInterface;
                        } else if (genericInterface instanceof ParameterizedType) {
                            interface0 = (Class) ((ParameterizedType) genericInterface).getRawType();
                        }
                        if (interface0 != null) {
                            actualTypes = getGenericTypes0(interface0, genericInterface, targetGenericClass, preferRawClass);
                            if (actualTypes != null) {
                                break;
                            }
                        }
                    }

                }
            }
        }

        //3.处理结果
        if (actualTypes != null) {
            for (int i = 0 ; i < actualTypes.length ; i++) {
                Type type = actualTypes[i];
                if (type instanceof TypeVariable) {
                    if (currentGenericType instanceof ParameterizedType) {
                        /*
                            如果发现有一个父类/接口的泛型没有在当前类被确定具体的类型, 就要查找当前类的泛型定义(就是public class Name <A, B, C>),
                            找到未确定的泛型对应当前类的第几个泛型定义, 然后在"当前类对于子类的泛型对象(currentGenericType)"中查找当前类定义的泛型
                            的实际类型.
                         */
                        //获取当前类的泛型定义(就是public class Name <A, B, C>)
                        TypeVariable[] currentTypeVariables = currentClass.getTypeParameters();
                        boolean matched = false;
                        if (currentTypeVariables != null) {
                            for (int offset = 0 ; offset < currentTypeVariables.length ; offset++) {
                                //泛型名称相同, 找到对应位置
                                if (((TypeVariable) type).getName().equals(currentTypeVariables[offset].getName())) {
                                    Type[] currentActualTypeArgs = ((ParameterizedType) currentGenericType).getActualTypeArguments();
                                    if (currentActualTypeArgs == null || offset >= currentActualTypeArgs.length) {
                                        //特殊: 正常情况是不可能找不到的, 会编译不通过, 这里简单的将类型赋值为Object
                                        break;
                                    }
                                    actualTypes[i] = currentActualTypeArgs[offset];
                                    matched = true;
                                    break;
                                }
                            }
                        }
                        if (!matched) {
                            //特殊: 正常情况是不可能找不到的, 会编译不通过, 这里简单的将类型赋值为Object
                            actualTypes[i] = Object.class;
                        }
                    } else {
                        //如果"当前类对于子类的泛型对象(currentGenericType)"不是ParameterizedType, 就没有办法继续查找了, 这说明子类没有确定类型, 视为Object
                        actualTypes[i] = Object.class;
                    }
                } else if (type instanceof ParameterizedType) {
                    //可以选择转成Class
                    if (preferRawClass) {
                        actualTypes[i] = ((ParameterizedType) type).getRawType();
                    }
                } else if (!(type instanceof Class)){
                    //特殊: 遇到奇怪的Type类型, 一般不会出现这种情况, 这里简单的将类型赋值为Object
                    actualTypes[i] = Object.class;
                }
            }
        }

        return actualTypes;
    }

    /**
     * 指定的泛型定义类没有在父类和接口中找到. 通俗地讲, 就是遍历了类的所有父类和接口, 都没有找到匹配第二个参数targetGenericClass
     * 的, 所以无法获取到泛型具体类型
     */
    public static class TargetGenericClassNotFoundException extends Exception {

        private static final long serialVersionUID = 8233374476663138140L;

        public TargetGenericClassNotFoundException(String message) {
            super(message);
        }

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
