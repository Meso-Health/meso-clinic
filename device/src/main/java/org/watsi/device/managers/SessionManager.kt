package org.watsi.device.managers

import io.reactivex.Completable
import org.watsi.domain.entities.AuthenticationToken
import org.watsi.domain.entities.User

interface SessionManager {
    fun login(username: String, password: String): Completable
    fun logout()
    fun currentAuthenticationToken(): AuthenticationToken?
    fun shouldClearUserData(): Boolean
    fun userHasPermission(neededPermission: Permissions): Boolean

    class PermissionException : Exception()

    // TODO: Get these from the backend
    enum class Permissions {
        WORKFLOW_HOSPITAL_IDENTIFICATION,
        WORKFLOW_CLINIC_IDENTIFICATION,
        WORKFLOW_CLAIMS_PREPARATION,
        FETCH_PHOTOS,
        FETCH_BILLABLES,
        FETCH_DIAGNOSES,
        FETCH_RETURNED_CLAIMS,
        SYNC_PRICE_SCHEDULES
        // TODO: Should facility head approval flows be gated through here?
    }

    companion object {
        val ALLOWED_ROLES = listOf("provider")

        val HEALTH_CENTER_PERMISSIONS = setOf(
            Permissions.WORKFLOW_CLINIC_IDENTIFICATION,
            Permissions.WORKFLOW_CLAIMS_PREPARATION,
            Permissions.FETCH_PHOTOS,
            Permissions.FETCH_BILLABLES,
            Permissions.FETCH_DIAGNOSES,
            Permissions.FETCH_RETURNED_CLAIMS,
            Permissions.SYNC_PRICE_SCHEDULES
        )

        val HOSPITAL_PERMISSIONS = setOf(
            Permissions.WORKFLOW_HOSPITAL_IDENTIFICATION
        )

        val PROVIDER_PERMISSIONS_MAP = mapOf(
            User.ProviderType.HEALTH_CENTER to HEALTH_CENTER_PERMISSIONS,
            User.ProviderType.PRIMARY_HOSPITAL to HOSPITAL_PERMISSIONS,
            User.ProviderType.GENERAL_HOSPITAL to HOSPITAL_PERMISSIONS,
            User.ProviderType.TERTIARY_HOSPITAL to HOSPITAL_PERMISSIONS,
            User.ProviderType.UNCLASSIFIED to HEALTH_CENTER_PERMISSIONS // TODO: What are the permissions for unclassified?
        )
    }
}
