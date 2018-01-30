/*
 * Copyright (C) 2015-2018 S.Violet
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

import sviolet.thistle.util.conversion.BeanMethodNameFormatter;
import sviolet.thistle.util.judge.CheckUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 简易的反射取值工具
 *
 * @author S.Violet
 */
public class ReflectGetter {

    public static Object get(Object obj, KeyPath keyPath, boolean getByMethodEnabled) throws IllegalKeyPathException, TypeNotMatchException, OutOfBoundException, ReflectException, FieldNotFoundException {
        if (obj == null){
            return null;
        }
        if (keyPath == null) {
            throw new IllegalKeyPathException("keyPath is null");
        }

        Object currentObj = obj;
        KeyPath currentKeyPath = keyPath;
        int index = 0;
        //0:key 1:index1 2:index2
        int stat = 0;
        while (currentKeyPath != null) {
            int type = getObjectType(currentObj);
            if (stat == 0) {

                if (CheckUtils.isEmpty(currentKeyPath.key)){
                    if (currentKeyPath.index1 >= 0){
                        stat = 1;
                        continue;
                    }
                }

                switch (type){
                    case 0:
                        throw new TypeNotMatchException("Can't get field by key from basic type, type:" + currentObj.getClass().getName() + ". objType:" + obj.getClass().getName() + ", index:" + index + ", keyPath:" + keyPath);
                    case 1:
                        Map<?, ?> mapObj = (Map<?, ?>) currentObj;
                        currentObj = mapObj.get(currentKeyPath.key);
                        break;
                    case 2:
                    case 3:
                    case 4:
                        throw new TypeNotMatchException("Can't get field by key from List / array[] / array[][], type:" + currentObj.getClass().getName() + ". objType:" + obj.getClass().getName() + ", index:" + index + ", keyPath:" + keyPath);
                    case 9:
                        Class<?> clazz = currentObj.getClass();
                        if (getByMethodEnabled) {
                            Method method = null;
                            try {
                                //ignore private method
                                method = clazz.getMethod(BeanMethodNameFormatter.toGetterName(currentKeyPath.key));
                                if(!void.class.isAssignableFrom(method.getReturnType())){
                                    currentObj = method.invoke(currentObj);
                                    break;
                                }
                            } catch (BeanMethodNameFormatter.FormatException | NoSuchMethodException | IllegalAccessException ignore) {
                                //find field
                            } catch (Throwable t) {
                                throw new ReflectException("Error while getting field by method, method:" + method.getName() + ", type:" + currentObj.getClass().getName() + ". objType:" + obj.getClass().getName() + ", index:" + index + ", keyPath:" + keyPath, t);
                            }
                        }
                        try {
                            Field field = clazz.getDeclaredField(currentKeyPath.key);
                            if (!field.isAccessible()){
                                field.setAccessible(true);
                            }
                            currentObj = field.get(currentObj);
                        } catch (NoSuchFieldException e) {
                            throw new FieldNotFoundException("Field not found in bean, field:" + currentKeyPath.key + ", type:" + currentObj.getClass().getName() + ". objType:" + obj.getClass().getName() + ", index:" + index + ", keyPath:" + keyPath);
                        } catch (Throwable t) {
                            throw new ReflectException("Error while getting field by name, field:" + currentKeyPath.key + ", type:" + currentObj.getClass().getName() + ". objType:" + obj.getClass().getName() + ", index:" + index + ", keyPath:" + keyPath, t);
                        }
                        break;
                    default:
                        throw new TypeNotMatchException("Unexpected object type, type:" + currentObj.getClass().getName() + ". objType:" + obj.getClass().getName() + ", index:" + index + ", keyPath:" + keyPath);
                }

                if (currentObj == null){
                    return null;
                }
                if (currentKeyPath.index1 >= 0){
                    stat = 1;
                } else {
                    currentKeyPath = currentKeyPath.next;
                    index++;
                    stat = 0;
                }

            } else if (stat == 1) {

                Object temp;

                switch (type){
                    case 0:
                        throw new TypeNotMatchException("Can't get field by index from basic type, type:" + currentObj.getClass().getName() + ". objType:" + obj.getClass().getName() + ", index:" + index + ", keyPath:" + keyPath);
                    case 1:
                        throw new TypeNotMatchException("Can't get field by index from Map, type:" + currentObj.getClass().getName() + ". objType:" + obj.getClass().getName() + ", index:" + index + ", keyPath:" + keyPath);
                    case 2:
                        List<?> listObj = (List<?>) currentObj;
                        try {
                            temp = listObj.get(currentKeyPath.index1);
                        } catch (Throwable t){
                            throw new OutOfBoundException("Out of bound while getting field from List, your index1:" + currentKeyPath.index1 + ", list size:" + listObj.size() + ". objType:" + obj.getClass().getName() + ", index:" + index + ", keyPath:" + keyPath);
                        }
                        break;
                    case 3:
                        try {
                            temp = Array.get(currentObj, currentKeyPath.index1);
                        } catch (Throwable t){
                            throw new OutOfBoundException("Out of bound while getting field from array[], your index1:" + currentKeyPath.index1 + ", array length:" + Array.getLength(currentObj) + ". objType:" + obj.getClass().getName() + ", index:" + index + ", keyPath:" + keyPath);
                        }
                        break;
                    case 4:
                        try {
                            temp = Array.get(currentObj, currentKeyPath.index1);
                        } catch (Throwable t){
                            throw new OutOfBoundException("Out of bound while getting field from array[][], your index1:" + currentKeyPath.index1 + ", array length:" + Array.getLength(currentObj) + ". objType:" + obj.getClass().getName() + ", index:" + index + ", keyPath:" + keyPath);
                        }
                        if (currentKeyPath.index2 < 0){
                            throw new TypeNotMatchException("Object is array[][], but array[] is defined in your keyPath, type:" + currentObj.getClass().getName() + ". objType:" + obj.getClass().getName() + ", index:" + index + ", keyPath:" + keyPath);
                        }
                        if (temp == null){
                            return null;
                        }
                        currentObj = temp;
                        stat = 2;
                        continue;
                    case 9:
                        throw new TypeNotMatchException("Can't get field by index from bean type, type:" + currentObj.getClass().getName() + ". objType:" + obj.getClass().getName() + ", index:" + index + ", keyPath:" + keyPath);
                    default:
                        throw new TypeNotMatchException("Unexpected object type, type:" + currentObj.getClass().getName() + ". objType:" + obj.getClass().getName() + ", index:" + index + ", keyPath:" + keyPath);
                }

                if (currentKeyPath.index2 >= 0){
                    throw new TypeNotMatchException("Object is array[], but array[][] is defined in your keyPath, type:" + currentObj.getClass().getName() + ". objType:" + obj.getClass().getName() + ", index:" + index + ", keyPath:" + keyPath);
                }
                if (temp == null){
                    return null;
                }

                currentObj = temp;
                currentKeyPath = currentKeyPath.next;
                index++;
                stat = 0;

            } else {

                try {
                    currentObj = Array.get(currentObj, currentKeyPath.index2);
                } catch (Throwable t){
                    throw new OutOfBoundException("Out of bound while getting field from array[][], your index2:" + currentKeyPath.index1 + ", array length2:" + Array.getLength(currentObj) + ". objType:" + obj.getClass().getName() + ", index:" + index + ", keyPath:" + keyPath);
                }

                currentKeyPath = currentKeyPath.next;
                index++;
                stat = 0;

            }
        }
        return currentObj;
    }

    /**
     * -1: nul
     * 0: basic
     * 1: map
     * 2: list
     * 3. array[]
     * 4. array[][]
     *
     * basic:
     * integer, long, float, boolean, byte, char, double, short, byte[], char[], String
     */
    private static int getObjectType(Object obj){
        //null
        if (obj == null){
            return -1;
        }
        //map
        if (obj instanceof Map) {
            return 1;
        }
        //list
        if (obj instanceof List) {
            return 2;
        }
        //basic
        if (obj.getClass().isPrimitive() ||
                obj instanceof Integer ||
                obj instanceof Long ||
                obj instanceof Float ||
                obj instanceof Boolean ||
                obj instanceof Byte ||
                obj instanceof Character ||
                obj instanceof Double ||
                obj instanceof Short ||
                obj instanceof String) {
            return 0;
        }
        String className = obj.getClass().getName();
        //basic
        if ("[B".equals(className) ||
                "[C".equals(className) ||
                "[Ljava.lang.Byte;".equals(className) ||
                "[Ljava.lang.Character;".equals(className)) {
            return 0;
        }
        //array [][]
        if (className.startsWith("[[")){
            return 4;
        }
        //array []
        if (className.startsWith("[")){
            return 3;
        }
        //bean
        return 9;
    }

    /**
     * <p>解析键路径</p>
     *
     * <p>示例:body.infoList[3].name</p>
     *
     * <p>
     * 转移符: <br>
     * // -> / <br>
     * /. -> . <br>
     * /[ -> [ <br>
     * /] -> ] <br>
     * </p>
     *
     * @param keyPath 键路径(字符串)
     * @return 键路径
     * @throws ReflectGetter.IllegalKeyPathException 无效的键路径
     */
    public static KeyPath parseKeyPath(String keyPath) throws ReflectGetter.IllegalKeyPathException {
        //split
        List<String> elements = splitPath(keyPath);
        //result
        KeyPath firstElement = null;
        //previous
        KeyPath previousElement = null;
        //current
        KeyPath currentElement;
        for (String element : elements) {
            //record origin for log
            String elementOrigin = element;
            //new key path
            currentElement = new KeyPath();

            if (!CheckUtils.isEmpty(element)){

                /*
                    element not null
                 */

                /*
                    0: init stat
                    1: first [
                    2: first ]
                    3: second [
                    4: second ]
                 */
                int step = 0;
                int start = 0;
                boolean escape = false;
                for (int i = 0 ; i < element.length() ; i++){

                    char current = element.charAt(i);
                    if (escape) {

                        /*
                            previous is escape /
                         */

                        switch (current) {
                            case '/':
                            case '[':
                            case ']':
                                if (i < 2){
                                    element = element.substring(i);
                                } else {
                                    element = element.substring(0, i - 1) + element.substring(i);
                                }
                                i--;
                                break;
                            default:
                                throw new ReflectGetter.IllegalKeyPathException("Illegal keyPath, unexpected \"/" + current + "\", element:" + elementOrigin + ", keyPath:" + keyPath);
                        }

                        escape = false;

                    } else {

                        /*
                            normal char
                         */

                        //chars after "key[index1][index2]"
                        if (step > 4){
                            throw new ReflectGetter.IllegalKeyPathException("Illegal keyPath, unexpected \"" + current + "\", element:" + elementOrigin + ", keyPath:" + keyPath);
                        }

                        switch (current) {
                            case '/':
                                escape = true;
                                break;
                            case '[':
                                if (step == 0) {
                                    currentElement.key = element.substring(start, i);
                                    start = i + 1;
                                    step = 1;
                                } else if (step == 2) {
                                    start = i + 1;
                                    step = 3;
                                } else {
                                    throw new ReflectGetter.IllegalKeyPathException("Illegal keyPath, unexpected \"" + current + "\", element:" + elementOrigin + ", keyPath:" + keyPath);
                                }
                                break;
                            case ']':
                                if (step == 1) {
                                    try {
                                        currentElement.index1 = Integer.valueOf(element.substring(start, i));
                                    } catch (Throwable t) {
                                        throw new ReflectGetter.IllegalKeyPathException("Illegal keyPath, \"" + current + "\" can't cast to integer, element:" + elementOrigin + ", keyPath:" + keyPath, t);
                                    }
                                    step = 2;
                                } else if (step == 3) {
                                    try {
                                        currentElement.index2 = Integer.valueOf(element.substring(start, i));
                                    } catch (Throwable t) {
                                        throw new ReflectGetter.IllegalKeyPathException("Illegal keyPath, \"" + current + "\" can't cast to integer, element:" + elementOrigin + ", keyPath:" + keyPath, t);
                                    }
                                    step = 4;
                                } else {
                                    throw new ReflectGetter.IllegalKeyPathException("Illegal keyPath, unexpected \"" + current + "\", element:" + elementOrigin + ", keyPath:" + keyPath);
                                }
                                break;
                            default:
                                //chars in ][
                                if (step == 2) {
                                    throw new ReflectGetter.IllegalKeyPathException("Illegal keyPath, unexpected \"" + current + "\", element:" + elementOrigin + ", keyPath:" + keyPath);
                                }
                                break;
                        }

                    }

                }

                // no [] element
                if (step == 0){
                    currentElement.key = element;
                }

            } else {

                /*
                    element is null
                 */

                currentElement.key = "";

            }

            if (firstElement == null){
                firstElement = currentElement;
            }
            if (previousElement != null){
                previousElement.next = currentElement;
            }
            previousElement = currentElement;
        }
        if (firstElement == null){
            throw new ReflectGetter.IllegalKeyPathException("Empty keyPath, keyPath:" + keyPath);
        }
        return firstElement;
    }

    /**
     * split keyPath by .
     * @param keyPath keyPath
     * @return List<String>
     * @throws ReflectGetter.IllegalKeyPathException exception
     */
    private static List<String> splitPath(String keyPath) throws ReflectGetter.IllegalKeyPathException{
        //record origin for log
        String keyPathOrigin = keyPath;
        //result
        List<String> list = new ArrayList<>();
        if (keyPath == null){
            return list;
        }

        boolean escape = false;
        int start = 0;
        for (int i = 0 ; i < keyPath.length() ; i++){
            char current = keyPath.charAt(i);
            if (escape) {

                /*
                    previous char is escape /
                 */

                switch (current) {
                    case '.':
                        if (i < 2){
                            keyPath = keyPath.substring(i);
                        } else {
                            keyPath = keyPath.substring(0, i - 1) + keyPath.substring(i);
                        }
                        i--;
                        break;
                    case '/':
                    case '[':
                    case ']':
                        // valid char but do nothing
                        break;
                    default:
                        throw new ReflectGetter.IllegalKeyPathException("Illegal keyPath, unexpected \"/" + current + "\", index:" + i + ", keyPath:" + keyPathOrigin);
                }

                escape = false;

            } else {

                /*
                    normal char
                 */

                switch (current) {
                    case '/':
                        escape = true;
                        break;
                    case '.':
                        list.add(keyPath.substring(start, i));
                        start = i + 1;
                        break;
                    default:
                        break;
                }
            }
        }

        //remain
        if (start <= keyPath.length()){
            list.add(keyPath.substring(start, keyPath.length()));
        }
        return list;
    }

    /**
     * 键路径实例
     */
    public final static class KeyPath {

        private String key;
        private int index1 = -1;
        private int index2 = -1;
        private KeyPath next;

        private KeyPath() {
        }

        @Override
        public String toString() {
            return "<" + key + ">" +
                    (index1 > -1 ? "[" + index1 + "]" : "") +
                    (index2 > -1 ? "[" + index2 + "]" : "") +
                    (next != null ? " - " + next : "");
        }

    }

    //Exceptions///////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 类型不匹配异常
     */
    public static class TypeNotMatchException extends Exception{
        public TypeNotMatchException(String message) {
            super(message);
        }
    }

    /**
     * 数组越界异常
     */
    public static class OutOfBoundException extends Exception{
        public OutOfBoundException(String message) {
            super(message);
        }
    }

    /**
     * Bean对象中成员变量或Getter方法不存在
     */
    public static class FieldNotFoundException extends Exception {
        public FieldNotFoundException(String message) {
            super(message);
        }
    }

    /**
     * Bean对象通过反射取值时异常
     */
    public static class ReflectException extends Exception {
        public ReflectException(String message) {
            super(message);
        }
        public ReflectException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * 非法键路径异常
     */
    public static class IllegalKeyPathException extends Exception {
        public IllegalKeyPathException(String message) {
            super(message);
        }
        public IllegalKeyPathException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
