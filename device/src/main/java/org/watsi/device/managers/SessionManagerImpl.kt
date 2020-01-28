package org.watsi.device.managers

import io.reactivex.Completable
import okhttp3.Credentials
import org.watsi.device.api.CoverageApi
import org.watsi.device.managers.SessionManager.Companion.ALLOWED_HEALTH_CENTER_ROLES
import org.watsi.device.managers.SessionManager.Companion.ALLOWED_HOSPITAL_ROLES
import org.watsi.domain.entities.AuthenticationToken
import org.watsi.domain.entities.User

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
            if (!isUserAllowed(newTokenApi.user.toUser())) {
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

    override fun isUserAllowed(user: User): Boolean {
        return if (user.isHospitalUser()) {
            ALLOWED_HOSPITAL_ROLES.contains(user.role)
        } else {
            ALLOWED_HEALTH_CENTER_ROLES.contains(user.role)
        }
    }

    override fun shouldClearUserData(): Boolean {
        val previousUser = preferencesManager.getPreviousUser()
        val currentUser = token?.user
        return previousUser != null && currentUser != null && previousUser != currentUser && previousUser.providerId != currentUser.providerId
    }

    override fun shouldClearPageKey(currentMemberCount: Int): Boolean {
        val previousMemberCount = preferencesManager.getMembersCountForCurrentPageKey()
        return previousMemberCount != 0 && previousMemberCount > currentMemberCount
    }

    override fun currentAuthenticationToken(): AuthenticationToken? = token

    override fun currentUser(): User? = currentAuthenticationToken()?.user

    override fun userHasPermission(permission: SessionManager.Permissions): Boolean {
        return currentUser()?.let { user ->
            if (isUserAllowed(user)) {
                if (user.isHospitalUser()) {
                    SessionManager.HOSPITAL_ROLE_PERMISSIONS.contains(permission)
                } else {
                    SessionManager.HEALTH_CENTER_ROLE_PERMISSIONS.contains(permission)
                }
            } else {
                false
            }
        } ?: false
    }
}
