package org.watsi.uhp.managers

import android.app.Activity
import android.content.Context
import org.watsi.device.managers.PreferencesManager
import java.util.Locale
import javax.inject.Inject


class LocaleManager @Inject constructor(private val preferencesManager: PreferencesManager) {
    companion object {
        val AMHARIC_LOCALE = Locale("am")
        val TIGRINYA_LOCALE = Locale("ti")
    }

    fun createLocalizedContext(baseContext: Context): Context {
        val config = baseContext.resources.configuration
        config.setLocale(preferencesManager.getLocale())
        return baseContext.createConfigurationContext(config)
    }

    fun setLocale(locale: Locale, activity: Activity) {
        preferencesManager.updateLocale(locale)
        activity.recreate()
    }

    fun toggleLocale(activity: Activity) {
        when (preferencesManager.getLocale()) {
            AMHARIC_LOCALE -> setLocale(Locale.US, activity)
            TIGRINYA_LOCALE -> setLocale(AMHARIC_LOCALE, activity)
            else -> setLocale(TIGRINYA_LOCALE, activity)
        }
    }
}
