package org.watsi.uhp.di.modules

import android.content.Context
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZonedDateTime
import org.watsi.device.api.CoverageApi
import org.watsi.uhp.BuildConfig
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class ApiModule {

    @Singleton
    @Provides
    fun provideHttpClient(context: Context): OkHttpClient {
        val httpTimeoutInSeconds = 30L
        val cacheSize = 10 * 1024 * 1024 // 10 MiB
        val cache = Cache(context.cacheDir, cacheSize.toLong())

        return OkHttpClient.Builder()
                .cache(cache)
                .connectTimeout(httpTimeoutInSeconds, TimeUnit.SECONDS)
                .readTimeout(httpTimeoutInSeconds, TimeUnit.SECONDS)
                .writeTimeout(httpTimeoutInSeconds, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .build()
    }

    @Provides
    fun provideCoverageApi(httpClient: OkHttpClient): CoverageApi {
        val gson = GsonBuilder()
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

        return Retrofit.Builder()
                .baseUrl(BuildConfig.API_HOST)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
                .client(httpClient)
                .build()
                .create(CoverageApi::class.java)
    }
}
