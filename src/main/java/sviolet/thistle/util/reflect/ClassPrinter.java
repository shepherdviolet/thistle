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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import sviolet.thistle.util.common.CheckUtils;

/**
 * <p>将一个类或一个对象的数据/构造函数/方法打印出来</p>
 *
 * Created by S.Violet on 2016/6/13.
 */
public class ClassPrinter {

    private static final String PRINTER_TITLE = "============================ClassPrinter============================\n";
    private static final String NULL_CLASS = "============================ClassPrinter============================\nClass:null";
    private static final String NULL_OBJECT = "============================ClassPrinter============================\nObject:null";

    /**
     * 打印class
     * @param clazz class
     * @param traversals true:遍历父类
     */
    public static String print(Class<?> clazz, boolean traversals) throws IllegalAccessException {
        return print(clazz, traversals, true, true, true);
    }

    /**
     * 打印class
     * @param clazz class
     * @param traversals true:遍历父类
     * @param printFields 输出fields
     * @param printConstructors 输出构造器
     * @param printMethods 输出方法
     */
    public static String print(Class<?> clazz, boolean traversals, boolean printFields, boolean printConstructors, boolean printMethods) throws IllegalAccessException {
        if (clazz == null){
            return NULL_CLASS;
        }
        Class<?> current = clazz;
        StringBuilder stringBuilder = new StringBuilder(PRINTER_TITLE);
        print(current, null, stringBuilder, printFields, printConstructors, printMethods);
        while(traversals && (current = current.getSuperclass()) != null){
            print(current, null, stringBuilder, printFields, printConstructors, printMethods);
        }
        return stringBuilder.toString();
    }

    /**
     * 打印对象
     * @param obj 对象
     * @param traversals true:遍历父类
     */
    public static String print(Object obj, boolean traversals) throws IllegalAccessException {
        return print(obj, traversals, true, true, true);
    }

    /**
     * 打印对象
     * @param obj 对象
     * @param traversals true:遍历父类
     * @param printFields 输出fields
     * @param printConstructors 输出构造器
     * @param printMethods 输出方法
     */
    public static String print(Object obj, boolean traversals, boolean printFields, boolean printConstructors, boolean printMethods) throws IllegalAccessException {
        if (obj == null){
            return NULL_OBJECT;
        }
        Class<?> current = obj.getClass();
        StringBuilder stringBuilder = new StringBuilder(PRINTER_TITLE);
        print(current, obj, stringBuilder, printFields, printConstructors, printMethods);
        while(traversals && (current = current.getSuperclass()) != null){
            print(current, obj, stringBuilder, printFields, printConstructors, printMethods);
        }
        return stringBuilder.toString();
    }

    private static void print(Class<?> clazz, Object obj, StringBuilder stringBuilder, boolean printFields, boolean printConstructors, boolean printMethods) throws IllegalAccessException {
        printClassInfo(clazz, stringBuilder);
        if (printFields) {
            printClassFields(clazz, obj, stringBuilder);
        }
        if (printConstructors) {
            printClassConstructors(clazz, stringBuilder);
        }
        if (printMethods) {
            printClassMethods(clazz, stringBuilder);
        }
    }

    private static void printClassMethods(Class<?> clazz, StringBuilder stringBuilder) {
        stringBuilder.append("\n--------------Methods--------------");
        Method[] methods = ReflectCache.getDeclaredMethods(clazz);
        for (Method method : methods) {
            String name = method.getName();
            if (name.contains("$")){
                continue;
            }
            stringBuilder.append("\n");
            printModifiers(method.getModifiers(), stringBuilder);
            stringBuilder.append(method.getReturnType().getName());
            stringBuilder.append(" ");
            stringBuilder.append(name);
            stringBuilder.append("(");
            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes != null) {
                for (int i = 0 ; i < paramTypes.length ; i++){
                    if (i != 0){
                        stringBuilder.append(", ");
                    }
                    stringBuilder.append(paramTypes[i].getName());
                }
            }
            stringBuilder.append(")");
        }
    }

    private static void printClassConstructors(Class<?> clazz, StringBuilder stringBuilder) {
        stringBuilder.append("\n--------------Constructors--------------");
        Constructor<?>[] constructors = ReflectCache.getDeclaredConstructors(clazz);
        for (Constructor<?> constructor : constructors) {
            stringBuilder.append("\n");
            printModifiers(constructor.getModifiers(), stringBuilder);
            stringBuilder.append(clazz.getSimpleName());
            stringBuilder.append("(");
            Class<?>[] paramTypes = constructor.getParameterTypes();
            if (paramTypes != null) {
                for (int j = 0 ; j < paramTypes.length ; j++){
                    if (j != 0){
                        stringBuilder.append(", ");
                    }
                    stringBuilder.append(paramTypes[j].getName());
                }
            }
            stringBuilder.append(")");
        }
    }

    private static void printClassFields(Class<?> clazz, Object obj, StringBuilder stringBuilder) throws IllegalAccessException {
        stringBuilder.append("\n--------------Fields--------------");
        Field[] fields = ReflectCache.getDeclaredFields(clazz);
        for (Field field : fields) {
            int modifiers = field.getModifiers();
            String name = field.getName();
            if (name.contains("$")){
                continue;
            }
            field.setAccessible(true);
            stringBuilder.append("\n");
            printModifiers(modifiers, stringBuilder);
            stringBuilder.append(field.getType().getName());
            stringBuilder.append(" ");
            stringBuilder.append(name);
            if (obj != null || CheckUtils.isFlagMatch(modifiers, Modifier.STATIC)) {
                stringBuilder.append(" = ");
                stringBuilder.append(field.get(obj));
            }
        }
    }

    private static void printClassInfo(Class<?> clazz, StringBuilder stringBuilder) {
        stringBuilder.append("\n++++++++++++++++++++++++Class++++++++++++++++++++++++\n");
        printModifiers(clazz.getModifiers(), stringBuilder);
        stringBuilder.append(clazz.getName());
    }

    private static void printModifiers(int modifiers, StringBuilder stringBuilder){
        if (CheckUtils.isFlagMatch(modifiers, Modifier.PUBLIC)){
            stringBuilder.append("public ");
        }
        if (CheckUtils.isFlagMatch(modifiers, Modifier.PRIVATE)){
            stringBuilder.append("private ");
        }
        if (CheckUtils.isFlagMatch(modifiers, Modifier.PROTECTED)){
            stringBuilder.append("protected ");
        }
        if (CheckUtils.isFlagMatch(modifiers, Modifier.STATIC)){
            stringBuilder.append("static ");
        }
        if (CheckUtils.isFlagMatch(modifiers, Modifier.FINAL)){
            stringBuilder.append("final ");
        }
        if (CheckUtils.isFlagMatch(modifiers, Modifier.SYNCHRONIZED)){
            stringBuilder.append("synchronized ");
        }
        if (CheckUtils.isFlagMatch(modifiers, Modifier.VOLATILE)){
            stringBuilder.append("volatile ");
        }
        if (CheckUtils.isFlagMatch(modifiers, Modifier.NATIVE)){
            stringBuilder.append("native ");
        }
        if (CheckUtils.isFlagMatch(modifiers, Modifier.INTERFACE)){
            stringBuilder.append("interface ");
        }
        if (CheckUtils.isFlagMatch(modifiers, Modifier.ABSTRACT)){
            stringBuilder.append("abstract ");
        }
        if (CheckUtils.isFlagMatch(modifiers, Modifier.STRICT)){
            stringBuilder.append("strict ");
        }
    }

}
