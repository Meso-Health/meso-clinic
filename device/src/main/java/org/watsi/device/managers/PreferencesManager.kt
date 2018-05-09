package org.watsi.device.managers

import io.reactivex.Flowable
import org.threeten.bp.Instant
import org.watsi.domain.entities.AuthenticationToken

interface PreferencesManager {
    fun getAuthenticationToken(): AuthenticationToken?
    fun setAuthenticationToken(token: AuthenticationToken?)
    fun getMemberLastFetched(): Flowable<Instant>
    fun updateMemberLastFetched(instant: Instant)
    fun getBillablesLastFetched(): Flowable<Instant>
    fun updateBillablesLastFetched(instant: Instant)
    fun getDiagnosesLastFetched(): Flowable<Instant>
    fun updateDiagnosesLastFetched(instant: Instant)
}
