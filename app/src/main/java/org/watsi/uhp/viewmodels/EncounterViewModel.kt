package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import me.xdrop.fuzzywuzzy.FuzzySearch
import org.threeten.bp.Instant
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.EncounterItem
import org.watsi.domain.relations.EncounterItemWithBillable
import org.watsi.domain.relations.EncounterWithItemsAndForms
import org.watsi.domain.repositories.BillableRepository
import java.util.UUID
import javax.inject.Inject

class EncounterViewModel @Inject constructor(
        billableRepository: BillableRepository,
        private val logger: Logger
) : ViewModel() {

    private val observable = MutableLiveData<ViewState>()
    private var billablesByType: Map<Billable.Type, List<Billable>> = emptyMap()
    private var uniqueDrugNames: List<String> = emptyList()

    init {
        billableRepository.all().subscribe({
            billablesByType = it.groupBy { it.type }
            if (billablesByType[Billable.Type.DRUG]?.isEmpty() == true) {
                logger.warning("No Billables of type Drug loaded")
            } else {
                uniqueDrugNames = billablesByType[Billable.Type.DRUG]!!.map { it.name }.distinct()
            }
        }, {
            logger.error(it)
        })
    }

    fun getObservable(encounter: EncounterWithItemsAndForms): LiveData<ViewState> {
        observable.value = ViewState(encounter = encounter)
        return observable
    }

    fun selectType(type: Billable.Type?) {
        val selectableBillables = if (type != null && type != Billable.Type.DRUG) {
            val encounterItems = currentEncounter()?.encounterItems
            val currentBillables = encounterItems?.map { it.billable } ?: emptyList()
            billablesByType[type]!!.filter {  it -> !currentBillables.contains(it) }.sortedBy { it.name }
        } else {
            emptyList()
        }
        observable.value = observable.value?.copy(
                type = type, selectableBillables = selectableBillables)
    }

    fun addItem(billable: Billable) {
        currentEncounter()?.let {
            if (it.containsBillable(billable.id)) {
                throw DuplicateBillableException()
            } else {
                val updatedEncounterItems = it.encounterItems.toMutableList()
                val encounterItem = EncounterItem(UUID.randomUUID(), it.encounter.id, billable.id, 1)
                updatedEncounterItems.add(EncounterItemWithBillable(encounterItem, billable))
                val updatedEncounter = it.copy(encounterItems = updatedEncounterItems)
                observable.value = observable.value?.copy(
                        encounter = updatedEncounter,
                        type = null,
                        selectableBillables = emptyList(),
                        searchResults = emptyList()
                )
            }
        }
    }

    fun setItemQuantity(encounterItemId: UUID, quantity: String) {
        currentEncounter()?.let { encounter ->
            if (quantity in listOf("", "0")) {
                throw InvalidQuantityException()
            } else {
                val updatedEncounterItems = encounter.encounterItems.toMutableList()
                updatedEncounterItems.find { it.encounterItem.id == encounterItemId }?.let { encounterItemRelation ->
                    encounterItemRelation.encounterItem.quantity = quantity.toInt()
                }
                val updatedEncounter = encounter.copy(encounterItems = updatedEncounterItems)
                observable.value = observable.value?.copy(encounter = updatedEncounter)
            }
        }
    }

    fun removeItem(encounterItemId: UUID) {
        currentEncounter()?.let { encounter ->
            val updatedEncounterItems = encounter.encounterItems.toMutableList()
                    .filterNot { it.encounterItem.id == encounterItemId }
            val updatedEncounter = encounter.copy(encounterItems = updatedEncounterItems)
            observable.value = observable.value?.copy(encounter = updatedEncounter)
        }
    }

    fun updateQuery(query: String) {
        if (query.length > 2) {
            val topMatchingNames = FuzzySearch.extractTop(query, uniqueDrugNames, 5, 50)

            val matchingBillables = topMatchingNames.map { result ->
                billablesByType[Billable.Type.DRUG]!!.filter { it.name == result.string }
            }.flatten().sortedBy { it.name }

            observable.value = observable.value?.copy(selectableBillables = matchingBillables)
        } else {
            observable.value = observable.value?.copy(selectableBillables = emptyList())
        }
    }

    fun updateBackdatedOccurredAt(instant: Instant) {
        currentEncounter()?.let {
            val updatedEncounter = it.encounter.copy(occurredAt = instant,
                                                     backdatedOccurredAt = true)
            observable.value = observable.value?.copy(
                    encounter = it.copy(encounter = updatedEncounter))
        }
    }

    fun currentEncounter(): EncounterWithItemsAndForms? {
        return observable.value?.encounter
    }

    data class ViewState(val type: Billable.Type? = null,
                         val selectableBillables: List<Billable> = emptyList(),
                         val encounter: EncounterWithItemsAndForms,
                         val searchResults: List<Billable> = emptyList())

    class DuplicateBillableException : Exception()
    class InvalidQuantityException : Exception()
}
