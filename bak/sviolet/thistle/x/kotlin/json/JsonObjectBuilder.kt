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

import com.google.gson.JsonObject

/**
 * Kotlin json 工具
 *
 * Created by S.Violet on 2017/7/31.
 */
class JsonObjectBuilder
internal constructor() {

    internal val bean = JsonObject()

    /**
     * Build string item or JsonObject item
     */
    val item: JsonObjectItem
        get() = JsonObjectItem(bean)

    /**
     * Build JsonArray item
     */
    val list: JsonObjectList
        get() = JsonObjectList(bean)

    fun build(): String {
        return bean.toString()
    }

}