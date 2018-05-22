package org.watsi.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.arch.persistence.room.Transaction
import io.reactivex.Single
import org.watsi.device.db.models.BillableModel
import org.watsi.device.db.models.DeltaModel
import org.watsi.device.db.models.EncounterFormModel
import org.watsi.device.db.models.EncounterItemModel
import org.watsi.device.db.models.EncounterModel
import org.watsi.device.db.models.EncounterWithItemsAndFormsModel
import java.util.UUID

@Dao
interface EncounterDao {

    @Transaction
    @Query("SELECT * FROM encounters WHERE id = :id LIMIT 1")
    fun find(id: UUID): Single<EncounterWithItemsAndFormsModel>

    @Insert
    fun insert(encounter: EncounterModel,
               items: List<EncounterItemModel>,
               createdBillables: List<BillableModel>,
               forms: List<EncounterFormModel>,
               deltas: List<DeltaModel>)
}
