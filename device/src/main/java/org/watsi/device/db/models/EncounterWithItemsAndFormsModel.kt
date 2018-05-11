package org.watsi.device.db.models

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation
import org.watsi.domain.relations.EncounterWithItemsAndForms

data class EncounterWithItemsAndFormsModel(
        @Embedded var encounterModel: EncounterModel? = null,
        @Relation(parentColumn = "id", entityColumn = "encounterId", entity = EncounterItemModel::class)
        var encounterItemModels: List<EncounterItemWithBillableModel>? = null,
        @Relation(parentColumn = "id", entityColumn = "encounterId", entity = EncounterFormModel::class)
        var encounterFormModels: List<EncounterFormModel>? = null) {

    fun toEncounterWithItemsAndForms(): EncounterWithItemsAndForms {
        encounterModel?.toEncounter()?.let { encounter ->
            encounterItemModels?.map{ it.toEncounterItemWithBillable() }?.let { items ->
                encounterFormModels?.map{ it.toEncounterForm() }?.let { forms ->
                    return EncounterWithItemsAndForms(encounter, items, forms)
                }
                throw IllegalStateException("EncounterFormModels cannot be null")
            }
            throw IllegalStateException("EncounterItemModels cannot be null")
        }
        throw IllegalStateException("EncounterModel cannot be null")
    }
}
