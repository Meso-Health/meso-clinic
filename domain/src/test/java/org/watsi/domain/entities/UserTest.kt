package org.watsi.domain.entities

import org.junit.Assert.assertEquals
import org.junit.Test
import org.watsi.domain.factories.UserFactory

class UserTest {

    @Test
    fun foo() {
        val user = UserFactory.build(id = 1)
        assertEquals(1, user.id)
    }
}
