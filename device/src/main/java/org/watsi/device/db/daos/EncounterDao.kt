package org.watsi.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import org.watsi.device.db.models.BillableModel
import org.watsi.device.db.models.DeltaModel
import org.watsi.device.db.models.EncounterFormModel
import org.watsi.device.db.models.EncounterItemModel
import org.watsi.device.db.models.EncounterModel

@Dao
interface EncounterDao {

    @Insert
    fun insert(encounters: EncounterModel,
               items: List<EncounterItemModel>,
               createdBillables: List<BillableModel>,
               forms: List<EncounterFormModel>)

    @Insert
    fun insertWithDeltas(encounter: EncounterModel,
                         items: List<EncounterItemModel>,
                         createdBillables: List<BillableModel>,
                         forms: List<EncounterFormModel>,
                         deltas: List<DeltaModel>)
}
