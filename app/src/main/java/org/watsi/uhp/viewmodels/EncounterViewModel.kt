package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import org.threeten.bp.Instant
import org.watsi.domain.entities.Billable
import org.watsi.domain.repositories.BillableRepository
import javax.inject.Inject

class EncounterViewModel @Inject constructor(
        private val billableRepository: BillableRepository
) : ViewModel() {

    private val observable = MutableLiveData<ViewState>()

    fun getObservable(initialLineItems: List<Pair<Billable, Int>>): LiveData<ViewState> {
        observable.value = ViewState(lineItems = initialLineItems)
        return observable
    }

    fun selectType(type: Billable.Type?) {
        val selectableBillables = if (type != null) {
            val currentBillables = observable.value?.lineItems?.map { it.first } ?: emptyList()
            billableRepository.findByType(type).filter {  it -> !currentBillables.contains(it) }
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
                                                  selectableBillables = emptyList())
    }

    data class ViewState(val type: Billable.Type? = null,
                         val selectableBillables: List<Billable> = emptyList(),
                         val backdatedOccurredAt: Instant? = null,
                         val lineItems: List<Pair<Billable, Int>>)
}
