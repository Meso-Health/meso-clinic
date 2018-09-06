package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import me.xdrop.fuzzywuzzy.FuzzySearch
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.EncounterItem
import org.watsi.domain.relations.EncounterItemWithBillableAndPrice
import org.watsi.domain.repositories.BillableRepository
import org.watsi.uhp.flowstates.EncounterFlowState
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

    fun getObservable(
        encounterId: UUID,
        encounterItemRelations: List<EncounterItemWithBillableAndPrice>
    ): LiveData<ViewState> {
        observable.value = ViewState(encounterItemRelations = encounterItemRelations, encounterId = encounterId)
        return observable
    }


    fun getSelectableBillables(
        type: Billable.Type?,
        encounterItemRelations: List<EncounterItemWithBillableAndPrice>? = currentEncounterItems()
    ): List<Billable>  {
        val billableList = billablesByType[type].orEmpty()
        return when {
            billableList.isEmpty() -> {
                logger.warning("No Billables of type $type loaded")
                emptyList()
            }
            type == Billable.Type.DRUG -> emptyList()
            else -> {
                val currentBillables =
                    encounterItemRelations.orEmpty().map { it.billableWithPriceSchedule.billable }
                billableList.minus(currentBillables).sortedBy { it.name }
            }
        }
    }

    fun selectType(type: Billable.Type?) {
        observable.value = observable.value?.copy(
                type = type, selectableBillables = getSelectableBillables(type))
    }

    fun addItem(billable: Billable) { // TODO: Add price schedule with new billable
        observable.value?.let { viewState ->
            val updatedEncounterItems = viewState.encounterItemRelations.toMutableList()
            val encounterItem = EncounterItem(UUID.randomUUID(), viewState.encounterId, billable.id, 1)
            updatedEncounterItems.add(EncounterItemWithBillableAndPrice(encounterItem, billable))
            observable.value = viewState.copy(
                    encounterItemRelations = updatedEncounterItems,
                    selectableBillables = getSelectableBillables(viewState.type, updatedEncounterItems)
            )
        }
    }

    fun removeItem(encounterItemId: UUID) {
        observable.value?.let { viewState ->
            val updatedEncounterItems = viewState.encounterItemRelations.toMutableList()
                    .filterNot { it.encounterItem.id == encounterItemId }
            observable.value = viewState.copy(encounterItemRelations = updatedEncounterItems,
                    selectableBillables = getSelectableBillables(viewState.type, updatedEncounterItems))
        }
    }

    fun setItemQuantity(encounterItemId: UUID, quantity: Int) {
        observable.value?.let { viewState ->
            val updatedEncounterItems = viewState.encounterItemRelations.map { encounterItemRelation ->
                if (encounterItemRelation.encounterItem.id == encounterItemId) {
                    val oldEncounterItem = encounterItemRelation.encounterItem
                    val newEncounterItem = oldEncounterItem.copy(quantity = quantity)
                    encounterItemRelation.copy(encounterItem = newEncounterItem)
                } else {
                    encounterItemRelation
                }
            }
            observable.value = viewState.copy(encounterItemRelations = updatedEncounterItems)
        }
    }

    fun updateQuery(query: String) {
        Completable.fromCallable {
            if (query.length > 2) {
                val currentDrugs = currentEncounterItems().orEmpty()
                        .map { it.billableWithPriceSchedule.billable }
                        .filter { it.type == Billable.Type.DRUG }
                val selectableDrugNames = uniqueDrugNames
                val topMatchingNames = FuzzySearch.extractTop(query, selectableDrugNames, 5, 50)

                val drugBillables =  billablesByType[Billable.Type.DRUG].orEmpty()

                val matchingBillables = topMatchingNames.sortedWith(Comparator { o1, o2 ->
                    if (o2.score == o1.score)
                        o1.string.compareTo(o2.string)
                    else
                        Integer.compare(o2.score, o1.score)
                }).map { result ->
                    drugBillables.filter { it.name == result.string }.minus(currentDrugs).sortedBy { it.details() }
                }.flatten()
                observable.postValue(observable.value?.copy(selectableBillables = matchingBillables))
            } else {
                observable.postValue(observable.value?.copy(selectableBillables = emptyList()))
            }
        }.subscribeOn(Schedulers.computation()).subscribe()
    }

    fun currentEncounterItems(): List<EncounterItemWithBillableAndPrice>? {
        return observable.value?.encounterItemRelations
    }

    fun updateEncounterWithLineItems(encounterFlowState: EncounterFlowState) {
        encounterFlowState.encounterItemRelations = observable.value?.encounterItemRelations.orEmpty()
    }

    data class ViewState(val type: Billable.Type? = null,
                         val selectableBillables: List<Billable> = emptyList(),
                         val encounterItemRelations: List<EncounterItemWithBillableAndPrice>,
                         val encounterId: UUID)
}
