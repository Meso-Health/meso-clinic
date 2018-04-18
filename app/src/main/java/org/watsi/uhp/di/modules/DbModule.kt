package org.watsi.uhp.di.modules

import android.arch.persistence.room.Room
import android.content.Context
import dagger.Module
import dagger.Provides
import org.threeten.bp.Clock
import org.watsi.device.db.AppDatabase
import org.watsi.device.db.daos.DeltaDao
import org.watsi.device.db.repositories.DeltaRepositoryImpl
import org.watsi.domain.repositories.DeltaRepository
import org.watsi.uhp.database.DatabaseHelper
import org.watsi.uhp.repositories.BillableRepository
import org.watsi.uhp.repositories.BillableRepositoryImpl
import org.watsi.uhp.repositories.DiagnosisRepository
import org.watsi.uhp.repositories.DiagnosisRepositoryImpl
import org.watsi.uhp.repositories.EncounterFormRepository
import org.watsi.uhp.repositories.EncounterFormRepositoryImpl
import org.watsi.uhp.repositories.EncounterRepository
import org.watsi.uhp.repositories.EncounterRepositoryImpl
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
    fun provideHelper(context: Context): DatabaseHelper {
        DatabaseHelper.init(context)
        return DatabaseHelper.getHelper()
    }

    @Singleton
    @Provides
    fun provideDatabase(context: Context): AppDatabase {
        val builder = Room.databaseBuilder(context, AppDatabase::class.java, "submission")
        builder.allowMainThreadQueries()
        return builder.build()
    }

    @Singleton
    @Provides
    fun provideDeltaDao(db: AppDatabase): DeltaDao = db.deltaDao()

    @Singleton
    @Provides
    fun provideBillableRepository(db: DatabaseHelper): BillableRepository = BillableRepositoryImpl()

    @Singleton
    @Provides
    fun provideDiagnosisRepository(db: DatabaseHelper): DiagnosisRepository =
            DiagnosisRepositoryImpl()

    @Singleton
    @Provides
    fun provideIdentificationEventRepository(db: DatabaseHelper): IdentificationEventRepository =
            IdentificationEventRepositoryImpl()

    @Singleton
    @Provides
    fun provideMemberRepository(db: DatabaseHelper): MemberRepository = MemberRepositoryImpl()

    @Singleton
    @Provides
    fun provideEncounterRepository(db: DatabaseHelper): EncounterRepository =
            EncounterRepositoryImpl()

    @Singleton
    @Provides
    fun provideEncounterFormRepository(db: DatabaseHelper): EncounterFormRepository =
            EncounterFormRepositoryImpl()

    @Singleton
    @Provides
    fun providePhotoRepository(db: DatabaseHelper): PhotoRepository = PhotoRepositoryImpl()

    @Singleton
    @Provides
    fun provideDeltaRepository(deltaDao: DeltaDao, clock: Clock): DeltaRepository {
        return DeltaRepositoryImpl(deltaDao, clock)
    }
}
