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

import sviolet.thistle.util.judge.CheckUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 简易的反射取值工具
 *
 * @author S.Violet
 */
public class ReflectGetter {

    public static KeyPath parseKeyPath(String keyPath) throws ReflectGetter.IllegalKeyPathException {
        List<String> elements = splitPath(keyPath);
        KeyPath firstElement = null;
        KeyPath previousElement = null;
        KeyPath currentElement;
        for (String element : elements) {
            String elementOrigin = element;
            currentElement = new KeyPath();
            if (!CheckUtils.isEmpty(element)){

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

                if (step == 0){
                    currentElement.key = element;
                }

            } else {
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
        return firstElement;
    }

    private static List<String> splitPath(String keyPath) throws ReflectGetter.IllegalKeyPathException{
        String keyPathOrigin = keyPath;
        List<String> list = new ArrayList<>();
        if (keyPath == null){
            return list;
        }
        boolean escape = false;
        int start = 0;
        for (int i = 0 ; i < keyPath.length() ; i++){
            char current = keyPath.charAt(i);
            if (escape) {
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
        if (start <= keyPath.length()){
            list.add(keyPath.substring(start, keyPath.length()));
        }
        return list;
    }


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

    public static class TypeNotMatchException extends Exception{
        public TypeNotMatchException(String message) {
            super(message);
        }
    }

    public static class IllegalKeyPathException extends Exception {
        public IllegalKeyPathException(String message) {
            super(message);
        }
        public IllegalKeyPathException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
