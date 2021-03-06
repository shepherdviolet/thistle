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

import sviolet.thistle.util.concurrent.ConcurrentUtils

/**
 * Map extensions
 *
 * Created by S.Violet on 2017/5/24.
 */

/**
 * Get snap shot of map, for concurrent use
 */
fun <K, V> Map<K, V>?.getSnapShot() : Map<K, V>?{
    return ConcurrentUtils.getSnapShot(this)
}