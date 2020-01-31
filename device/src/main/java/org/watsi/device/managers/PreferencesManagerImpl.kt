package org.watsi.device.managers

import android.content.Context
import android.preference.PreferenceManager
import com.google.gson.Gson
import org.threeten.bp.Instant
import org.watsi.domain.entities.AuthenticationToken
import org.watsi.domain.entities.User
import java.util.Locale

class PreferencesManagerImpl(context: Context, private val gson: Gson = Gson()) : PreferencesManager {
    companion object {
        private const val AUTHENTICATION_TOKEN_KEY = "authentication_token"
        private const val LOCALE_KEY = "locale"
        private const val USER = "user"
        private const val MEMBERS_PAGE_KEY = "members_page_key"
        private const val DATA_LAST_FETCHED_KEY = "data_last_fetched"
        private const val PHOTOS_LAST_FETCHED_KEY = "photos_last_fetched"
        private const val DATA_LAST_SYNCED_KEY = "data_last_synced"
        private const val PHOTOS_LAST_SYNCED_KEY = "photos_last_synced"
        private const val MEMBERS_LAST_FETCHED_KEY = "members_last_fetched"
        private const val MEMBER_PHOTOS_LAST_FETCHED_KEY = "member_photos_last_fetched"
        private const val BILLABLES_LAST_FETCHED_KEY = "billables_last_fetched"
        private const val DIAGNOSES_LAST_FETCHED_KEY = "diagnoses_last_fetched"
        private const val RETURNED_CLAIMS_LAST_FETCHED_KEY = "returned_claims_last_fetched"
        private const val IDENTIFICATION_EVENTS_LAST_FETCHED_KEY = "identification_events_last_fetched"
        private const val COPAYMENT_DEFAULT = "copayment_default"
        private const val MEMBERS_COUNT = "members_count_current_page_key"
    }

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    override fun getAuthenticationToken(): AuthenticationToken? {
        val tokenJson = sharedPreferences.getString(AUTHENTICATION_TOKEN_KEY, null)
        return if (tokenJson == null) null else gson.fromJson(tokenJson, AuthenticationToken::class.java)
    }

    override fun setAuthenticationToken(token: AuthenticationToken?) {
        val tokenJson = if (token == null) null else gson.toJson(token)
        sharedPreferences.edit().putString(AUTHENTICATION_TOKEN_KEY, tokenJson).apply()
    }

    override fun getPreviousUser(): User? {
        val userJson = sharedPreferences.getString(USER, null)
        return if (userJson == null) null else gson.fromJson(userJson, User::class.java)
    }

    override fun setPreviousUser(user: User?) {
        val userJson = if (user == null) null else gson.toJson(user)
        sharedPreferences.edit().putString(USER, userJson).apply()
    }

    override fun getLocale(): Locale? {
        val localeString = sharedPreferences.getString(LOCALE_KEY, null)
        if (localeString == null) {
            return localeString
        }
        val localeParts = localeString.split("_".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        val language = localeParts[0]
        val country = if (localeParts.size > 1) localeParts[1] else ""
        return Locale(language, country)
    }

    override fun updateLocale(locale: Locale) {
        sharedPreferences.edit().putString(LOCALE_KEY, locale.toString()).apply()
    }

    override fun getMembersPageKey(): String? {
        return sharedPreferences.getString(MEMBERS_PAGE_KEY, null)
    }

    override fun updateMembersPageKey(pageKey: String?) {
        sharedPreferences.edit().putString(MEMBERS_PAGE_KEY, pageKey).apply()
    }

    override fun getMembersCountForCurrentPageKey(): Int {
        return sharedPreferences.getInt(MEMBERS_COUNT, 0)
    }

    override fun updateMembersCountForCurrentPageKey(count: Int) {
        sharedPreferences.edit().putInt(MEMBERS_COUNT, count).apply()
    }

    override fun getDataLastFetched(): Instant {
        return Instant.ofEpochMilli(sharedPreferences.getLong(DATA_LAST_FETCHED_KEY, 0))
    }

    override fun updateDataLastFetched(instant: Instant) {
        sharedPreferences.edit().putLong(DATA_LAST_FETCHED_KEY, instant.toEpochMilli()).apply()
    }

    override fun getPhotoLastFetched(): Instant {
        return Instant.ofEpochMilli(sharedPreferences.getLong(PHOTOS_LAST_FETCHED_KEY, 0))
    }

    override fun updatePhotoLastFetched(instant: Instant) {
        sharedPreferences.edit().putLong(PHOTOS_LAST_FETCHED_KEY, instant.toEpochMilli()).apply()
    }

    override fun getDataLastSynced(): Instant {
        return Instant.ofEpochMilli(sharedPreferences.getLong(DATA_LAST_SYNCED_KEY, 0))
    }

    override fun updateDataLastSynced(instant: Instant) {
        sharedPreferences.edit().putLong(DATA_LAST_SYNCED_KEY, instant.toEpochMilli()).apply()
    }

    override fun getPhotoLastSynced(): Instant {
        return Instant.ofEpochMilli(sharedPreferences.getLong(PHOTOS_LAST_SYNCED_KEY, 0))
    }

    override fun updatePhotoLastSynced(instant: Instant) {
        sharedPreferences.edit().putLong(PHOTOS_LAST_SYNCED_KEY, instant.toEpochMilli()).apply()
    }

    override fun getMemberLastFetched(): Instant {
        return Instant.ofEpochMilli(sharedPreferences.getLong(MEMBERS_LAST_FETCHED_KEY, 0))
    }

    override fun updateMemberLastFetched(instant: Instant) {
        sharedPreferences.edit().putLong(MEMBERS_LAST_FETCHED_KEY, instant.toEpochMilli()).apply()
    }

    override fun getMemberPhotosLastFetched(): Instant {
        return Instant.ofEpochMilli(sharedPreferences.getLong(MEMBER_PHOTOS_LAST_FETCHED_KEY, 0))
    }

    override fun updateMemberPhotosLastFetched(instant: Instant) {
        sharedPreferences.edit().putLong(MEMBER_PHOTOS_LAST_FETCHED_KEY, instant.toEpochMilli()).apply()
    }

    override fun getBillablesLastFetched(): Instant {
        return Instant.ofEpochMilli(sharedPreferences.getLong(BILLABLES_LAST_FETCHED_KEY, 0))
    }

    override fun updateBillablesLastFetched(instant: Instant) {
        sharedPreferences.edit().putLong(BILLABLES_LAST_FETCHED_KEY, instant.toEpochMilli()).apply()
    }

    override fun getDiagnosesLastFetched(): Instant {
        return Instant.ofEpochMilli(sharedPreferences.getLong(DIAGNOSES_LAST_FETCHED_KEY, 0))
    }

    override fun updateDiagnosesLastFetched(instant: Instant) {
        sharedPreferences.edit().putLong(DIAGNOSES_LAST_FETCHED_KEY, instant.toEpochMilli()).apply()
    }

    override fun getReturnedClaimsLastFetched(): Instant {
        return Instant.ofEpochMilli(sharedPreferences.getLong(RETURNED_CLAIMS_LAST_FETCHED_KEY, 0))
    }

    override fun updateReturnedClaimsLastFetched(instant: Instant) {
        sharedPreferences.edit().putLong(RETURNED_CLAIMS_LAST_FETCHED_KEY, instant.toEpochMilli()).apply()
    }

    override fun getIdentificationEventsLastFetched(): Instant {
        return Instant.ofEpochMilli(sharedPreferences.getLong(IDENTIFICATION_EVENTS_LAST_FETCHED_KEY, 0))
    }

    override fun updateIdentificationEventsLastFetched(instant: Instant) {
        sharedPreferences.edit().putLong(IDENTIFICATION_EVENTS_LAST_FETCHED_KEY, instant.toEpochMilli()).apply()
    }

    override fun getDefaultCopaymentAmount(): Int {
        val copaymentDefaultJson = sharedPreferences.getString(COPAYMENT_DEFAULT, null)
        return if (copaymentDefaultJson == null) 0 else copaymentDefaultJson.toInt()
    }

    override fun setDefaultCopaymentAmount(amount: Int) {
        val copaymentDefaultJson = gson.toJson(amount)
        sharedPreferences.edit().putString(COPAYMENT_DEFAULT, copaymentDefaultJson).apply()
    }
}
