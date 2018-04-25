package org.watsi.domain.entities

import org.junit.Assert.assertEquals
import org.junit.Test

class UserTest {

    @Test
    fun foo() {
        val user = User(1, "foo", "bar", 1)
        assertEquals(1, user.id)
    }
}
