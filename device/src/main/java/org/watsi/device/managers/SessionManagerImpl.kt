package org.watsi.device.managers

import io.reactivex.Completable
import okhttp3.Credentials
import org.watsi.device.api.CoverageApi
import org.watsi.domain.entities.AuthenticationToken

class SessionManagerImpl(
        private val preferencesManager: PreferencesManager,
        private val api: CoverageApi
) : SessionManager {

    private var token: AuthenticationToken? = null

    init {
        token = preferencesManager.getAuthenticationToken()
    }

    override fun login(username: String, password: String): Completable {
        val apiAuthorizationHeader = Credentials.basic(username, password)
        return api.getAuthToken(apiAuthorizationHeader).flatMapCompletable {
            preferencesManager.setAuthenticationToken(it.toAuthenticationToken())
            Completable.complete()
        }
    }

    override fun logout() {
        preferencesManager.setAuthenticationToken(null)
        token = null
    }

    override fun currentToken(): AuthenticationToken? = token
}
