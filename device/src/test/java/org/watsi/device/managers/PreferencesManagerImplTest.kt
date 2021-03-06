package org.watsi.device.managers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.threeten.bp.Instant
import org.watsi.domain.entities.AuthenticationToken
import org.watsi.domain.factories.UserFactory

@RunWith(RobolectricTestRunner::class)
class PreferencesManagerImplTest {

    val preferencesManager = PreferencesManagerImpl(RuntimeEnvironment.application)

    @Test
    fun supportsGettingAndSettingAuthenticationToken() {
        val user = UserFactory.build()
        val token = AuthenticationToken("token", Instant.now(), user)
        assertNull(preferencesManager.getAuthenticationToken())
        preferencesManager.setAuthenticationToken(token)
        assertEquals(preferencesManager.getAuthenticationToken().toString(), token.toString())
    }

    @Test
    fun supportsGettingAndSettingPreviousUser() {
        val user = UserFactory.build()
        assertNull(preferencesManager.getPreviousUser())
        preferencesManager.setPreviousUser(user)
        assertEquals(preferencesManager.getPreviousUser(), user)
    }
}
