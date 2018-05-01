package org.watsi.device.managers

import io.reactivex.Completable
import okhttp3.Credentials
import org.watsi.device.api.CoverageApi
import org.watsi.domain.entities.AuthenticationToken

class SessionManagerImpl(
        private val preferencesManager: PreferencesManager,
        private val api: CoverageApi,
        private val logger: Logger
) : SessionManager {

    private var token: AuthenticationToken? = preferencesManager.getAuthenticationToken()

    override fun login(username: String, password: String): Completable {
        val apiAuthorizationHeader = Credentials.basic(username, password)
        return api.getAuthToken(apiAuthorizationHeader).flatMapCompletable {
            it.toAuthenticationToken().let { newToken ->
                preferencesManager.setAuthenticationToken(newToken)
                logger.setUser(newToken.user)
                token = newToken
            }
            Completable.complete()
        }
    }

    override fun logout() {
        preferencesManager.setAuthenticationToken(null)
        logger.clearUser()
        token = null
    }

    override fun currentToken(): AuthenticationToken? = token
}
