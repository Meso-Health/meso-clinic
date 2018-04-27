package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import org.watsi.domain.entities.Billable
import org.watsi.domain.repositories.BillableRepository
import java.util.UUID
import javax.inject.Inject

class AddNewBillableViewModel @Inject constructor(
        billableRepository: BillableRepository
) : ViewModel() {

    private val observable = MutableLiveData<ViewState>()
    private val compositions = billableRepository.uniqueCompositions()

    fun getObservable(): LiveData<ViewState> {
        observable.value = ViewState(compositions = compositions)
        return observable
    }

    fun updateType(type: Billable.Type) {
        observable.value = observable.value?.copy(type = type)
    }

    fun updateName(name: String) {
        observable.value = observable.value?.copy(name = name)
    }

    fun updateComposition(composition: String) {
        observable.value = observable.value?.copy(composition = composition)
    }

    fun updateUnit(unit: String) {
        observable.value = observable.value?.copy(unit = unit)
    }

    fun updatePrice(price: Int) {
        observable.value = observable.value?.copy(price = price)
    }

    fun getBillable(): Billable? {
        val state = observable.value
        return if (state?.name != null && state.type != null && state.price != null) {
            if (state.type == Billable.Type.DRUG &&
                    (state.unit == null || state.composition == null)) {
                null
            } else {
                Billable(id = UUID.randomUUID(),
                         type = state.type,
                         composition = state.composition,
                         unit = state.unit,
                         price = state.price,
                         name = state.name)
            }
        } else {
            null
        }
    }

    data class ViewState(val compositions: List<String>,
                         val composition: String? = null,
                         val unit: String? = null,
                         val price: Int? = null,
                         val name: String? = null,
                         val type: Billable.Type? = null)
}
