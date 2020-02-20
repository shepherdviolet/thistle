/*
 * Copyright (C) 2015-2020 S.Violet
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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 泛型工具
 *
 * @author S.Violet
 */
public class GenericClassUtils {

    /**
     * <p>获取一个类的父类/接口类的泛型实际类型. 无缓存. </p>
     *
     * <p>例如, 有个类A, 继承了类B(可以是多层), 类B定义了一个泛型, 而类A继承类B时指定了泛型, 获取这个泛型类型的代码如下:</p>
     *
     * <pre>
     *     GenericClassUtils.getActualClasses(A.class, B.class);
     * </pre>
     *
     * <p>例如, 有个类A, 实现了接口C(可以是多层), 类C定义了一个泛型, 而类A实现接口C时指定了泛型, 获取这个泛型类型的代码如下:</p>
     *
     * <pre>
     *     GenericClassUtils.getActualClasses(A.class, C.class);
     * </pre>
     *
     * <p>例如, 有个父类P, 会被其他类继承, 父类P定义了一个泛型, P想要获得自己的泛型最终被指定成了什么, 代码如下:</p>
     *
     * <pre>
     *     GenericClassUtils.getActualClasses(this.getClass(), P.class);
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
    public static Map<String, Class<?>> getActualClasses(Class<?> clazz, Class<?> targetGenericClass) throws TargetGenericClassNotFoundException {
        if (clazz == null) {
            throw new NullPointerException("clazz is null");
        }
        if (targetGenericClass == null) {
            throw new NullPointerException("targetGenericClass is null");
        }
        if (targetGenericClass.getTypeParameters().length <= 0) {
            return new LinkedHashMap<>();
        }
        //当前类 / 当前类对于子类的泛型对象 / 指定要获取的泛型定义在哪个类/接口 / 是否转成Class
        Type[] actualTypes = getActualTypes0(clazz, null, targetGenericClass, true);
        if (actualTypes == null) {
            throw new TargetGenericClassNotFoundException("The targetGenericClass '" + targetGenericClass.getName() +
                    "' was not found in the super classes or interfaces of given class " + clazz.getName());
        }
        Map<String, Class<?>> actualTypeMap = new LinkedHashMap<>();
        TypeVariable<?>[] typeVariables = targetGenericClass.getTypeParameters();
        for (int i = 0 ; i < actualTypes.length ; i++) {
            actualTypeMap.put(typeVariables[i].getName(), (Class<?>) actualTypes[i]);
        }
        return actualTypeMap;
    }

    /**
     * <p>获取一个类的父类/接口类的泛型实际类型. 无缓存. </p>
     *
     * <p>例如, 有个类A, 继承了类B(可以是多层), 类B定义了一个泛型, 而类A继承类B时指定了泛型, 获取这个泛型类型的代码如下:</p>
     *
     * <pre>
     *     GenericClassUtils.getActualTypes(A.class, B.class);
     * </pre>
     *
     * <p>例如, 有个类A, 实现了接口C(可以是多层), 类C定义了一个泛型, 而类A实现接口C时指定了泛型, 获取这个泛型类型的代码如下:</p>
     *
     * <pre>
     *     GenericClassUtils.getActualTypes(A.class, C.class);
     * </pre>
     *
     * <p>例如, 有个父类P, 会被其他类继承, 父类P定义了一个泛型, P想要获得自己的泛型最终被指定成了什么, 代码如下:</p>
     *
     * <pre>
     *     GenericClassUtils.getActualTypes(this.getClass(), P.class);
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
    public static Map<String, Type> getActualTypes(Class<?> clazz, Class<?> targetGenericClass) throws TargetGenericClassNotFoundException {
        if (clazz == null) {
            throw new NullPointerException("clazz is null");
        }
        if (targetGenericClass == null) {
            throw new NullPointerException("targetGenericClass is null");
        }
        if (targetGenericClass.getTypeParameters().length <= 0) {
            return new LinkedHashMap<>();
        }
        //当前类 / 当前类对于子类的泛型对象 / 指定要获取的泛型定义在哪个类/接口 / 是否转成Class
        Type[] actualTypes = getActualTypes0(clazz, null, targetGenericClass, false);
        if (actualTypes == null) {
            throw new TargetGenericClassNotFoundException("The targetGenericClass '" + targetGenericClass.getName() +
                    "' was not found in the super classes or interfaces of given class " + clazz.getName());
        }
        Map<String, Type> actualTypeMap = new LinkedHashMap<>();
        TypeVariable<?>[] typeVariables = targetGenericClass.getTypeParameters();
        for (int i = 0 ; i < actualTypes.length ; i++) {
            actualTypeMap.put(typeVariables[i].getName(), actualTypes[i]);
        }
        return actualTypeMap;
    }

    private static Type[] getActualTypes0(Class<?> currentClass, Type currentGenericType, Class<?> targetGenericClass, boolean preferRawClass) {
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
            TypeVariable<?>[] targetTypeParameters = targetGenericClass.getTypeParameters();
            actualTypes = new Type[targetTypeParameters.length];
            for (int i = 0 ; i < targetTypeParameters.length ; i++) {
                actualTypes[i] = Object.class;
            }
        } else if (genericSuperClass != null && !genericSuperClass.equals(Object.class)) {
            //递归
            actualTypes = getActualTypes0(currentClass.getSuperclass(), genericSuperClass, targetGenericClass, preferRawClass);
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
                        TypeVariable<?>[] targetTypeParameters = targetGenericClass.getTypeParameters();
                        actualTypes = new Type[targetTypeParameters.length];
                        for (int i = 0 ; i < targetTypeParameters.length ; i++) {
                            actualTypes[i] = Object.class;
                        }
                        break;
                    } else {
                        //递归
                        Class<?> interface0 = null;
                        if (genericInterface instanceof Class) {
                            interface0 = (Class<?>) genericInterface;
                        } else if (genericInterface instanceof ParameterizedType) {
                            interface0 = (Class<?>) ((ParameterizedType) genericInterface).getRawType();
                        }
                        if (interface0 != null) {
                            actualTypes = getActualTypes0(interface0, genericInterface, targetGenericClass, preferRawClass);
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
                        TypeVariable<?>[] currentTypeVariables = currentClass.getTypeParameters();
                        boolean matched = false;
                        if (currentTypeVariables != null) {
                            for (int offset = 0 ; offset < currentTypeVariables.length ; offset++) {
                                //泛型名称相同, 找到对应位置
                                if (((TypeVariable<?>) type).getName().equals(currentTypeVariables[offset].getName())) {
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

    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Type转Class.
     *
     * null -> null
     * Class -> input
     * ParameterizedType -> input.getRawType()
     * Else -> Object.class
     *
     * @param type Type
     * @return Class
     */
    public static Class<?> typeToRawClass(Type type) {
        if (type == null) {
            return null;
        }
        if (type instanceof Class) {
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        }
        return Object.class;
    }

}
