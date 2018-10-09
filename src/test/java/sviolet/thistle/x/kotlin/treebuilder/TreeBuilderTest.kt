package sviolet.thistle.x.kotlin.treebuilder

import org.junit.Assert
import org.junit.Test

class TreeBuilderTest {

    @Test
    fun json() {
        val json = TreeBuilder.json {
            item k "name" v "kotlin"
            item k "version" v "1.1"
            item k "identity" v {
                item k "no" v "001"
                item k "verify" v "658"
            }
            list k "features" i 1..3 v { id ->
                item v {
                    item k "id" v id
                    item k "description" v "the $id feature"
                }
            }
            list k "data" i 1..3 v { i ->
                list i 1..3 v { j ->
                    item v "$i$j"
                }
            }
        }
        Assert.assertEquals(
                "{\"name\":\"kotlin\",\"version\":\"1.1\",\"identity\":{\"no\":\"001\",\"verify\":\"658\"},\"features\":[{\"id\":\"1\",\"description\":\"the 1 feature\"},{\"id\":\"2\",\"description\":\"the 2 feature\"},{\"id\":\"3\",\"description\":\"the 3 feature\"}],\"data\":[[\"11\",\"12\",\"13\"],[\"21\",\"22\",\"23\"],[\"31\",\"32\",\"33\"]]}",
                json
        )
    }

    @Test
    fun map(){
        val map = TreeBuilder.map {
            item k "name" v "kotlin"
            item k "version" v "1.1"
            item k "identity" v {
                item k "no" v "001"
                item k "verify" v "658"
            }
            list k "features" i 1..3 v { id ->
                item v {
                    item k "id" v id
                    item k "description" v "the $id feature"
                }
            }
            list k "data" i 1..3 v { i ->
                list i 1..3 v { j ->
                    item v "$i$j"
                }
            }
        }
        Assert.assertEquals(
                "{features=[{description=the 1 feature, id=1}, {description=the 2 feature, id=2}, {description=the 3 feature, id=3}], data=[[11, 12, 13], [21, 22, 23], [31, 32, 33]], identity={no=001, verify=658}, name=kotlin, version=1.1}",
                map.toString()
        )
    }

}