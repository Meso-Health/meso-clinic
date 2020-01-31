package org.watsi.uhp.di.modules

import android.content.Context
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import com.jakewharton.threetenabp.AndroidThreeTen
import com.rollbar.android.Rollbar
import dagger.Module
import dagger.Provides
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZonedDateTime
import org.watsi.device.api.CoverageApi
import org.watsi.device.managers.Logger
import org.watsi.device.managers.NetworkManager
import org.watsi.device.managers.NetworkManagerImpl
import org.watsi.device.managers.PreferencesManager
import org.watsi.device.managers.PreferencesManagerImpl
import org.watsi.device.managers.SessionManager
import org.watsi.device.managers.SessionManagerImpl
import org.watsi.uhp.BuildConfig
import org.watsi.uhp.managers.AndroidKeyboardManager
import org.watsi.uhp.managers.DebugLogger
import org.watsi.uhp.managers.KeyboardManager
import org.watsi.uhp.managers.RollbarLogger
import javax.inject.Singleton

@Module
class DeviceModule {
    @Provides
    fun provideGson(): Gson {
        return GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .serializeNulls()
                .registerTypeAdapter(LocalDate::class.javaObjectType, JsonSerializer<LocalDate> { src, _, _ ->
                    JsonPrimitive(src.toString())
                })
                .registerTypeAdapter(LocalDate::class.javaObjectType, JsonDeserializer<LocalDate> { json, _, _ ->
                    LocalDate.parse(json.asJsonPrimitive.asString)
                })
                .registerTypeAdapter(Instant::class.javaObjectType, JsonSerializer<Instant> { src, _, _ ->
                    JsonPrimitive(src.toString())
                })
                .registerTypeAdapter(Instant::class.javaObjectType, JsonDeserializer<Instant> { json, _, _ ->
                    // need to parse using ZonedDateTime because Instant.parse does not
                    // understand the ISO 8061 format with timezone returned from the server
                    ZonedDateTime.parse(json.asJsonPrimitive.asString).toInstant()
                })
                .create()
    }

    @Provides
    fun provideClock(context: Context): Clock {
        AndroidThreeTen.init(context)
        return Clock.systemDefaultZone()
    }

    @Singleton
    @Provides
    fun providePreferencesManager(context: Context): PreferencesManager {
        return PreferencesManagerImpl(context)
    }

    @Singleton
    @Provides
    fun provideNetworkManager(context: Context): NetworkManager {
        return NetworkManagerImpl(context)
    }

    @Singleton
    @Provides
    fun provideSessionManager(preferencesManager: PreferencesManager,
                              api: CoverageApi,
                              logger: Logger): SessionManager {
        return SessionManagerImpl(preferencesManager, api, logger)
    }

    @Singleton
    @Provides
    fun provideLogger(context: Context): Logger {
        return if (BuildConfig.REPORT_TO_ROLLBAR) {
            val rollbarEnvironmentIdentifier = BuildConfig.FLAVOR + BuildConfig.BUILD_TYPE
            Rollbar.init(context, BuildConfig.ROLLBAR_API_KEY, rollbarEnvironmentIdentifier)
            RollbarLogger()
        } else {
            DebugLogger()
        }
    }

    @Provides
    fun provideKeyboardManager(context: Context): KeyboardManager {
        return AndroidKeyboardManager(context)
    }
}
