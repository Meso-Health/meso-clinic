package org.watsi.device.managers

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.watsi.device.api.CoverageApi
import org.watsi.device.api.models.AuthenticationTokenApi
import org.watsi.device.api.models.UserApi
import org.watsi.domain.factories.AuthenticationTokenFactory

@RunWith(MockitoJUnitRunner::class)
class SessionManagerImplTest {

    @Mock lateinit var mockPreferencesManager: PreferencesManager
    @Mock lateinit var mockCoverageApi: CoverageApi
    @Mock lateinit var mockLogger: Logger
    lateinit var sessionManager: SessionManagerImpl

    @Before
    fun setup() {
        sessionManager = SessionManagerImpl(mockPreferencesManager, mockCoverageApi, mockLogger)
    }

    @Test
    fun login() {
        val username = "foo"
        val password = "bar"
        val token = AuthenticationTokenFactory.build()
        val authenticationTokenApi = AuthenticationTokenApi(
                token.token, token.expiresAt.toString(), UserApi(1, username, username, 1))
        whenever(mockCoverageApi.getAuthToken(any())).thenReturn(Single.just(authenticationTokenApi))

        sessionManager.login(username, password).test()

        val parsedToken = authenticationTokenApi.toAuthenticationToken()
        verify(mockPreferencesManager).setAuthenticationToken(parsedToken)
        verify(mockLogger).setUser(parsedToken.user)
        assertEquals(sessionManager.currentToken(), parsedToken)
    }

    @Test
    fun logout() {
        sessionManager.logout()

        verify(mockPreferencesManager).setAuthenticationToken(null)
        verify(mockLogger).clearUser()
        assertNull(sessionManager.currentToken())
    }
}
