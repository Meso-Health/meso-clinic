package org.watsi.uhp.di.modules

import android.arch.persistence.room.Room
import android.content.Context
import dagger.Module
import dagger.Provides
import org.watsi.device.db.AppDatabase
import javax.inject.Singleton

@Module
class DbModule {

    @Singleton
    @Provides
    fun provideDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "claims").build()
    }
}
