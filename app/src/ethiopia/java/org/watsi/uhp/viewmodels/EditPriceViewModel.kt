package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import org.watsi.domain.entities.Billable
import org.watsi.domain.relations.EncounterItemWithBillableAndPrice
import javax.inject.Inject

class EditPriceViewModel @Inject constructor() : ViewModel() {
    private val observable = MutableLiveData<ViewState>()

    fun getObservable(
            encounterItemRelation: EncounterItemWithBillableAndPrice
    ): LiveData<ViewState> {
        observable.value = ViewState(
            encounterItemRelation.billableWithPriceSchedule.billable,
            encounterItemRelation.billableWithPriceSchedule.priceSchedule.price,
            encounterItemRelation.encounterItem.quantity,
            encounterItemRelation.price()
        )
        return observable
    }

    fun updateUnitPrice(unitPrice: Int) {
        observable.value?.let { viewState ->
            if (unitPrice != viewState.unitPrice) {
                observable.value = ViewState(
                        viewState.billable,
                        unitPrice,
                        viewState.quantity,
                        unitPrice * viewState.quantity
                )
            }
        }
    }

    fun updateQuantity(quantity: Int) {
        observable.value?.let { viewState ->
            if (quantity != viewState.quantity) {
                observable.value = ViewState(
                        viewState.billable,
                        viewState.unitPrice,
                        quantity,
                        viewState.unitPrice * quantity
                )
            }
        }
    }

    fun updateTotalPrice(totalPrice: Int) {
        observable.value?.let { viewState ->
            if (totalPrice != viewState.totalPrice) {
                observable.value = ViewState(
                        viewState.billable,
                        totalPrice / viewState.quantity,
                        viewState.quantity,
                        totalPrice
                )
            }
        }
    }

    data class ViewState(val billable: Billable?,
                         val unitPrice: Int,
                         val quantity: Int,
                         val totalPrice: Int)
}
