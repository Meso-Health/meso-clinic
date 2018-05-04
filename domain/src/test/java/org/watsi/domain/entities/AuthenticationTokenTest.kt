package org.watsi.domain.entities

import org.junit.Assert.assertEquals
import org.junit.Test
import org.watsi.domain.factories.AuthenticationTokenFactory

class AuthenticationTokenTest {

    @Test
    fun getHeaderString() {
        val subject = AuthenticationTokenFactory.build()

        assertEquals("Token ${subject.token}", subject.getHeaderString())
    }
}
