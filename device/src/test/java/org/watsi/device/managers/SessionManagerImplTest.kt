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
import org.watsi.domain.entities.User
import org.watsi.domain.factories.AuthenticationTokenFactory
import org.watsi.domain.factories.UserFactory

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
    fun login_allowedRole() {
        val username = "foo"
        val password = "bar"
        val token = AuthenticationTokenFactory.build(user = UserFactory.build(role = "provider"))
        val authenticationTokenApi = AuthenticationTokenApi(
            token = token.token,
            expiresAt = token.expiresAt.toString(),
            user = UserApi(token.user)
        )
        whenever(mockCoverageApi.login(any())).thenReturn(Single.just(authenticationTokenApi))

        sessionManager.login(username, password).test().assertComplete()

        val parsedToken = authenticationTokenApi.toAuthenticationToken()
        verify(mockPreferencesManager).setAuthenticationToken(parsedToken)
        verify(mockLogger).setUser(parsedToken.user)
        assertEquals(parsedToken, sessionManager.currentAuthenticationToken())
    }

    @Test
    fun login_admin() {
        val username = "foo"
        val password = "bar"
        val disallowedRoles = listOf("admin", "enrollment_worker")
        disallowedRoles.forEach { disallowedRole ->
            val token = AuthenticationTokenFactory.build(user = UserFactory.build(role = disallowedRole))
            val authenticationTokenApi = AuthenticationTokenApi(
                token.token, token.expiresAt.toString(), UserApi(token.user))
            whenever(mockCoverageApi.login(any())).thenReturn(Single.just(authenticationTokenApi))

            val result = sessionManager.login(username, password).test()

            result.assertError(SessionManager.PermissionException::class.java)
        }
    }

    @Test
    fun logout() {
        sessionManager.logout()

        verify(mockPreferencesManager).setAuthenticationToken(null)
        verify(mockLogger).clearUser()
        assertNull(sessionManager.currentAuthenticationToken())
    }

    @Test
    fun userHasPermission_noUser_returnsFalse() {
        whenever(mockPreferencesManager.getAuthenticationToken()).thenReturn(null)
        sessionManager = SessionManagerImpl(mockPreferencesManager, mockCoverageApi, mockLogger)

        assertEquals(false, sessionManager.userHasPermission(SessionManager.Permissions.FETCH_ENROLLMENT_PERIODS))
    }

    private fun setUser(isHospitalUser: Boolean, isReceptionist: Boolean) {
        val role = if (isReceptionist) "receptionist" else "claims_preparer"
        val providerType = if (isHospitalUser) User.ProviderType.GENERAL_HOSPITAL else User.ProviderType.HEALTH_CENTER
        val user = UserFactory.build(role = role, providerType = providerType)
        val authToken = AuthenticationTokenFactory.build(user = user)
        whenever(mockPreferencesManager.getAuthenticationToken()).thenReturn(authToken)
        sessionManager = SessionManagerImpl(mockPreferencesManager, mockCoverageApi, mockLogger)
    }

    @Test
    fun userHasPermission_hospitalReceptionist() {
        setUser(true, true)

        assertEquals(false, sessionManager.userHasPermission(SessionManager.Permissions.WORKFLOW_CLAIMS_PREPARATION))
        assertEquals(true, sessionManager.userHasPermission(SessionManager.Permissions.WORKFLOW_IDENTIFICATION))
        assertEquals(true, sessionManager.userHasPermission(SessionManager.Permissions.CAPTURE_INBOUND_ENCOUNTER_INFORMATION))
    }

    @Test
    fun userHasPermission_hospitalNonReceptionist() {
        setUser(true, false)

        assertEquals(false, sessionManager.userHasPermission(SessionManager.Permissions.WORKFLOW_CLAIMS_PREPARATION))
        assertEquals(false, sessionManager.userHasPermission(SessionManager.Permissions.WORKFLOW_IDENTIFICATION))
        assertEquals(false, sessionManager.userHasPermission(SessionManager.Permissions.CAPTURE_INBOUND_ENCOUNTER_INFORMATION))
    }

    @Test
    fun userHasPermission_healthCenterReceptionist() {
        setUser(false, true)

        assertEquals(true, sessionManager.userHasPermission(SessionManager.Permissions.WORKFLOW_CLAIMS_PREPARATION))
        assertEquals(true, sessionManager.userHasPermission(SessionManager.Permissions.WORKFLOW_IDENTIFICATION))
        assertEquals(false, sessionManager.userHasPermission(SessionManager.Permissions.CAPTURE_INBOUND_ENCOUNTER_INFORMATION))
    }

    @Test
    fun userHasPermission_healthCenterNonReceptionist() {
        setUser(false, false)

        assertEquals(true, sessionManager.userHasPermission(SessionManager.Permissions.WORKFLOW_CLAIMS_PREPARATION))
        assertEquals(true, sessionManager.userHasPermission(SessionManager.Permissions.WORKFLOW_IDENTIFICATION))
        assertEquals(false, sessionManager.userHasPermission(SessionManager.Permissions.CAPTURE_INBOUND_ENCOUNTER_INFORMATION))
    }
}
