package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.EncounterItem
import org.watsi.domain.relations.EncounterItemWithBillable
import org.watsi.domain.repositories.BillableRepository
import org.watsi.uhp.flowstates.EncounterFlowState
import java.util.UUID
import javax.inject.Inject

class SpinnerLineItemViewModel @Inject constructor(
        private val billableRepository: BillableRepository,
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
        val encounterItems = observable.value?.encounterFlowState?.encounterItems.orEmpty()
        val selectedBillables = encounterItems.map { it.billable }
        return billables.minus(selectedBillables).sortedBy { it.name }
    }

    private fun updateEncounterItems(viewState: ViewState, encounterItems: List<EncounterItemWithBillable>) {
        observable.value = viewState.copy(selectableBillables = getSelectableBillables(),
                encounterFlowState = viewState.encounterFlowState.copy(encounterItems = encounterItems))
    }

    fun addItem(billable: Billable) {
        observable.value?.let { viewState ->
            val encounterState = viewState.encounterFlowState
            val updatedEncounterItems = encounterState.encounterItems.toMutableList()
            val encounterItem = EncounterItem(
                    UUID.randomUUID(), encounterState.encounter.id, billable.id, 1)
            updatedEncounterItems.add(EncounterItemWithBillable(encounterItem, billable))
            updateEncounterItems(viewState, updatedEncounterItems)
        }
    }

    fun removeItem(encounterItemId: UUID) {
        observable.value?.let { viewState ->
            val encounterState = viewState.encounterFlowState
            val updatedEncounterItems = encounterState.encounterItems.toMutableList()
                    .filterNot { it.encounterItem.id == encounterItemId }
            updateEncounterItems(viewState, updatedEncounterItems)
        }
    }

    fun setItemQuantity(encounterItemId: UUID, quantity: Int) {
        observable.value?.let { viewState ->
            val updatedEncounterItems = viewState.encounterFlowState.encounterItems
                    .map { encounterItemWithBillable ->
                if (encounterItemWithBillable.encounterItem.id == encounterItemId) {
                    val oldEncounterItem = encounterItemWithBillable.encounterItem
                    val newEncounterItem = oldEncounterItem.copy(quantity = quantity)
                    encounterItemWithBillable.copy(encounterItem = newEncounterItem)
                } else {
                    encounterItemWithBillable
                }
            }
            updateEncounterItems(viewState, updatedEncounterItems)
        }
    }

    fun getEncounterFlowState(): EncounterFlowState? = observable.value?.encounterFlowState

    data class ViewState(val selectableBillables: List<Billable> = emptyList(),
                         val encounterFlowState: EncounterFlowState)
}
