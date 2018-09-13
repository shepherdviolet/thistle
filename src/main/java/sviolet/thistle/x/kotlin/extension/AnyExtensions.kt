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

package sviolet.thistle.x.kotlin.extension

/**
 * Any extensions
 *
 * Created by S.Violet on 2017/5/23.
 */

/*********************************************************************************
 * Java Class
 */

/**
 * Get java class.
 * Can be called with a null receiver, in which case it returns null.
 */
fun <T: Any> T?.getJClass() : Class<T>?{
    return this?.javaClass
}

/**
 * Get java class name.
 * Can be called with a null receiver, in which case it returns "null".
 */
fun Any?.getJClassName() : String{
    return this.getJClass()?.name ?: "null"
}

/**
 * Get java class simple name (without package).
 * Can be called with a null receiver, in which case it returns "null".
 */
fun Any?.getJClassSimpleName() : String{
    return this.getJClass()?.simpleName ?: "null"
}

/*********************************************************************************
 * DEMO
 */

/**
 * define function of BigDecimal("0.1") ADD BigDecimal("0.2")
 */
//infix fun BigDecimal?.ADD(d: BigDecimal?) : BigDecimal?{
//    if (d == null){
//        return null
//    }
//    return this?.add(d)
//}

/**
 * define function of BigDecimal("0.1") + BigDecimal("0.2")
 */
//operator fun BigDecimal?.plus(d: BigDecimal?) : BigDecimal?{
//    if (d == null){
//        return null
//    }
//    return this?.add(d)
//}