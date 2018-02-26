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

package sviolet.thistle.util.crypto;

/**
 * 安全随机数工具
 */
public class SecureRandomUtils {

    public static void nextBytes(byte[] bytes){
        BaseKeyGenerator.getSecureRandom().nextBytes(bytes);
    }

    public static int nextInt(){
        return BaseKeyGenerator.getSecureRandom().nextInt();
    }

    public static int nextInt(int bound){
        return BaseKeyGenerator.getSecureRandom().nextInt(bound);
    }

    public static boolean nextBoolean(){
        return BaseKeyGenerator.getSecureRandom().nextBoolean();
    }

    public static double nextDouble(){
        return BaseKeyGenerator.getSecureRandom().nextDouble();
    }

    public static float nextFloat(){
        return BaseKeyGenerator.getSecureRandom().nextFloat();
    }

    public static double nextGaussian(){
        return BaseKeyGenerator.getSecureRandom().nextGaussian();
    }

    public static long nextLong(){
        return BaseKeyGenerator.getSecureRandom().nextLong();
    }

}
