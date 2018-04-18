package org.watsi.uhp.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import org.watsi.uhp.database.DatabaseHelper
import org.watsi.uhp.repositories.BillableRepository
import org.watsi.uhp.repositories.BillableRepositoryImpl
import org.watsi.uhp.repositories.DiagnosisRepository
import org.watsi.uhp.repositories.DiagnosisRepositoryImpl
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
    fun provideDatabase(context: Context): DatabaseHelper {
        DatabaseHelper.init(context)
        return DatabaseHelper.getHelper()
    }

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
    fun provideMemberRepository(helper: DatabaseHelper): MemberRepository = MemberRepositoryImpl()

    @Singleton
    @Provides
    fun provideEncounterRepository(helper: DatabaseHelper): EncounterRepository =
            EncounterRepositoryImpl()

    @Singleton
    @Provides
    fun providePhotoRepository(helper: DatabaseHelper): PhotoRepository = PhotoRepositoryImpl()
}
