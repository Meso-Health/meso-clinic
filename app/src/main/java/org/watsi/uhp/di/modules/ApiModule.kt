package org.watsi.uhp.di.modules

import android.content.Context
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.watsi.device.api.CoverageApi
import org.watsi.device.api.NotModifiedInterceptor
import org.watsi.uhp.BuildConfig
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlin.reflect.jvm.internal.impl.resolve.constants.EnumValue

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
                .addInterceptor(NotModifiedInterceptor())
                .retryOnConnectionFailure(false)
                .build()
    }

    @Provides
    fun provideCoverageApi(httpClient: OkHttpClient): CoverageApi {
        val gson = GsonBuilder()
                .registerTypeAdapter(LocalDate::class.javaObjectType, JsonDeserializer<LocalDate> { json, _, _ ->
                    LocalDate.parse(json.asJsonPrimitive.asString)
                })
                .registerTypeAdapter(Instant::class.javaObjectType, JsonDeserializer<Instant> { json, _, _ ->
                    Instant.parse(json.asJsonPrimitive.asString)
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
