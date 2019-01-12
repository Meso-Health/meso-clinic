package org.watsi.device.managers

import android.content.Context
import android.preference.PreferenceManager
import com.google.gson.Gson
import org.threeten.bp.Instant
import org.watsi.domain.entities.AuthenticationToken
import java.util.Locale

class PreferencesManagerImpl(context: Context, private val gson: Gson = Gson()) : PreferencesManager {
    companion object {
        private const val AUTHENTICATION_TOKEN_KEY = "authentication_token"
        private const val MEMBERS_LAST_FETCHED_KEY = "members_last_fetched"
        private const val BILLABLES_LAST_FETCHED_KEY = "billables_last_fetched"
        private const val DIAGNOSES_LAST_FETCHED_KEY = "diagnoses_last_fetched"
        private const val LOCALE_KEY = "locale"
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

    override fun getMemberLastFetched(): Instant {
        return Instant.ofEpochMilli(sharedPreferences.getLong(MEMBERS_LAST_FETCHED_KEY, 0))
    }

    override fun updateMemberLastFetched(instant: Instant) {
        sharedPreferences.edit().putLong(MEMBERS_LAST_FETCHED_KEY, instant.toEpochMilli()).apply()
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
}
