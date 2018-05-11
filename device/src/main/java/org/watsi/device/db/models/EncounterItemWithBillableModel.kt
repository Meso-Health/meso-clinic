package org.watsi.device.db.models

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation
import org.watsi.domain.relations.EncounterItemWithBillable

data class EncounterItemWithBillableModel(
        @Embedded var encounterItemModel: EncounterItemModel? = null,
        @Relation(parentColumn = "billableId", entityColumn = "id", entity = BillableModel::class)
        var billableModel: List<BillableModel>? = null) {

    fun toEncounterItemWithBillable(): EncounterItemWithBillable {
        encounterItemModel?.toEncounterItem()?.let { encounterItem ->
            billableModel?.firstOrNull()?.toBillable()?.let { billable ->
                return EncounterItemWithBillable(encounterItem, billable)
            }
            throw IllegalStateException("BillableModel cannot be null")
        }
        throw IllegalStateException("EncounterItemModel cannot be null")
    }
}
