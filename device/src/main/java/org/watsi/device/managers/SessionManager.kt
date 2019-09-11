package org.watsi.device.managers

import io.reactivex.Completable
import org.watsi.domain.entities.AuthenticationToken
import org.watsi.domain.entities.User

interface SessionManager {
    fun login(username: String, password: String): Completable
    fun logout()
    fun currentAuthenticationToken(): AuthenticationToken?
    fun currentUser(): User?
    fun isUserAllowed(user: User): Boolean
    fun shouldClearUserData(): Boolean
    fun userHasPermission(permission: Permissions): Boolean

    class PermissionException : Exception()

    // TODO: Get these from the backend
    enum class Permissions {
        WORKFLOW_IDENTIFICATION,
        WORKFLOW_CLAIMS_PREPARATION,
        CAPTURE_INBOUND_ENCOUNTER_INFORMATION,
        FETCH_PHOTOS, // TODO: Photo fetching probably won't stay as a permission like this. It'll probably be divided by catchment
        FETCH_BILLABLES,
        FETCH_DIAGNOSES,
        FETCH_RETURNED_CLAIMS,
        FETCH_IDENTIFICATION_EVENTS, // TODO: This is not yet safe for clinics while the backend requires partial encounters
        FETCH_ENROLLMENT_PERIODS,
        SYNC_PARTIAL_CLAIMS,
        SYNC_PRICE_SCHEDULES
    }

    companion object {
        val ALLOWED_HEALTH_CENTER_ROLES = listOf(
            "provider", // legacy role
            "facility_head", // legacy role
            "claims_preparer", // legacy role
            "card_room_worker", // legacy role
            "receptionist",
            "claims_officer",
            "facility_director"
        )

        val ALLOWED_HOSPITAL_ROLES = listOf(
            "card_room_worker", // legacy role
            "receptionist"
        )

        /*
         * This application supports two different workflows:
         *
         * 1. The hospital check-in workflow is used by hospital card room workers.
         *    The only action they take is to identify and check-in patients; the actual
         *    claim entry will occur separately via the web application.
         *    The only difference between this check-in workflow and the one used by health center
         *    users is that they are required to collect additional inbound encounter information
         *    (e.g. visit reason) during the check-in.
         *
         * 2. The submission workflow is only supported for health center users and involves all the
         *    steps of claims submission - identification, check-in, claims entry, approval and
         *    handling returned claims.
         *
         */

        val HOSPITAL_CHECK_IN_PERMISSIONS = setOf(
            Permissions.WORKFLOW_IDENTIFICATION,
            Permissions.SYNC_PARTIAL_CLAIMS,
            Permissions.CAPTURE_INBOUND_ENCOUNTER_INFORMATION,
            Permissions.FETCH_IDENTIFICATION_EVENTS,
            Permissions.FETCH_ENROLLMENT_PERIODS
        )

        val HEALTH_CENTER_CHECK_IN_PERMISSIONS = setOf(
            Permissions.WORKFLOW_IDENTIFICATION,
            Permissions.FETCH_ENROLLMENT_PERIODS
        )

        val HEALTH_CENTER_CLAIMS_PREPARATION_PERMISSIONS = setOf(
            Permissions.WORKFLOW_CLAIMS_PREPARATION,
            Permissions.FETCH_PHOTOS,
            Permissions.FETCH_BILLABLES,
            Permissions.FETCH_DIAGNOSES,
            Permissions.FETCH_RETURNED_CLAIMS,
            Permissions.SYNC_PRICE_SCHEDULES
        )

        val HOSPITAL_ROLE_PERMISSIONS = HOSPITAL_CHECK_IN_PERMISSIONS
        val HEALTH_CENTER_ROLE_PERMISSIONS = HEALTH_CENTER_CHECK_IN_PERMISSIONS.union(HEALTH_CENTER_CLAIMS_PREPARATION_PERMISSIONS)
    }
}
