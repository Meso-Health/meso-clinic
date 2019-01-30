package org.watsi.device.testutils

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okreplay.MatchRules
import okreplay.OkReplayConfig
import okreplay.OkReplayInterceptor
import okreplay.Recorder
import okreplay.TapeMode
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZonedDateTime
import org.watsi.device.api.CoverageApi
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

@RunWith(RobolectricTestRunner::class)
abstract class OkReplayTest {

    lateinit var recorder: Recorder
    lateinit var api: CoverageApi

    @Before
    fun setup() {
        val replayInterceptor = OkReplayInterceptor()
        api = buildApi(replayInterceptor)
        val replayConfig = OkReplayConfig.Builder()
                .sslEnabled(true)
                .interceptor(replayInterceptor)
                .defaultMatchRules(MatchRules.body, MatchRules.path, MatchRules.method, MatchRules.authorization)
                .build()
        recorder = Recorder(replayConfig)
        recorder.start(javaClass.simpleName, tapeMode())
        afterSetup()
    }

    abstract fun afterSetup()

    @After
    fun tearDown() {
        recorder.stop()
    }

    /**
     * Override to TapeMode.READ_WRITE when you are writing tests and need to add/edit tapes
     */
    protected open fun tapeMode(): TapeMode = TapeMode.READ_ONLY

    private fun buildApi(replayInterceptor: OkReplayInterceptor): CoverageApi {
        val gson = GsonBuilder()
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

        val httpClient = OkHttpClient.Builder()
                .addInterceptor(replayInterceptor)
                // SynchronousExecutorService ensures the normally async API requests run immediately
                .dispatcher(Dispatcher(SynchronousExecutorService()))
                .build()

        return Retrofit.Builder()
                .baseUrl("http://localhost:5000")
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
                .build()
                .create(CoverageApi::class.java)
    }
}
