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
    lateinit var billableDao: BillableDao
    lateinit var deltaDao: DeltaDao
    lateinit var diagnosisDao: DiagnosisDao
    lateinit var encounterDao: EncounterDao
    lateinit var encounterItemDao: EncounterItemDao
    lateinit var encounterFormDao: EncounterFormDao
    lateinit var identificationEventDao: IdentificationEventDao
    lateinit var memberDao: MemberDao
    lateinit var photoDao: PhotoDao
    lateinit var priceScheduleDao: PriceScheduleDao

    // Instantly execute all DB operations in Dao tests
    @Rule
    @JvmField val rule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setUpDatabase() {
        database = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.application.baseContext, AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        billableDao = database.billableDao()
        deltaDao = database.deltaDao()
        diagnosisDao = database.diagnosisDao()
        encounterDao = database.encounterDao()
        encounterFormDao = database.encounterFormDao()
        encounterItemDao = database.encounterItemDao()
        identificationEventDao = database.identificationEventDao()
        memberDao = database.memberDao()
        photoDao = database.photoDao()
        priceScheduleDao = database.priceScheduleDao()
    }

    @After
    fun tearDown() {
        database.close()
    }
}
