package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import org.threeten.bp.Instant
import org.watsi.domain.entities.Billable
import org.watsi.domain.relations.EncounterItemWithBillable
import javax.inject.Inject

class EncounterViewModel @Inject constructor() : ViewModel() {

    private val observable = MutableLiveData<ViewState>()

    fun getObservable(initialEncounterItems: List<EncounterItemWithBillable>): LiveData<ViewState> {
        observable.value = ViewState(encounterItems = initialEncounterItems)
        return observable
    }

    fun selectType(type: Billable.Type?) {
        observable.value = observable.value?.copy(type = type)
    }

    data class ViewState(val type: Billable.Type? = null,
                         val selectableBillables: List<Billable> = emptyList(),
                         val backdatedOccurredAt: Instant? = null,
                         val encounterItems: List<EncounterItemWithBillable>)
}
