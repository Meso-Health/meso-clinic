package org.watsi.uhp.services

import okreplay.*
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.watsi.uhp.api.ApiService
import org.watsi.uhp.database.DatabaseHelper

@RunWith(RobolectricTestRunner::class)
abstract class ReplayTest {

    private val context = RuntimeEnvironment.application
    private lateinit var recorder: Recorder

    @Before
    fun setup() {
        DatabaseHelper.init(context)
        ApiService.requestBuilder(context)

        val replayConfig = OkReplayConfig.Builder()
                .sslEnabled(true)
                .interceptor(ApiService.replayInterceptor)
                .defaultMatchRules(MatchRules.path, MatchRules.method, MatchRules.authorization)
                .build()

        recorder = Recorder(replayConfig)
        recorder.start(javaClass.simpleName, tapeMode())
    }

    @After
    fun tearDown() {
        DatabaseHelper.reset()
        recorder.stop()
    }

    /**
     * Override when you are writing tests and need to add/edit tapes
     */
    protected open fun tapeMode(): TapeMode = TapeMode.READ_ONLY
}
