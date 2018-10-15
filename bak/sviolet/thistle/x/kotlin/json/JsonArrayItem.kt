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

package sviolet.thistle.x.kotlin.treebuilder.json

import com.google.gson.JsonArray

class JsonArrayItem
internal constructor(
        val bean: JsonArray
){

    /**
     * value(add String)
     */
    infix fun v(value: Any?) {
        bean.add(value?.toString() ?: "")
    }

    /**
     * block(to build JsonObject)
     */
    infix fun v(block: JsonObjectBuilder.() -> Unit) {
        val obj = JsonObjectBuilder()
        obj.block()
        bean.add(obj.bean)
    }

}