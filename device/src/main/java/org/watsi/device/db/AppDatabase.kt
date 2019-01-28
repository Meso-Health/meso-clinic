package org.watsi.device.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import org.watsi.device.db.daos.BillableDao
import org.watsi.device.db.daos.DeltaDao
import org.watsi.device.db.daos.DiagnosisDao
import org.watsi.device.db.daos.EncounterDao
import org.watsi.device.db.daos.EncounterFormDao
import org.watsi.device.db.daos.EncounterItemDao
import org.watsi.device.db.daos.IdentificationEventDao
import org.watsi.device.db.daos.MemberDao
import org.watsi.device.db.daos.PhotoDao
import org.watsi.device.db.daos.PriceScheduleDao
import org.watsi.device.db.models.BillableModel
import org.watsi.device.db.models.DeltaModel
import org.watsi.device.db.models.DiagnosisModel
import org.watsi.device.db.models.EncounterFormModel
import org.watsi.device.db.models.EncounterItemModel
import org.watsi.device.db.models.EncounterModel
import org.watsi.device.db.models.IdentificationEventModel
import org.watsi.device.db.models.MemberModel
import org.watsi.device.db.models.PhotoModel
import org.watsi.device.db.models.PriceScheduleModel

@Database(exportSchema = true, version = 6, entities = [
    BillableModel::class,
    DeltaModel::class,
    DiagnosisModel::class,
    EncounterModel::class,
    EncounterItemModel::class,
    EncounterFormModel::class,
    IdentificationEventModel::class,
    MemberModel::class,
    PhotoModel::class,
    PriceScheduleModel::class
])
@TypeConverters(TypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun billableDao(): BillableDao
    abstract fun deltaDao(): DeltaDao
    abstract fun diagnosisDao(): DiagnosisDao
    abstract fun encounterDao(): EncounterDao
    abstract fun encounterFormDao(): EncounterFormDao
    abstract fun encounterItemDao(): EncounterItemDao
    abstract fun identificationEventDao(): IdentificationEventDao
    abstract fun memberDao(): MemberDao
    abstract fun photoDao(): PhotoDao
    abstract fun priceScheduleDao(): PriceScheduleDao
}
