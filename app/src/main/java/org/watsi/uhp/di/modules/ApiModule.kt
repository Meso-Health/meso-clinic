package org.watsi.uhp.di.modules

import android.content.Context
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
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
    fun provideCoverageApi(httpClient: OkHttpClient, gson: Gson): CoverageApi {
        return Retrofit.Builder()
                .baseUrl(BuildConfig.API_HOST)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
                .client(httpClient)
                .build()
                .create(CoverageApi::class.java)
    }
}
