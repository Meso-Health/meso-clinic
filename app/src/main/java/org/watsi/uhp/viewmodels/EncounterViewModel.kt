package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import me.xdrop.fuzzywuzzy.FuzzySearch
import org.threeten.bp.Instant
import org.watsi.domain.entities.Billable
import org.watsi.domain.repositories.BillableRepository
import javax.inject.Inject

class EncounterViewModel @Inject constructor(billableRepository: BillableRepository) : ViewModel() {

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
            billablesByType[type]!!.filter {  it -> !currentBillables.contains(it) }
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
            }.flatten()

            observable.value = observable.value?.copy(selectableBillables = matchingBillables)
        }
    }

    fun updateBackdatedOccurredAt(instant: Instant) {
        observable.value = observable.value?.copy(backdatedOccurredAt = instant)
    }

    data class ViewState(val type: Billable.Type? = null,
                         val selectableBillables: List<Billable> = emptyList(),
                         val backdatedOccurredAt: Instant? = null,
                         val lineItems: List<Pair<Billable, Int>>,
                         val searchResults: List<Billable> = emptyList())
}
