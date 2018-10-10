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
    private val billableRelations: MutableList<BillableWithPriceSchedule> = mutableListOf()
    private var uniqueDrugNames: List<String> = emptyList()

    fun getObservable(encounterFlowState: EncounterFlowState): LiveData<ViewState> {
        loadBillablesOfTypeUseCase.execute(Billable.Type.DRUG).subscribe({
            billableRelations.addAll(it)
            uniqueDrugNames = billableRelations.map { it.billable.name }.distinct()
            observable.postValue(ViewState(selectableBillableRelations = getSelectableBillables(),
                encounterFlowState = encounterFlowState))
        }, {
            logger.error(it)
        })
        return observable
    }

    private fun getSelectableBillables(): List<BillableWithPriceSchedule>  {
        val encounterItems = observable.value?.encounterFlowState?.encounterItemRelations.orEmpty()
        val selectedBillables = encounterItems.map { it.billableWithPriceSchedule }
        return billableRelations.minus(selectedBillables).sortedBy { it.billable.name }
    }

    private fun updateEncounterItems(viewState: ViewState, encounterItemRelations: List<EncounterItemWithBillableAndPrice>) {
        viewState.encounterFlowState.encounterItemRelations = encounterItemRelations
        observable.value = viewState.copy(selectableBillableRelations = getSelectableBillables(),
                encounterFlowState = viewState.encounterFlowState)
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
                    billableRelations.filter { it.billable.name == result.string }.minus(currentDrugs).sortedBy { it.billable.details() }
                }.flatten()
                observable.postValue(observable.value?.copy(selectableBillableRelations = matchingBillables))
            } else {
                observable.postValue(observable.value?.copy(selectableBillableRelations = emptyList()))
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

    data class ViewState(val selectableBillableRelations: List<BillableWithPriceSchedule> = emptyList(),
                         val encounterFlowState: EncounterFlowState)
}
