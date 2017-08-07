package sviolet.thistle.kotlin.utilx.treebuilder.map

class MapArrayItem
internal constructor(
        val bean: ArrayList<Any?>
){

    /**
     * value(add String)
     */
    infix fun v(value: Any?) {
        bean.add(value)
    }

    /**
     * block(to build Map)
     */
    infix fun v(block: MapObjectBuilder.() -> Unit) {
        val obj = MapObjectBuilder()
        obj.block()
        bean.add(obj.bean)
    }

}