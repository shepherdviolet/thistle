/*
 * Copyright (C) 2015-2019 S.Violet
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

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;

public class ReflectUtilsTest {

    @Test
    public void getActualType(){
        Assert.assertEquals("[class java.lang.Integer]",
                Arrays.toString(ReflectUtils.getGenericClasses(Wrapper.class, Parent.class)));
        Assert.assertEquals("[class java.lang.String]",
                Arrays.toString(ReflectUtils.getGenericClasses(Wrapper.class, Eat.class)));
        Assert.assertEquals("[interface java.util.Map]",
                Arrays.toString(ReflectUtils.getGenericClasses(Wrapper.class, Game.class)));
    }

    public static class Wrapper extends Child {
    }

    public static class Child extends Parent<Integer> implements Game<Map<String, Object>>{
        @Override
        public void game(Map<String, Object> game) {
        }
    }

    public static class Parent <S> implements Eat<String> {
        public void sleep(S time){
        }
        @Override
        public void eat(String food) {
        }
    }

    public interface Eat<T> {
        void eat(T food);
    }

    public interface Game<T> {
        void game(T game);
    }

}
