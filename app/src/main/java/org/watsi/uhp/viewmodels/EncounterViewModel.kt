package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import me.xdrop.fuzzywuzzy.FuzzySearch
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterItem
import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.relations.EncounterItemWithBillable
import org.watsi.domain.relations.EncounterWithItemsAndForms
import org.watsi.domain.repositories.BillableRepository
import java.util.UUID
import javax.inject.Inject

class EncounterViewModel @Inject constructor(
        billableRepository: BillableRepository,
        private val clock: Clock
) : ViewModel() {

    private val observable = MutableLiveData<ViewState>()
    private val billables = billableRepository.all()
    private val billablesByType = billables.groupBy { it.type }
    private val uniqueDrugNames = billablesByType[Billable.Type.DRUG]!!.map { it.name }.distinct()

    fun getObservable(initialLineItems: List<Pair<Billable, Int>>): LiveData<ViewState> {
        observable.value = ViewState(lineItems = initialLineItems)
        return observable
    }

    fun selectType(type: Billable.Type?) {
        val selectableBillables = if (type != null && type != Billable.Type.DRUG) {
            val currentBillables = observable.value?.lineItems?.map { it.first } ?: emptyList()
            billablesByType[type]!!.filter {  it -> !currentBillables.contains(it) }.sortedBy { it.name }
        } else {
            emptyList()
        }
        observable.value = observable.value?.copy(
                type = type, selectableBillables = selectableBillables)
    }

    fun addItem(billable: Billable) {
        val updatedList = observable.value?.lineItems?.toMutableList() ?: mutableListOf()
        updatedList.add(Pair(billable, 1))
        observable.value = observable.value?.copy(lineItems = updatedList,
                                                  type = null,
                                                  selectableBillables = emptyList(),
                                                  searchResults = emptyList())
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
        observable.value = observable.value?.copy(backdatedOccurredAt = instant)
    }

    fun buildEncounterWithItemsAndForms(identificationEvent: IdentificationEvent): EncounterWithItemsAndForms? {
        val encounter = Encounter(id = UUID.randomUUID(),
                memberId = identificationEvent.memberId,
                identificationEventId = identificationEvent.id,
                occurredAt = clock.instant(),
                backdatedOccurredAt = observable.value?.backdatedOccurredAt)
        return observable.value?.lineItems?.map {
            val encounterItem = EncounterItem(
                    UUID.randomUUID(), encounter.id, it.first.id, it.second)
            EncounterItemWithBillable(encounterItem, it.first)
        }?.let { encounterItems ->
            EncounterWithItemsAndForms(encounter, encounterItems, emptyList())
        }
    }

    data class ViewState(val type: Billable.Type? = null,
                         val selectableBillables: List<Billable> = emptyList(),
                         val backdatedOccurredAt: Instant? = null,
                         val lineItems: List<Pair<Billable, Int>>,
                         val searchResults: List<Billable> = emptyList())
}
