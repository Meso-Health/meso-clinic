package org.watsi.device.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import org.watsi.device.db.daos.DeltaDao
import org.watsi.device.db.models.DeltaModel

@Database(exportSchema = true, version = 1, entities = [
    DeltaModel::class
])
@TypeConverters(TypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deltaDao(): DeltaDao
}
