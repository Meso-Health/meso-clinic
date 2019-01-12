package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import org.threeten.bp.Instant
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.PriceSchedule
import org.watsi.domain.relations.BillableWithPriceSchedule
import org.watsi.domain.repositories.BillableRepository
import java.util.UUID
import javax.inject.Inject

class AddNewBillableViewModel @Inject constructor(
        billableRepository: BillableRepository,
        private val logger: Logger
) : ViewModel() {

    private val observable = MutableLiveData<ViewState>()

    init {
        observable.value = ViewState()
        billableRepository.uniqueCompositions().subscribe({
            observable.postValue(observable.value?.copy(compositions = it))
        }, {
            logger.error(it)
        })
    }

    fun getObservable(): LiveData<ViewState> {
        return observable
    }

    fun updateType(type: Billable.Type) {
        validateAndUpdate(observable.value?.copy(type = type))
    }

    fun updateName(name: String?) {
        validateAndUpdate(observable.value?.copy(name = clearEmptyString(name)))
    }

    fun updateComposition(composition: String?) {
        validateAndUpdate(observable.value?.copy(composition = clearEmptyString(composition)))
    }

    fun updateUnit(unit: String?) {
        validateAndUpdate(observable.value?.copy(unit = clearEmptyString(unit)))
    }

    fun updatePrice(price: Int?) {
        validateAndUpdate(observable.value?.copy(price = price))
    }

    fun getBillable(): BillableWithPriceSchedule? {
        val state = observable.value
        return if (state?.name != null && state.type != null && state.price != null && state.isValid) {
            val billable = Billable(
                id = UUID.randomUUID(),
                type = state.type,
                composition = when (state.type) {
                    Billable.Type.DRUG -> state.composition
                    Billable.Type.VACCINE -> Billable.VACCINE_COMPOSITION
                    Billable.Type.SUPPLY -> Billable.SUPPLY_COMPOSITION
                    else -> null
                },
                unit = when (state.type) {
                    in listOf(Billable.Type.DRUG, Billable.Type.VACCINE) -> state.unit
                    else -> null
                },
                price = state.price,
                name = state.name
            )
            val priceSchedule = PriceSchedule(
                id = UUID.randomUUID(),
                issuedAt = Instant.now(),
                billableId = billable.id,
                price = state.price,
                previousPriceScheduleModelId = null
            )
            return BillableWithPriceSchedule(billable, priceSchedule)
        } else {
            null
        }
    }

    private fun clearEmptyString(field: String?): String? {
        return if (field != null && !field.isEmpty()) field else null
    }

    private fun isStateValid(state: ViewState? = observable.value): Boolean {
        return (state?.name != null && state.type != null && state.price != null) &&
                !(state.type == Billable.Type.DRUG && (state.unit == null || state.composition == null))
    }

    private fun validateAndUpdate(state: ViewState? = observable.value) {
        val isValid = isStateValid(state)
        observable.value = state?.copy(isValid = isValid)
    }

    data class ViewState(val compositions: List<String> = emptyList(),
                         val composition: String? = null,
                         val unit: String? = null,
                         val price: Int? = null,
                         val name: String? = null,
                         val type: Billable.Type? = null,
                         val isValid: Boolean = false)
}
