package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import me.xdrop.fuzzywuzzy.FuzzySearch
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
            val drugBillables = billablesByType[Billable.Type.DRUG].orEmpty()
            if (drugBillables.isEmpty()) {
                logger.warning("No Billables of type Drug loaded")
            } else {
                uniqueDrugNames = drugBillables.map { it.name }.distinct()
            }
        }, {
            logger.error(it)
        })
    }

    fun getObservable(encounter: EncounterWithItemsAndForms): LiveData<ViewState> {
        observable.value = ViewState(encounter = encounter)
        return observable
    }

    fun getSelectableBillables(
            type: Billable.Type?,
            currentEncounter: EncounterWithItemsAndForms? = currentEncounter()
    ) : List<Billable>  {
        val billableList = billablesByType[type].orEmpty()
        return when {
            billableList.isEmpty() -> {
                logger.warning("No Billables of type $type loaded")
                emptyList()
            }
            type == Billable.Type.DRUG -> emptyList()
            else -> {
                val currentBillables = currentEncounter?.billables().orEmpty()
                billableList.minus(currentBillables).sortedBy { it.name }
            }
        }
    }

    fun selectType(type: Billable.Type?) {
        observable.value = observable.value?.copy(
                type = type, selectableBillables = getSelectableBillables(type))
    }

    fun addItem(billable: Billable) {
        currentEncounter()?.let { encounter ->
            val updatedEncounterItems = encounter.encounterItems.toMutableList()
            val encounterItem = EncounterItem(UUID.randomUUID(), encounter.encounter.id, billable.id, 1)
            updatedEncounterItems.add(EncounterItemWithBillable(encounterItem, billable))
            val updatedEncounter = encounter.copy(encounterItems = updatedEncounterItems)
            observable.value = observable.value?.copy(
                    encounter = updatedEncounter,
                    selectableBillables = getSelectableBillables(observable.value?.type, updatedEncounter),
                    searchResults = emptyList()
            )
        }
    }

    fun removeItem(encounterItemId: UUID) {
        currentEncounter()?.let { encounter ->
            val updatedEncounterItems = encounter.encounterItems.toMutableList()
                    .filterNot { it.encounterItem.id == encounterItemId }
            val updatedEncounter = encounter.copy(encounterItems = updatedEncounterItems)
            observable.value = observable.value?.copy(
                    encounter = updatedEncounter,
                    selectableBillables = getSelectableBillables(observable.value?.type, updatedEncounter)
            )
        }
    }

    fun setItemQuantity(encounterItemId: UUID, quantity: Int) {
        currentEncounter()?.let { encounter ->
            val updatedEncounterItems = encounter.encounterItems.map { encounterItemWithBillable ->
                if (encounterItemWithBillable.encounterItem.id == encounterItemId) {
                    val oldEncounterItem = encounterItemWithBillable.encounterItem
                    val newEncounterItem = oldEncounterItem.copy(quantity = quantity)
                    encounterItemWithBillable.copy(encounterItem = newEncounterItem)
                } else {
                    encounterItemWithBillable
                }
            }
            val updatedEncounter = encounter.copy(encounterItems = updatedEncounterItems)
            observable.value = observable.value?.copy(encounter = updatedEncounter)
        }
    }

    fun updateQuery(query: String) {
        if (query.length > 2) {
            val currentDrugNames = currentEncounter()?.billables().orEmpty()
                    .filter { it.type == Billable.Type.DRUG }
                    .map { it.name }
            val selectableDrugNames = uniqueDrugNames.minus(currentDrugNames)
            val topMatchingNames = FuzzySearch.extractTop(query, selectableDrugNames, 5, 50)

            val drugBillables =  billablesByType[Billable.Type.DRUG].orEmpty()
            val matchingBillables = topMatchingNames.map { result ->
                drugBillables.filter { it.name == result.string }
            }.flatten().sortedBy { it.name }

            observable.value = observable.value?.copy(selectableBillables = matchingBillables)
        } else {
            observable.value = observable.value?.copy(selectableBillables = emptyList())
        }
    }

    fun currentEncounter(): EncounterWithItemsAndForms? {
        return observable.value?.encounter
    }

    data class ViewState(val type: Billable.Type? = null,
                         val selectableBillables: List<Billable> = emptyList(),
                         val encounter: EncounterWithItemsAndForms,
                         val searchResults: List<Billable> = emptyList())
}
