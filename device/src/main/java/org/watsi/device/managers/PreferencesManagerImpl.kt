package org.watsi.device.managers

import android.content.Context
import android.preference.PreferenceManager
import com.google.gson.Gson
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import org.threeten.bp.Instant
import org.watsi.domain.entities.AuthenticationToken

class PreferencesManagerImpl(context: Context, private val gson: Gson = Gson()) : PreferencesManager {

    companion object {
        private const val AUTHENTICATION_TOKEN_KEY = "authentication_token"
        private const val MEMBERS_LAST_FETCHED_KEY = "members_last_fetched"
        private const val BILLABLES_LAST_FETCHED_KEY = "billables_last_fetched"
        private const val DIAGNOSES_LAST_FETCHED_KEY = "diagnoses_last_fetched"
    }

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    internal var memberLastFetchedObservable = PublishSubject.create<Instant>()
    internal var billablesLastFetchedObservable = PublishSubject.create<Instant>()
    internal var diagnosesLastFetchedObservable = PublishSubject.create<Instant>()

    override fun getAuthenticationToken(): AuthenticationToken? {
        val tokenJson = sharedPreferences.getString(AUTHENTICATION_TOKEN_KEY, null)
        return if (tokenJson == null) null else gson.fromJson(tokenJson, AuthenticationToken::class.java)
    }

    override fun setAuthenticationToken(token: AuthenticationToken?) {
        val tokenJson = if (token == null) null else gson.toJson(token)
        sharedPreferences.edit().putString(AUTHENTICATION_TOKEN_KEY, tokenJson).apply()
    }

    override fun getMemberLastFetched(): Flowable<Instant> {
        return memberLastFetchedObservable
                .toFlowable(BackpressureStrategy.BUFFER)
                .startWith(Instant.ofEpochMilli(sharedPreferences.getLong(MEMBERS_LAST_FETCHED_KEY, 0)))
    }

    override fun updateMemberLastFetched(instant: Instant) {
        sharedPreferences.edit().putLong(MEMBERS_LAST_FETCHED_KEY, instant.toEpochMilli()).apply()
        memberLastFetchedObservable.onNext(instant)
    }

    override fun getBillablesLastFetched(): Flowable<Instant> {
        return billablesLastFetchedObservable
                .toFlowable(BackpressureStrategy.BUFFER)
                .startWith(Instant.ofEpochMilli(sharedPreferences.getLong(BILLABLES_LAST_FETCHED_KEY, 0)))
    }

    override fun updateBillablesLastFetched(instant: Instant) {
        sharedPreferences.edit().putLong(BILLABLES_LAST_FETCHED_KEY, instant.toEpochMilli()).apply()
        billablesLastFetchedObservable.onNext(instant)
    }

    override fun getDiagnosesLastFetched(): Flowable<Instant> {
        return diagnosesLastFetchedObservable
                .toFlowable(BackpressureStrategy.BUFFER)
                .startWith(Instant.ofEpochMilli(sharedPreferences.getLong(DIAGNOSES_LAST_FETCHED_KEY, 0)))
    }

    override fun updateDiagnosesLastFetched(instant: Instant) {
        sharedPreferences.edit().putLong(DIAGNOSES_LAST_FETCHED_KEY, instant.toEpochMilli()).apply()
        diagnosesLastFetchedObservable.onNext(instant)
    }
}
