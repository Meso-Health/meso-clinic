package org.watsi.device.managers

import io.reactivex.Completable
import okhttp3.Credentials
import org.watsi.device.api.CoverageApi
import org.watsi.device.managers.SessionManager.Companion.ALLOWED_ROLES
import org.watsi.domain.entities.AuthenticationToken

class SessionManagerImpl(
        private val preferencesManager: PreferencesManager,
        private val api: CoverageApi,
        private val logger: Logger
) : SessionManager {

    private var token: AuthenticationToken? = preferencesManager.getAuthenticationToken()

    override fun login(username: String, password: String): Completable {
        return Completable.fromAction {
            val apiAuthorizationHeader = Credentials.basic(username, password)
            val newTokenApi = api.login(apiAuthorizationHeader).blockingGet()
            if (!ALLOWED_ROLES.contains(newTokenApi.user.role)) {
                throw SessionManager.PermissionException()
            }
            val newToken = newTokenApi.toAuthenticationToken()
            preferencesManager.setAuthenticationToken(newToken)
            logger.setUser(newToken.user)
            token = newToken
        }
    }

    override fun logout() {
        preferencesManager.setPreviousUser(token?.user)
        preferencesManager.setAuthenticationToken(null)
        logger.clearUser()
        token = null
    }

    override fun shouldClearUserData(): Boolean {
        val previousUser = preferencesManager.getPreviousUser()
        val currentUser = token?.user
        return previousUser != null && currentUser != null && previousUser != currentUser && previousUser.providerId != currentUser.providerId
    }

    override fun currentAuthenticationToken(): AuthenticationToken? = token
}
