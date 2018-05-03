package org.watsi.domain.entities

import org.junit.Test
import org.watsi.domain.factories.AuthenticationTokenFactory
import java.time.Instant

class AuthenticationTokenTest {

    @Test
    fun getHeaderString() {
        val subject = AuthenticationTokenFactory.build()

    }
}
