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
import org.watsi.domain.relations.BillableWithPriceSchedule
import org.watsi.domain.relations.EncounterItemWithBillableAndPrice
import org.watsi.domain.usecases.LoadBillablesOfTypeUseCase
import org.watsi.uhp.flowstates.EncounterFlowState
import java.util.UUID
import javax.inject.Inject

class DrugAndSupplyViewModel @Inject constructor(
    private val loadBillablesOfTypeUseCase: LoadBillablesOfTypeUseCase,
    private val logger: Logger
) : ViewModel() {

    private val observable = MutableLiveData<ViewState>()
    private val billables: MutableList<BillableWithPriceSchedule> = mutableListOf()
    private var uniqueDrugNames: List<String> = emptyList()

    fun getObservable(encounterFlowState: EncounterFlowState): LiveData<ViewState> {
        loadBillablesOfTypeUseCase.execute(Billable.Type.DRUG).subscribe({
            billables.addAll(it)
            uniqueDrugNames = billables.map { it.billable.name }.distinct()
            observable.postValue(ViewState(selectableBillables = getSelectableBillables(),
                encounterFlowState = encounterFlowState))
        }, {
            logger.error(it)
        })
        return observable
    }

    private fun getSelectableBillables(): List<BillableWithPriceSchedule>  {
        val encounterItems = observable.value?.encounterFlowState?.encounterItemRelations.orEmpty()
        val selectedBillables = encounterItems.map { it.billableWithPriceSchedule }
        return billables.minus(selectedBillables).sortedBy { it.billable.name }
    }

    private fun updateEncounterItems(viewState: ViewState, encounterItemRelations: List<EncounterItemWithBillableAndPrice>) {
        observable.value = viewState.copy(selectableBillables = getSelectableBillables(),
                encounterFlowState = viewState.encounterFlowState.copy(encounterItemRelations = encounterItemRelations))
    }

    fun updateQuery(query: String) {
        Completable.fromCallable {
            if (query.length > 2) {
                val currentDrugs = getEncounterFlowState()
                    ?.getEncounterItemsOfType(Billable.Type.DRUG)
                    ?.map { it.billableWithPriceSchedule }.orEmpty()
                val selectableDrugNames = uniqueDrugNames
                val topMatchingNames = FuzzySearch.extractTop(query, selectableDrugNames, 5, 50)

                val matchingBillables = topMatchingNames.sortedWith(Comparator { o1, o2 ->
                    if (o2.score == o1.score)
                        o1.string.compareTo(o2.string)
                    else
                        Integer.compare(o2.score, o1.score)
                }).map { result ->
                    billables.filter { it.billable.name == result.string }.minus(currentDrugs).sortedBy { it.billable.details() }
                }.flatten()
                observable.postValue(observable.value?.copy(selectableBillables = matchingBillables))
            } else {
                observable.postValue(observable.value?.copy(selectableBillables = emptyList()))
            }
        }.subscribeOn(Schedulers.computation()).subscribe()
    }

    fun addItem(billableWithPrice: BillableWithPriceSchedule) {
        observable.value?.let { viewState ->
            val encounterState = viewState.encounterFlowState
            val updatedEncounterItems = encounterState.encounterItemRelations.toMutableList()
            val encounterItem = EncounterItem(
                UUID.randomUUID(),
                encounterState.encounter.id,
                billableWithPrice.billable.id,
                1,
                billableWithPrice.priceSchedule.id,
                false
            )
            updatedEncounterItems.add(
                EncounterItemWithBillableAndPrice(
                    encounterItem,
                    billableWithPrice
                )
            )
            updateEncounterItems(viewState, updatedEncounterItems)
        }
    }

    fun removeItem(encounterItemId: UUID) {
        observable.value?.let { viewState ->
            val encounterState = viewState.encounterFlowState
            val updatedEncounterItems = encounterState.encounterItemRelations.toMutableList()
                    .filterNot { it.encounterItem.id == encounterItemId }
            updateEncounterItems(viewState, updatedEncounterItems)
        }
    }

    fun setItemQuantity(encounterItemId: UUID, quantity: Int) {
        observable.value?.let { viewState ->
            val updatedEncounterItems = viewState.encounterFlowState.encounterItemRelations
                    .map { encounterItemRelation ->
                        if (encounterItemRelation.encounterItem.id == encounterItemId) {
                            val oldEncounterItem = encounterItemRelation.encounterItem
                            val newEncounterItem = oldEncounterItem.copy(quantity = quantity)
                            encounterItemRelation.copy(encounterItem = newEncounterItem)
                        } else {
                            encounterItemRelation
                        }
                    }
            updateEncounterItems(viewState, updatedEncounterItems)
        }
    }

    fun getEncounterFlowState(): EncounterFlowState? = observable.value?.encounterFlowState

    data class ViewState(val selectableBillables: List<BillableWithPriceSchedule> = emptyList(),
                         val encounterFlowState: EncounterFlowState)
}
