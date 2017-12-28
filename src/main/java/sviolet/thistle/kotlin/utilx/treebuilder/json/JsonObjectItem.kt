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

package sviolet.thistle.kotlin.utilx.treebuilder.json

import com.google.gson.JsonObject

class JsonObjectItem
internal constructor(
        val bean: JsonObject
){

    private var key: String? = null

    /**
     * key, required
     */
    infix fun k(key: String) : JsonObjectItem {
        this.key = key
        return this
    }

    /**
     * value(add String)
     */
    infix fun v(value: Any?) {
        if (key == null){
            throw IllegalArgumentException("You should invoke method \"k\" to set key before set value")
        }
        bean.addProperty(key, value?.toString() ?: "")
    }

    /**
     * block(to build JsonObject)
     */
    infix fun v(block: JsonObjectBuilder.() -> Unit) {
        if (key == null){
            throw IllegalArgumentException("You should invoke method \"k\" to set key before set value")
        }
        val obj = JsonObjectBuilder()
        obj.block()
        bean.add(key, obj.bean)
    }

}