package org.watsi.device.db.daos

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.persistence.room.Room
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.watsi.device.db.AppDatabase

@RunWith(RobolectricTestRunner::class)
abstract class DaoBaseTest {
    lateinit var database: AppDatabase
    lateinit var deltaDao: DeltaDao
    lateinit var diagnosisDao: DiagnosisDao
    lateinit var encounterDao: EncounterDao
    lateinit var encounterFormDao: EncounterFormDao
    lateinit var identificationEventDao: IdentificationEventDao
    lateinit var memberDao: MemberDao
    lateinit var photoDao: PhotoDao

    // Instantly execute all DB operations in Dao tests
    @Rule
    @JvmField val rule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setUpDatabase() {
        database = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.application.baseContext, AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        deltaDao = database.deltaDao()
        diagnosisDao = database.diagnosisDao()
        encounterDao = database.encounterDao()
        encounterFormDao = database.encounterFormDao()
        identificationEventDao = database.identificationEventDao()
        memberDao = database.memberDao()
        photoDao = database.photoDao()
    }

    @After
    fun tearDown() {
        database.close()
    }
}
