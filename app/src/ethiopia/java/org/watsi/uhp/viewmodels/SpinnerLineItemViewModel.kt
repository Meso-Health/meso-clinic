package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.EncounterItem
import org.watsi.domain.relations.BillableWithPriceSchedule
import org.watsi.domain.relations.EncounterItemWithBillableAndPrice
import org.watsi.domain.repositories.BillableRepository
import org.watsi.uhp.flowstates.EncounterFlowState
import java.util.UUID
import javax.inject.Inject

class SpinnerLineItemViewModel @Inject constructor(
        private val billableRepository: BillableRepository, // TODO: Replace this with a use case to fetch List<BillableWithPriceSchedule>
        private val logger: Logger
) : ViewModel() {

    private val observable = MutableLiveData<ViewState>()
    private val billables: MutableList<Billable> = mutableListOf()

    fun getObservable(
            encounterFlowState: EncounterFlowState,
            billableType: Billable.Type
    ): LiveData<ViewState> {
        billableRepository.ofType(billableType).subscribe({
            billables.addAll(it)
            observable.postValue(ViewState(selectableBillables = getSelectableBillables(),
                    encounterFlowState = encounterFlowState))
        }, {
            logger.error(it)
        })
        return observable
    }

    private fun getSelectableBillables(): List<Billable>  {
        val encounterItems = observable.value?.encounterFlowState?.encounterItemRelations.orEmpty()
        val selectedBillables = encounterItems.map { it.billableWithPriceSchedule.billable }
        return billables.minus(selectedBillables).sortedBy { it.name }
    }

    private fun updateEncounterItems(viewState: ViewState, encounterItemRelations: List<EncounterItemWithBillableAndPrice>) {
        observable.value = viewState.copy(selectableBillables = getSelectableBillables(),
                encounterFlowState = viewState.encounterFlowState.copy(encounterItemRelations = encounterItemRelations))
    }

    fun addItem(billableWithPrice: BillableWithPriceSchedule) { // TODO: Add price schedule
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
            updatedEncounterItems.add(EncounterItemWithBillableAndPrice(encounterItem, billableWithPrice))
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
