package sviolet.thistle.kotlin.utilx.treebuilder.json

import com.google.gson.JsonArray

class JsonArrayList
internal constructor(
        val bean: JsonArray
){

    private var hasIterable = false
    private var iterable: Any? = null

    /**
     * iterable, optional
     */
    infix fun i(iterable: Iterable<*>): JsonArrayList {
        this.iterable = iterable
        hasIterable = true
        return this
    }

    /**
     * iterable, optional
     */
    infix fun i(array: Array<*>): JsonArrayList {
        this.iterable = array
        hasIterable = true
        return this
    }

    /**
     * block(to build JsonArray)
     */
    infix fun v(block: JsonArrayBuilder.(Any?) -> Unit) {
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
                throw IllegalArgumentException("[TJson]The \"iterable\" argument cannot be iterate")
            }
        } else {
            array.block(null)
        }
        bean.add(array.bean)
    }

}