package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.EncounterItem
import org.watsi.domain.entities.LabResult
import org.watsi.domain.relations.BillableWithPriceSchedule
import org.watsi.domain.relations.EncounterItemWithBillableAndPrice
import org.watsi.domain.usecases.LoadAllBillablesTypesUseCase
import org.watsi.domain.usecases.LoadAllBillablesUseCase
import org.watsi.uhp.flowstates.EncounterFlowState
import org.watsi.uhp.utils.FuzzySearchUtil
import java.util.UUID
import javax.inject.Inject

class EncounterViewModel @Inject constructor(
    val loadAllBillablesUseCase: LoadAllBillablesUseCase,
    val loadAllBillableTypesUseCase: LoadAllBillablesTypesUseCase,
    private val logger: Logger
) : ViewModel() {

    private val observable = MutableLiveData<ViewState>()
    private var billablesByType: Map<Billable.Type, List<BillableWithPriceSchedule>> = emptyMap()
    private var uniqueDrugNames: List<String> = emptyList()

    fun getObservable(encounterFlowState: EncounterFlowState): LiveData<ViewState> {
        loadAllBillablesUseCase.execute().subscribe({
            billablesByType = it.groupBy { it.billable.type }
            val drugBillables = billablesByType[Billable.Type.DRUG].orEmpty()
            if (drugBillables.isEmpty()) {
                logger.warning("No Billables of type Drug loaded")
            } else {
                uniqueDrugNames = drugBillables.map { it.billable.name }.distinct()
            }
            observable.postValue(
                ViewState(
                    encounterFlowState = encounterFlowState
                )
            )
        }, {
            logger.error(it)
        })
        return observable
    }

    fun getBillableTypeObservable(): LiveData<List<Billable.Type>> {
        return LiveDataReactiveStreams.fromPublisher(loadAllBillableTypesUseCase.execute())
    }

    fun getSelectableBillables(
        type: Billable.Type?
    ): List<BillableWithPriceSchedule>  {
        val billableList = billablesByType[type].orEmpty()
        return when {
            billableList.isEmpty() -> {
                logger.warning("No Billables of type $type loaded")
                emptyList()
            }
            type == Billable.Type.DRUG -> emptyList()
            else -> {
                billableList.sortedBy { it.billable.name }
            }
        }
    }

    fun selectType(type: Billable.Type?) {
        observable.value = observable.value?.copy(
            type = type,
            selectableBillables = getSelectableBillables(type)
        )
    }


    fun onSelectedBillable(billableWithPriceSchedule: BillableWithPriceSchedule?) {
        billableWithPriceSchedule?.let {
            // Set the billableWithPriceSchedule if the billable requires lab result.
            if (it.billable.requiresLabResult) {
                observable.value = observable.value?.copy(
                    billableWithPriceSchedule = it
                )
            } else {
                // Add item if it does not require lab result.
                addItem(
                    billableWithPrice = it,
                    labResultValue = null
                )
            }
        }
    }

    fun onLabResultChange(labResultValue: String) {
        observable.value?.billableWithPriceSchedule?.let { billableWithPrice ->
            addItem(
                billableWithPrice = billableWithPrice,
                labResultValue = labResultValue
            )
        }
    }

    fun requiresLabResult(): Boolean {
        return observable.value?.billableWithPriceSchedule?.billable?.requiresLabResult ?: false
    }

    fun addItem(billableWithPrice: BillableWithPriceSchedule, labResultValue: String? = null) {
        observable.value?.let { viewState ->
            val encounterState = viewState.encounterFlowState
            val updatedEncounterItems = encounterState.encounterItemRelations.toMutableList()
            val encounterItem = EncounterItem(
                id = UUID.randomUUID(),
                encounterId = encounterState.encounter.id,
                quantity = 1,
                priceScheduleId = billableWithPrice.priceSchedule.id,
                priceScheduleIssued = false
            )

            val labResult = labResultValue?.let {
                LabResult(
                    id = UUID.randomUUID(),
                    encounterItemId = encounterItem.id,
                    result = labResultValue
                )
            }
            updatedEncounterItems.add(
                EncounterItemWithBillableAndPrice(
                    encounterItem,
                    billableWithPrice,
                    labResult
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

    fun setSurgicalScore(encounterItemId: UUID, score: Int?) {
        observable.value?.let { viewState ->
            val updatedEncounterItems = viewState.encounterFlowState.encounterItemRelations
                    .map { encounterItemRelation ->
                        if (encounterItemRelation.encounterItem.id == encounterItemId) {
                            val oldEncounterItem = encounterItemRelation.encounterItem
                            val newEncounterItem = oldEncounterItem.copy(surgicalScore = score)
                            encounterItemRelation.copy(encounterItem = newEncounterItem)
                        } else {
                            encounterItemRelation
                        }
                    }
            updateEncounterItems(viewState, updatedEncounterItems)
        }
    }

    fun getSurgicalScore(encounterItemId: UUID): Int? {
        return observable.value?.let { viewState ->
            viewState.encounterFlowState.encounterItemRelations
                    .find { it.encounterItem.id == encounterItemId }?.encounterItem?.surgicalScore
        } ?: null
    }

    private fun updateEncounterItems(viewState: ViewState, encounterItemRelations: List<EncounterItemWithBillableAndPrice>) {
        viewState.encounterFlowState.encounterItemRelations = encounterItemRelations
        observable.value = viewState.copy(
            encounterFlowState = viewState.encounterFlowState,
            billableWithPriceSchedule = null
        )
    }

    fun updateQuery(query: String) {
        Completable.fromCallable {
            if (query.length > 2) {
                val currentDrugs = currentEncounterItems().orEmpty()
                        .map { it.billableWithPriceSchedule }
                        .filter { it.billable.type == Billable.Type.DRUG }
                val selectableDrugNames = uniqueDrugNames
                val topMatchingNames = FuzzySearchUtil.topMatches(query, selectableDrugNames, 5)

                val drugBillables =  billablesByType[Billable.Type.DRUG].orEmpty()

                val matchingBillables = topMatchingNames
                .map { result ->
                    drugBillables.filter { it.billable.name == result }.minus(currentDrugs).sortedBy { it.billable.details() }
                }.flatten()
                observable.postValue(observable.value?.copy(selectableBillables = matchingBillables))
            } else {
                observable.postValue(observable.value?.copy(selectableBillables = emptyList()))
            }
        }.subscribeOn(Schedulers.computation()).subscribe()
    }

    fun currentEncounterItems(): List<EncounterItemWithBillableAndPrice>? {
        return observable.value?.encounterFlowState?.encounterItemRelations
    }

    fun updateEncounterWithLineItems(encounterFlowState: EncounterFlowState) {
        encounterFlowState.encounterItemRelations = observable.value?.encounterFlowState?.encounterItemRelations.orEmpty()
    }

    fun getEncounterFlowState(): EncounterFlowState? = observable.value?.encounterFlowState
    
    data class ViewState(
        val type: Billable.Type? = null,
        val billableWithPriceSchedule: BillableWithPriceSchedule? = null,
        val encounterFlowState: EncounterFlowState,
        val selectableBillables: List<BillableWithPriceSchedule> = emptyList(),
        val billableTypes: List<Billable.Type> = emptyList()
    )
}
