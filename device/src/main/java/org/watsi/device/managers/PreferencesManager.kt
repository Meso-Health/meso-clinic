package org.watsi.device.managers

import org.threeten.bp.Instant
import org.watsi.domain.entities.AuthenticationToken
import org.watsi.domain.entities.User
import java.util.Locale

interface PreferencesManager {
    fun getAuthenticationToken(): AuthenticationToken?
    fun setAuthenticationToken(token: AuthenticationToken?)
    fun getPreviousUser(): User?
    fun setPreviousUser(user: User?)
    fun getLocale(): Locale?
    fun updateLocale(locale: Locale)
    fun getMembersPageKey(): String?
    fun updateMembersPageKey(pageKey: String?)
    fun getDataLastFetched(): Instant
    fun updateDataLastFetched(instant: Instant)
    fun getPhotoLastFetched(): Instant
    fun updatePhotoLastFetched(instant: Instant)
    fun getDataLastSynced(): Instant
    fun updateDataLastSynced(instant: Instant)
    fun getPhotoLastSynced(): Instant
    fun updatePhotoLastSynced(instant: Instant)
    fun getMemberLastFetched(): Instant
    fun updateMemberLastFetched(instant: Instant)
    fun getMemberPhotosLastFetched(): Instant
    fun updateMemberPhotosLastFetched(instant: Instant)
    fun getBillablesLastFetched(): Instant
    fun updateBillablesLastFetched(instant: Instant)
    fun getDiagnosesLastFetched(): Instant
    fun updateDiagnosesLastFetched(instant: Instant)
    fun getReturnedClaimsLastFetched(): Instant
    fun updateReturnedClaimsLastFetched(instant: Instant)
}
