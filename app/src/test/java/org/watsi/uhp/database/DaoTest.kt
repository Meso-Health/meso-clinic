package org.watsi.uhp.database

import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
abstract class DaoTest {
    @Before
    fun setup() {
        DatabaseHelper.init(RuntimeEnvironment.application)
    }

    @After
    fun tearDown() {
        DatabaseHelper.reset()
    }

}