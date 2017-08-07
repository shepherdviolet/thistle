package sviolet.thistle.kotlin.utilx.treebuilder.map

class MapObjectItem
internal constructor(
        val bean: HashMap<String, Any?>
){

    private var key: String? = null

    /**
     * key, required
     */
    infix fun k(key: String) : MapObjectItem {
        this.key = key
        return this
    }

    /**
     * value(add String)
     */
    infix fun v(value: Any?) {
        val k = key ?: throw IllegalArgumentException("You should invoke method \"k\" to set key before set value")
        bean.put(k, value)
    }

    /**
     * block(to build Map)
     */
    infix fun v(block: MapObjectBuilder.() -> Unit) {
        val k = key ?: throw IllegalArgumentException("You should invoke method \"k\" to set key before set value")
        val obj = MapObjectBuilder()
        obj.block()
        bean.put(k, obj.bean)
    }

}