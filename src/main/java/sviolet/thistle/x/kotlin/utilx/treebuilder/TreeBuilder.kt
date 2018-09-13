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

package sviolet.thistle.x.kotlin.utilx.treebuilder

import sviolet.thistle.x.kotlin.utilx.treebuilder.json.JsonObjectBuilder
import sviolet.thistle.x.kotlin.utilx.treebuilder.map.MapObjectBuilder

/**
 * Kotlin 树结构对象构造工具
 * Created by S.Violet on 2017/7/31.
 */

object TreeBuilder {

    /**
     * 构造json
     */
    fun json(block: JsonObjectBuilder.() -> Unit) : String {
        val obj = JsonObjectBuilder()
        obj.block()
        return obj.build()
    }

    /**
     * 构造Map
     */
    fun map(block: MapObjectBuilder.() -> Unit) : Map<String, Any?> {
        val obj = MapObjectBuilder()
        obj.block()
        return obj.build()
    }

}