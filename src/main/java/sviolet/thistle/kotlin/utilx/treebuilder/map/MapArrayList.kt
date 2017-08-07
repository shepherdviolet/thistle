package sviolet.thistle.kotlin.utilx.treebuilder.map

class MapArrayList
internal constructor(
        val bean: ArrayList<Any?>
){

    private var hasIterable = false
    private var iterable: Any? = null

    /**
     * iterable, optional
     */
    infix fun i(iterable: Iterable<*>): MapArrayList {
        this.iterable = iterable
        hasIterable = true
        return this
    }

    /**
     * iterable, optional
     */
    infix fun i(array: Array<*>): MapArrayList {
        this.iterable = array
        hasIterable = true
        return this
    }

    /**
     * block(to build List)
     */
    infix fun v(block: MapArrayBuilder.(Any?) -> Unit) {
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
                throw IllegalArgumentException("[TJson]The \"iterable\" argument cannot be iterate")
            }
        } else {
            array.block(null)
        }
        bean.add(array.bean)
    }

}