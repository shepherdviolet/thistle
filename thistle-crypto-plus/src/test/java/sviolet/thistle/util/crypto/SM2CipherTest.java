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

import org.junit.Test;
import sviolet.thistle.util.conversion.ByteUtils;

public class SM2CipherTest {

    @Test
    public void key() throws Exception {
        SM2KeyGenerator.SM2KeyParamsPair keyPair = SM2KeyGenerator.generateKeyParamsPair();

        System.out.println(keyPair.getPrivateD());
        System.out.println(ByteUtils.bytesToHex(keyPair.getPublicASN1Encoding()));

        SM2KeyGenerator.generatePrivateKeyParams(keyPair.getPrivateD());
        SM2KeyGenerator.generatePublicKeyParamsByASN1(keyPair.getPublicASN1Encoding());

    }

}
