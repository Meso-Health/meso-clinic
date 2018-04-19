package org.watsi.device.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import org.watsi.device.db.daos.BillableDao
import org.watsi.device.db.daos.DeltaDao
import org.watsi.device.db.daos.DiagnosisDao
import org.watsi.device.db.daos.EncounterDao
import org.watsi.device.db.daos.EncounterFormDao
import org.watsi.device.db.models.BillableModel
import org.watsi.device.db.models.DeltaModel
import org.watsi.device.db.models.DiagnosisModel
import org.watsi.device.db.models.EncounterFormModel
import org.watsi.device.db.models.EncounterModel

@Database(exportSchema = true, version = 1, entities = [
    BillableModel::class,
    DeltaModel::class,
    DiagnosisModel::class,
    EncounterModel::class,
    EncounterFormModel::class
])
@TypeConverters(TypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun billableDao(): BillableDao
    abstract fun deltaDao(): DeltaDao
    abstract fun diagnosisDao(): DiagnosisDao
    abstract fun encounterDao(): EncounterDao
    abstract fun encounterFormDao(): EncounterFormDao
}
