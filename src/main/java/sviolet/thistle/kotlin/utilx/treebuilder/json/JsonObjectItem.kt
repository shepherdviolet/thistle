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