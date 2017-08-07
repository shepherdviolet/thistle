package sviolet.thistle.kotlin.utilx.treebuilder.json

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