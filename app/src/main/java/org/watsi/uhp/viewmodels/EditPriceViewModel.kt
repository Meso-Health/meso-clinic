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
            encounterItemRelation.price(),
            encounterItemRelation.encounterItem.stockout
        )
        return observable
    }

    fun updateUnitPrice(unitPrice: Int) {
        observable.value?.let { viewState ->
            if (unitPrice != viewState.unitPrice) {
                observable.value = viewState.copy(
                    unitPrice = unitPrice,
                    totalPrice = unitPrice * viewState.quantity
                )
            }
        }
    }

    fun updateQuantity(quantity: Int) {
        observable.value?.let { viewState ->
            if (quantity != viewState.quantity) {
                observable.value = viewState.copy(
                    quantity = quantity,
                    totalPrice = viewState.unitPrice * quantity
                )
            }
        }
    }

    fun updateTotalPrice(totalPrice: Int) {
        observable.value?.let { viewState ->
            if (totalPrice != viewState.totalPrice) {
                observable.value = viewState.copy(
                    unitPrice = totalPrice / viewState.quantity,
                    totalPrice = totalPrice
                )
            }
        }
    }

    fun updateStockout(stockout: Boolean) {
        observable.value?.let { viewState ->
            if (stockout != viewState.stockout) {
                observable.value = viewState.copy(stockout = stockout)
            }
        }
    }

    data class ViewState(
        val billable: Billable?,
        val unitPrice: Int,
        val quantity: Int,
        val totalPrice: Int,
        val stockout: Boolean
    )
}
