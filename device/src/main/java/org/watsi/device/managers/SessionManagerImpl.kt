package org.watsi.device.managers

import io.reactivex.Completable
import okhttp3.Credentials
import org.watsi.device.api.CoverageApi
import org.watsi.domain.entities.AuthenticationToken

class SessionManagerImpl(
        private val preferencesManager: PreferencesManager,
        private val api: CoverageApi
) : SessionManager {

    private var token: AuthenticationToken? = preferencesManager.getAuthenticationToken()

    override fun login(username: String, password: String): Completable {
        val apiAuthorizationHeader = Credentials.basic(username, password)
        return api.getAuthToken(apiAuthorizationHeader).flatMapCompletable {
            token = it.toAuthenticationToken()
            preferencesManager.setAuthenticationToken(token)
            Completable.complete()
        }
    }

    override fun logout() {
        preferencesManager.setAuthenticationToken(null)
        token = null
    }

    override fun currentToken(): AuthenticationToken? = token
}
