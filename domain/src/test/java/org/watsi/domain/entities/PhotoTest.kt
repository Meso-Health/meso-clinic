package org.watsi.domain.entities

import org.junit.Assert
import org.junit.Test
import java.util.UUID

class PhotoTest {

    @Test
    fun equals_differentId_returnsFalse() {
        val bytes = ByteArray(1, { 0xa })
        val p1 = Photo(UUID.randomUUID(), bytes)
        val p2 = Photo(UUID.randomUUID(), bytes)

        Assert.assertFalse(p1 == p2)
    }

    @Test
    fun equals_differentArray_returnsFalse() {
        val id = UUID.randomUUID()
        val p1 = Photo(id, ByteArray(1, { 0xa }))
        val p2 = Photo(id, ByteArray(1, { 0xb }))

        Assert.assertFalse(p1 == p2)
    }

    @Test
    fun equals_allPropertiesEqual_returnsTrue() {
        val id = UUID.randomUUID()
        val p1 = Photo(id, ByteArray(1, { 0xa }))
        val p2 = Photo(id, ByteArray(1, { 0xa }))

        Assert.assertTrue(p1 == p2)
    }
}
