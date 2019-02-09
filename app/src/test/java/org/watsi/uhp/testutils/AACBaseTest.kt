package org.watsi.uhp.testutils

import android.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Rule
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
/*
 *  This base test class should be used whenever testing Android Architecture Components that involves async behavior.
 *  i.e. use this for LiveData so it doesn't try to use an Android Looper.
 *  This swaps the background executor used by the Architecture Components with a different one which executes each task synchronously.
 *  https://developer.android.com/reference/android/arch/core/executor/testing/InstantTaskExecutorRule.html
 */
abstract class AACBaseTest {
    @Rule @JvmField val rule: TestRule = InstantTaskExecutorRule()
}
