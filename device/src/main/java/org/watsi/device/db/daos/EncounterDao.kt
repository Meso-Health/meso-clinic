package org.watsi.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import io.reactivex.Single
import org.watsi.device.db.models.BillableModel
import org.watsi.device.db.models.DeltaModel
import org.watsi.device.db.models.EncounterFormModel
import org.watsi.device.db.models.EncounterItemModel
import org.watsi.device.db.models.EncounterModel
import java.util.UUID

@Dao
interface EncounterDao {

    @Query("SELECT * FROM encounters WHERE id = :id LIMIT 1")
    fun find(id: UUID): Single<EncounterModel>

    @Query("SELECT * FROM encounter_items WHERE encounterId = :encounterId")
    fun findEncounterItems(encounterId: UUID): Single<List<EncounterItemModel>>

    @Insert
    fun insert(encounter: EncounterModel,
               items: List<EncounterItemModel>,
               createdBillables: List<BillableModel>,
               forms: List<EncounterFormModel>,
               deltas: List<DeltaModel>)
}
