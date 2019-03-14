package org.watsi.device.managers

import org.threeten.bp.Instant
import org.watsi.domain.entities.AuthenticationToken
import org.watsi.domain.entities.User
import java.util.Locale

interface PreferencesManager {
    fun getAuthenticationToken(): AuthenticationToken?
    fun setAuthenticationToken(token: AuthenticationToken?)
    fun getMembersPageKey(): String?
    fun updateMembersPageKey(pageKey: String?)
    fun getMemberLastFetched(): Instant
    fun updateMemberLastFetched(instant: Instant)
    fun getBillablesLastFetched(): Instant
    fun updateBillablesLastFetched(instant: Instant)
    fun getDiagnosesLastFetched(): Instant
    fun updateDiagnosesLastFetched(instant: Instant)
    fun getLocale(): Locale?
    fun updateLocale(locale: Locale)
    fun getPreviousUser(): User?
    fun setPreviousUser(user: User?)
}
