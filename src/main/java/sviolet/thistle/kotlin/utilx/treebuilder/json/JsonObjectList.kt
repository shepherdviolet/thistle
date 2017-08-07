package sviolet.thistle.kotlin.utilx.treebuilder.json

import com.google.gson.JsonObject

class JsonObjectList
internal constructor(
        val bean: JsonObject
){

    private var key: String? = null
    private var hasIterable = false
    private var iterable: Any? = null

    /**
     * key, required
     */
    infix fun k(key: String?): JsonObjectList {
        this.key = key
        return this
    }

    /**
     * iterable, optional.
     */
    infix fun i(iterable: Iterable<*>): JsonObjectList {
        this.iterable = iterable
        hasIterable = true
        return this
    }

    /**
     * iterable, optional
     */
    infix fun i(array: Array<*>): JsonObjectList {
        this.iterable = array
        hasIterable = true
        return this
    }

    /**
     * block(to build JsonArray)
     */
    infix fun v(block: JsonArrayBuilder.(Any?) -> Unit) {
        if (key == null){
            throw IllegalArgumentException("You should invoke method \"k\" to set key before set value")
        }
        val array = JsonArrayBuilder()
        if (hasIterable) {
            val i = iterable
            if (i is Iterable<*>) {
                i.forEach {
                    array.block(it)
                }
            } else if (i is Array<*>) {
                i.forEach {
                    array.block(it)
                }
            } else {
                throw IllegalArgumentException("The \"iterable\" argument cannot be iterate")
            }
        } else {
            array.block(null)
        }
        bean.add(key, array.bean)
    }
}