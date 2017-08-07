package sviolet.thistle.kotlin.utilx.treebuilder

import org.junit.Assert
import org.junit.Test

class TreeBuilderTest {

    @Test
    fun json() {
        val json = TreeBuilder.json {
            item k "name" v "kotlin"
            item k "version" v "1.1"
            list k "features" i 1..3 v { id ->
                item v {
                    item k "id" v id
                    item k "description" v "the $id feature"
                }
            }
        }
        Assert.assertEquals(
                "{\"name\":\"kotlin\",\"version\":\"1.1\",\"features\":[{\"id\":\"1\",\"description\":\"the 1 feature\"},{\"id\":\"2\",\"description\":\"the 2 feature\"},{\"id\":\"3\",\"description\":\"the 3 feature\"}]}",
                json
        )
    }

}