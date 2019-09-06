package org.watsi.device.managers

import io.reactivex.Completable
import org.watsi.domain.entities.AuthenticationToken
import org.watsi.domain.entities.User

interface SessionManager {
    fun login(username: String, password: String): Completable
    fun logout()
    fun currentAuthenticationToken(): AuthenticationToken?
    fun currentUser(): User?
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
        val ALLOWED_ROLES = listOf(
            "provider", // legacy role
            "facility_head", // legacy role
            "claims_preparer", // legacy role
            "card_room_worker", // legacy role
            "receptionist",
            "claims_officer",
            "facility_director"
        )

        // these roles can only access identification functionality and not prepare claims
        val ID_ONLY_ROLES = listOf(
            "card_room_worker",
            "receptionist"
        )

        /*
         * This application supports two different workflows:
         *
         * 1. The check-in workflow is used by card room workers and the only action they take is
         *    to identify and check-in patients. The actual claim entry will occur separately
         *    via the web application. This is supported for both health center and hospital card
         *    room workers with the only difference being that hospital users are required to
         *    collect additional inbound encounter information (e.g. visit reason) during the check-in.
         *    Because of this entry
         *
         * 2. The submission workflow is only supported for health center users and involves all the
         *    steps of claims submission - identification, check-in, claims entry, approval and
         *    handling returned claims.
         *
         */
        val HEALTH_CENTER_CHECK_IN_PERMISSIONS = setOf(
            Permissions.WORKFLOW_IDENTIFICATION,
            Permissions.SYNC_PARTIAL_CLAIMS,
            Permissions.FETCH_IDENTIFICATION_EVENTS,
            Permissions.FETCH_ENROLLMENT_PERIODS
        )

        val HOSPITAL_CENTER_CHECK_IN_PERMISSIONS = setOf(
            Permissions.WORKFLOW_IDENTIFICATION,
            Permissions.SYNC_PARTIAL_CLAIMS,
            Permissions.CAPTURE_INBOUND_ENCOUNTER_INFORMATION,
            Permissions.FETCH_IDENTIFICATION_EVENTS,
            Permissions.FETCH_ENROLLMENT_PERIODS
        )

        val SUBMISSION_PERMISSIONS = setOf(
            Permissions.WORKFLOW_IDENTIFICATION,
            Permissions.WORKFLOW_CLAIMS_PREPARATION,
            Permissions.FETCH_PHOTOS,
            Permissions.FETCH_BILLABLES,
            Permissions.FETCH_DIAGNOSES,
            Permissions.FETCH_RETURNED_CLAIMS,
            Permissions.FETCH_ENROLLMENT_PERIODS,
            Permissions.SYNC_PRICE_SCHEDULES
        )
    }
}
