package sviolet.thistle.kotlin.utilx.treebuilder.map

class MapObjectList
internal constructor(
        val bean: HashMap<String, Any?>
){

    private var key: String? = null
    private var hasIterable = false
    private var iterable: Any? = null

    /**
     * key, required
     */
    infix fun k(key: String?): MapObjectList {
        this.key = key
        return this
    }

    /**
     * iterable, optional.
     */
    infix fun i(iterable: Iterable<*>): MapObjectList {
        this.iterable = iterable
        hasIterable = true
        return this
    }

    /**
     * iterable, optional
     */
    infix fun i(array: Array<*>): MapObjectList {
        this.iterable = array
        hasIterable = true
        return this
    }

    /**
     * block(to build List)
     */
    infix fun v(block: MapArrayBuilder.(Any?) -> Unit) {
        val k = key ?: throw IllegalArgumentException("You should invoke method \"k\" to set key before set value")
        val array = MapArrayBuilder()
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
        bean.put(k, array.bean)
    }
}