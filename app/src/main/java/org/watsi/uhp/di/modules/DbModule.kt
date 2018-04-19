package org.watsi.uhp.di.modules

import android.arch.persistence.room.Room
import android.content.Context
import dagger.Module
import dagger.Provides
import org.threeten.bp.Clock
import org.watsi.device.db.AppDatabase
import org.watsi.device.db.daos.BillableDao
import org.watsi.device.db.daos.DeltaDao
import org.watsi.device.db.daos.EncounterDao
import org.watsi.device.db.repositories.BillableRepositoryImpl
import org.watsi.device.db.repositories.DeltaRepositoryImpl
import org.watsi.device.db.repositories.EncounterRepositoryImpl
import org.watsi.domain.repositories.BillableRepository
import org.watsi.domain.repositories.DeltaRepository
import org.watsi.domain.repositories.EncounterRepository
import org.watsi.uhp.repositories.DiagnosisRepository
import org.watsi.uhp.repositories.DiagnosisRepositoryImpl
import org.watsi.uhp.repositories.EncounterFormRepository
import org.watsi.uhp.repositories.EncounterFormRepositoryImpl
import org.watsi.uhp.repositories.IdentificationEventRepository
import org.watsi.uhp.repositories.IdentificationEventRepositoryImpl
import org.watsi.uhp.repositories.MemberRepository
import org.watsi.uhp.repositories.MemberRepositoryImpl
import org.watsi.uhp.repositories.PhotoRepository
import org.watsi.uhp.repositories.PhotoRepositoryImpl
import javax.inject.Singleton

@Module
class DbModule {

    @Singleton
    @Provides
    fun provideDatabase(context: Context): AppDatabase {
        val builder = Room.databaseBuilder(context, AppDatabase::class.java, "submission")
        builder.allowMainThreadQueries()
        return builder.build()
    }

    @Singleton
    @Provides
    fun provideBillableDao(db: AppDatabase): BillableDao = db.billableDao()

    @Singleton
    @Provides
    fun provideEncounterDao(db: AppDatabase): EncounterDao = db.encounterDao()

    @Singleton
    @Provides
    fun provideDeltaDao(db: AppDatabase): DeltaDao = db.deltaDao()

    @Provides
    fun provideBillableRepository(billableDao: BillableDao, clock: Clock): BillableRepository =
            BillableRepositoryImpl(billableDao, clock)

    @Provides
    fun provideEncounterRepository(encounterDao: EncounterDao, clock: Clock): EncounterRepository =
            EncounterRepositoryImpl(encounterDao, clock)

    @Provides
    fun provideDiagnosisRepository(db: DatabaseHelper): DiagnosisRepository =
            DiagnosisRepositoryImpl()

    @Provides
    fun provideIdentificationEventRepository(db: DatabaseHelper): IdentificationEventRepository =
            IdentificationEventRepositoryImpl()

    @Provides
    fun provideMemberRepository(db: DatabaseHelper): MemberRepository = MemberRepositoryImpl()

    @Provides
    fun provideEncounterFormRepository(db: DatabaseHelper): EncounterFormRepository =
            EncounterFormRepositoryImpl()

    @Provides
    fun providePhotoRepository(db: DatabaseHelper): PhotoRepository = PhotoRepositoryImpl()

    @Provides
    fun provideDeltaRepository(deltaDao: DeltaDao, clock: Clock): DeltaRepository {
        return DeltaRepositoryImpl(deltaDao, clock)
    }
}
