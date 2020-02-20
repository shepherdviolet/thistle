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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bean info utils
 *
 * @author S.Violet
 */
public class BeanInfoUtils {

    private static final Map<Class<?>, Object> CACHE = new ConcurrentHashMap<>(128);

    /**
     * Get property info of Java Bean, With cache
     *
     * @param beanClass Bean class
     * @throws IntrospectionException Inspect error
     */
    public static Map<String, PropertyInfo> getPropertyInfos(Class<?> beanClass) throws IntrospectionException {
        return getPropertyInfos(beanClass, true);
    }

    /**
     * Get property info of Java Bean
     *
     * @param beanClass Bean class
     * @param cacheEnabled Is cache enabled
     * @throws IntrospectionException Inspect error
     */
    public static Map<String, PropertyInfo> getPropertyInfos(Class<?> beanClass, boolean cacheEnabled) throws IntrospectionException {
        if (beanClass == null) {
            throw new NullPointerException("beanClass is null");
        }

        if (!cacheEnabled) {
            return getPropertyInfos0(beanClass);
        }

        Object propertyInfos = CACHE.get(beanClass);
        if (propertyInfos == null) {
            try {
                propertyInfos = getPropertyInfos0(beanClass);
                propertyInfos = Collections.unmodifiableMap((Map<?, ?>) propertyInfos);
            } catch (IntrospectionException e) {
                propertyInfos = e;
            }
            CACHE.put(beanClass, propertyInfos);
        }

        if (propertyInfos instanceof IntrospectionException) {
            throw (IntrospectionException) propertyInfos;
        }

        return (Map<String, PropertyInfo>) propertyInfos;
    }

    private static Map<String, PropertyInfo> getPropertyInfos0(Class<?> beanClass) throws IntrospectionException {
        BeanInfo beanInfo = Introspector.getBeanInfo(beanClass, Object.class);
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        Map<String, PropertyInfo> propertyInfos = new HashMap<>(propertyDescriptors.length << 1);
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            Class<?> propertyClass = propertyDescriptor.getPropertyType();
            Type propertyType;
            Method readMethod = propertyDescriptor.getReadMethod();
            Method writeMethod = propertyDescriptor.getWriteMethod();
            Class<?> declaringClass;
            if (propertyClass == null) {
                propertyClass = Object.class;
            }
            if (readMethod != null) {
                propertyType = readMethod.getGenericReturnType();
                declaringClass = readMethod.getDeclaringClass();
            } else {
                // writeMethod must exist if readMethod is null
                Type[] parameterTypes = writeMethod.getGenericParameterTypes();
                // IndexedPropertyDescriptor has 2 parameters, see Introspector
                propertyType = parameterTypes[parameterTypes.length - 1];
                declaringClass = writeMethod.getDeclaringClass();
            }
            if (propertyType instanceof TypeVariable && ((TypeVariable<?>) propertyType).getGenericDeclaration() instanceof Class) {
                Type actualType = null;
                try {
                    // try to find generic type in class
                    actualType = GenericClassUtils.getActualTypes(beanClass, declaringClass)
                            .get(((TypeVariable<?>) propertyType).getName());
                } catch (GenericClassUtils.TargetGenericClassNotFoundException ignore) {
                }
                if (!isAcceptedType(actualType)) {
                    actualType = propertyClass;
                }
                propertyType = actualType;
            } else if (!isAcceptedType(propertyType)) {
                propertyType = propertyClass;
            }
            String propertyName = propertyDescriptor.getName();
            if (propertyName != null) {
                propertyName = propertyName.intern();
            }
            propertyInfos.put(propertyName, new PropertyInfo(
                    propertyName,
                    propertyClass,
                    propertyType,
                    readMethod,
                    writeMethod
            ));
        }
        return propertyInfos;
    }

    private static boolean isAcceptedType(Type type) {
        return type instanceof Class || type instanceof ParameterizedType;
    }

    public static class PropertyInfo {

        private String propertyName;
        private Class<?> propertyClass;
        private Type propertyType;
        private Method readMethod;
        private Method writeMethod;

        private PropertyInfo(String propertyName, Class<?> propertyClass, Type propertyType, Method readMethod, Method writeMethod) {
            this.propertyName = propertyName;
            this.propertyClass = propertyClass;
            this.propertyType = propertyType;
            this.readMethod = readMethod;
            this.writeMethod = writeMethod;
        }

        /**
         * Bean property name, Not null
         */
        public String getPropertyName() {
            return propertyName;
        }

        /**
         * Class of property, Not null
         */
        public Class<?> getPropertyClass() {
            return propertyClass;
        }

        /**
         * Generic type of property, Not null
         */
        public Type getPropertyType() {
            return propertyType;
        }

        /**
         * Read method, Nullable, But one of the two methods must not be null
         */
        public Method getReadMethod() {
            return readMethod;
        }

        /**
         * Write method, Nullable, But one of the two methods must not be null
         */
        public Method getWriteMethod() {
            return writeMethod;
        }

        @Override
        public String toString() {
            return "PropertyInfo{" +
                    "propertyName='" + propertyName + '\'' +
                    ", propertyClass=" + propertyClass +
                    ", propertyType=" + propertyType +
                    ", readMethod=" + readMethod +
                    ", writeMethod=" + writeMethod +
                    '}';
        }
    }

}
