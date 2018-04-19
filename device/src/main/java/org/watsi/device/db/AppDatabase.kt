package org.watsi.device.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import org.watsi.device.db.daos.BillableDao
import org.watsi.device.db.daos.DeltaDao
import org.watsi.device.db.daos.EncounterDao
import org.watsi.device.db.models.BillableModel
import org.watsi.device.db.models.DeltaModel
import org.watsi.device.db.models.EncounterModel

@Database(exportSchema = true, version = 1, entities = [
    BillableModel::class,
    EncounterModel::class,
    DeltaModel::class
])
@TypeConverters(TypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun billableDao(): BillableDao
    abstract fun encounterDao(): EncounterDao
    abstract fun deltaDao(): DeltaDao
}
