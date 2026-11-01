package de.mm20.launcher2.data.customattrs

import org.junit.Assert
import org.junit.Test

class FirstItemIconTest {

    @Test
    fun roundTripsThroughTheDatabaseEntity() {
        val entity = FirstItemIcon.toDatabaseEntity("tag://home")
        Assert.assertEquals(CustomAttributeType.Icon.value, entity.type)
        Assert.assertSame(FirstItemIcon, CustomAttribute.fromDatabaseEntity(entity))
    }
}
