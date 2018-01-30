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

package sviolet.thistle.reflect;

import static org.junit.Assert.*;
import org.junit.Test;
import sviolet.thistle.util.reflect.ReflectGetter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReflectGetterTest {

    /**
     * stress testing
     */
    public static void main(String[] args) throws ReflectGetter.IllegalKeyPathException, ReflectGetter.TypeNotMatchException, ReflectGetter.FieldNotFoundException, ReflectGetter.OutOfBoundException, ReflectGetter.ReflectException {
        //parse
        long time = System.currentTimeMillis();
        ReflectGetter.KeyPath keyPath = null;
        for (int i = 0 ; i < 1000000 ; i++){
            keyPath = ReflectGetter.parseKeyPath("objectMap.map.list[1].methodBean.methodString");
        }
        System.out.println("parse: " + 1000000d / (double)(System.currentTimeMillis() - time) + " times/ms");
        //get
        Bean bean = buildTestBean();
        time = System.currentTimeMillis();
        for (int i = 0 ; i < 100000 ; i++){
            ReflectGetter.get(bean, keyPath, true);
        }
        System.out.println("get: " + 100000d / (double)(System.currentTimeMillis() - time) + " times/ms");
    }

    @Test
    public void getCommon() throws ReflectGetter.IllegalKeyPathException, ReflectGetter.TypeNotMatchException, ReflectGetter.FieldNotFoundException, ReflectGetter.OutOfBoundException, ReflectGetter.ReflectException {
        Bean bean = buildTestBean();
        assertEquals(
                "hello",
                ReflectGetter.get(bean, ReflectGetter.parseKeyPath("objectMap.name"), true)
        );
        assertEquals(
                null,
                ReflectGetter.get(bean, ReflectGetter.parseKeyPath("objectMap.undefined"), true)
        );
        assertEquals(
                596,
                ReflectGetter.get(bean, ReflectGetter.parseKeyPath("objectMap.obj.intValue"), true)
        );
        assertEquals(
                495L,
                ReflectGetter.get(bean, ReflectGetter.parseKeyPath("objectMap.map.list[1].longValue"), true)
        );
        assertArrayEquals(
                new byte[]{51, 52, 53},
                (byte[]) ReflectGetter.get(bean, ReflectGetter.parseKeyPath("objectMap.lo"), true)
        );
        assertArrayEquals(
                new String[]{"a", "b", "c"},
                (String[]) ReflectGetter.get(bean, ReflectGetter.parseKeyPath("objectMap.ssss"), true)
        );

        String s = ReflectGetter.<String>get(bean, "bean", true);
        System.out.println(s);

//        assertEquals(
//                "",
//                ReflectGetter.get(bean, ReflectGetter.parseKeyPath(""), true)
//        );
//        assertEquals(
//                "",
//                ReflectGetter.get(bean, ReflectGetter.parseKeyPath(""), true)
//        );
//        assertEquals(
//                "",
//                ReflectGetter.get(bean, ReflectGetter.parseKeyPath(""), true)
//        );
    }

    private static Bean buildTestBean() {
        Bean bean = new Bean();

        Map<String, Object> map = new HashMap<>();
        List<Object> list = new ArrayList<>();
        map.put("int", 9965);
        map.put("string", "woshishui");
        map.put("list", list);
        list.add(5453988);
        list.add(new Bean());

        bean.objectMap = new HashMap<>();
        bean.objectMap.put("name", "hello");
        bean.objectMap.put("value", "world");
        bean.objectMap.put("obj", new Bean());
        bean.objectMap.put("map", map);
        bean.objectMap.put("lo", new byte[]{51, 52, 53});
        bean.objectMap.put("ssss", new String[]{"a", "b", "c"});

        bean.objectList = new ArrayList<>();
        bean.objectList.add("st");
        bean.objectList.add(new Bean());
        bean.objectList.add(map);
        bean.objectList.add(new byte[]{53, 54, 55});
        bean.objectList.add(new String[]{"aa", "bb", "cc"});

        bean.strings = new String[]{"xx", "xxx", "xxxx"};
        bean.stringss = new String[][]{{"gg", "ggg"}, {"dd", "ddd"}};
        bean.stringsss = new String[][][]{{{"gg", "ggg"}, {"qq", "qqq"}}, {{"dd", "ddd"}, {"uu", "uuu"}}};

        bean.bean = new Bean();
        bean.bean.stringValue = "modified";

        bean.beanss = new Bean[][]{{new Bean("11"), new Bean("12")}, {new Bean("21"), new Bean("22")}};

        return bean;
    }

    private static class Bean {

        public int intValue = 596;
        public long longValue = 495;
        private float floatValue = 1.698f;
        public boolean booleanValue = true;
        public byte byteValue = 62;
        private char charValue = 35;
        public double doubleValue = 5.9659554d;
        private short shortValue = 5;
        public byte[] bytesValue = new byte[]{1, 2, 3, 4};
        public char[] charsValue = new char[]{11, 12, 13, 14};
        private String stringValue = "origin";

        private Map<String, Object> objectMap;

        public List<Object> objectList;

        public String[] strings;
        public String[][] stringss;
        public String[][][] stringsss;

        private Bean bean;
        public Bean[][] beanss;

        public Bean() {
        }

        public Bean(String stringValue) {
            this.stringValue = stringValue;
        }

        public void getMethodVoid(){

        }

        public String getMethodString(){
            return "hello";
        }

        public Bean getMethodBean(){
            return new Bean();
        }

    }

}
