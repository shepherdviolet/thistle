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

package sviolet.thistle.util.crypto.base;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * 提供验证所需的证书颁发者
 *
 * @param <ParameterType> IssuerProvider接收的参数类型, 可选
 * @see SimpleIssuerResolver
 * @see RootIssuerProvider
 */
public interface IssuerProvider<ParameterType> {

    /**
     * 根据DN信息查找颁发者的证书
     *
     * @param dn DN信息
     * @param issuerProviderParameter IssuerProvider接收的参数, 可选
     * @return 颁发者的证书
     */
    X509Certificate findIssuer(String dn, ParameterType issuerProviderParameter) throws CertificateException;

}
