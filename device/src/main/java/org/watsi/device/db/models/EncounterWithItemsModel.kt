package org.watsi.device.db.models

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation
import org.watsi.domain.relations.EncounterWithItems

data class EncounterWithItemsModel(
        @Embedded var encounterModel: EncounterModel? = null,
        @Relation(parentColumn = "id", entityColumn = "encounterId", entity = EncounterItemModel::class)
        var encounterItemModels: List<EncounterItemModel>? = null) {

    fun toEncounterWithItems(): EncounterWithItems {
        encounterModel?.toEncounter()?.let { encounter ->
            encounterItemModels?.map{ it.toEncounterItem() }?.let { items ->
                return EncounterWithItems(encounter, items)
            }
            throw IllegalStateException("EncounterItemModels cannot be null")
        }
        throw IllegalStateException("EncounterModel cannot be null")
    }
}
