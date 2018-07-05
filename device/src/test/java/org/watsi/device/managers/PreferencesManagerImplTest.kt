package org.watsi.device.managers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.threeten.bp.Instant
import org.watsi.domain.entities.AuthenticationToken
import org.watsi.domain.entities.User

@RunWith(RobolectricTestRunner::class)
class PreferencesManagerImplTest {

    val preferencesManager = PreferencesManagerImpl(RuntimeEnvironment.application)

    @Test
    fun supportsGettingAndSettingAuthenticationToken() {
        val user = User(0, "foo", "bar", 0)
        val token = AuthenticationToken("token", Instant.now(), user)
        assertNull(preferencesManager.getAuthenticationToken())
        preferencesManager.setAuthenticationToken(token)
        assertEquals(preferencesManager.getAuthenticationToken().toString(), token.toString())
    }
}
