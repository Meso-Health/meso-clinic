package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.EncounterItem
import org.watsi.domain.relations.BillableWithPriceSchedule
import org.watsi.domain.relations.EncounterItemWithBillableAndPrice
import org.watsi.domain.usecases.LoadBillablesOfTypeUseCase
import org.watsi.uhp.flowstates.EncounterFlowState
import java.util.UUID
import javax.inject.Inject

class SpinnerLineItemViewModel @Inject constructor(
    private val loadBillablesOfTypeUseCase: LoadBillablesOfTypeUseCase,
    private val logger: Logger
) : ViewModel() {

    private val observable = MutableLiveData<ViewState>()
    private val billables: MutableList<BillableWithPriceSchedule> = mutableListOf()

    fun getObservable(
            encounterFlowState: EncounterFlowState,
            billableType: Billable.Type
    ): LiveData<ViewState> {
        loadBillablesOfTypeUseCase.execute(billableType).subscribe({
            billables.addAll(it)
            observable.postValue(ViewState(encounterFlowState = encounterFlowState))
        }, {
            logger.error(it)
        })
        return observable
    }

    fun getSelectableBillables(): List<BillableWithPriceSchedule>  {
        val encounterItems = observable.value?.encounterFlowState?.encounterItemRelations.orEmpty()
        val selectedBillables = encounterItems.map { it.billableWithPriceSchedule }
        return billables.minus(selectedBillables).sortedBy { it.billable.name }
    }

    private fun updateEncounterItems(viewState: ViewState, encounterItemRelations: List<EncounterItemWithBillableAndPrice>) {
        observable.value = viewState.copy(encounterFlowState = viewState.encounterFlowState.copy(encounterItemRelations = encounterItemRelations))
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

    data class ViewState(val encounterFlowState: EncounterFlowState)
}
