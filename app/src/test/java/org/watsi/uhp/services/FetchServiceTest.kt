package org.watsi.uhp.services

import android.accounts.AccountManager
import android.accounts.AccountManagerFuture
import android.os.Bundle
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.watsi.uhp.managers.PreferencesManager
import org.watsi.uhp.managers.SessionManager

class FetchServiceTest {

    private val mockPreferencesManager = mock(PreferencesManager::class.java)
    private val service = FetchService()
    private val serviceSpy = spy(service)

    @Test
    fun performSync_nullToken_doesNotPerformFetch() {
        doReturn(null).`when`(serviceSpy).getAuthenticationToken(any())
        doReturn(mockPreferencesManager).`when`(serviceSpy).fetchPreferencesManager()

        serviceSpy.performSync()

        verify(serviceSpy, never()).fetchMembers(any(), any())
        verify(serviceSpy, never()).fetchBillables(any(), any())
    }

    @Test
    fun performSync_tokenAvailable_performsFetch() {
        val token = "token"
        doReturn("token").`when`(serviceSpy).getAuthenticationToken(any())
        doReturn(mockPreferencesManager).`when`(serviceSpy).fetchPreferencesManager()
        doNothing().`when`(serviceSpy).fetchMembers(any(), any())
        doNothing().`when`(serviceSpy).fetchBillables(any(), any())

        serviceSpy.performSync()

        verify(serviceSpy).fetchMembers(token, mockPreferencesManager)
        verify(serviceSpy).fetchBillables(token, mockPreferencesManager)
    }

//    TODO: implement when we introduce dependency injection so we can inject mock ExceptionManager
//    @Test
//    fun performSync_fetchRaisesException_reportsException() {}

    @Test
    fun getAuthenticationToken_returnsTokenFromSessionManager() {
        val token = "token"
        val mockSessionManager = mock(SessionManager::class.java)
        val mockTokenFuture = mock(AccountManagerFuture::class.java)
        val mockBundle = mock(Bundle::class.java)
        doReturn(mockSessionManager).`when`(serviceSpy).fetchSessionManager(mockPreferencesManager)
        doReturn(mockTokenFuture).`when`(mockSessionManager).fetchToken()
        doReturn(mockBundle).`when`(mockTokenFuture).result
        doReturn(token).`when`(mockBundle).getString(AccountManager.KEY_AUTHTOKEN)

        assertEquals(serviceSpy.getAuthenticationToken(mockPreferencesManager), token)
    }
}
