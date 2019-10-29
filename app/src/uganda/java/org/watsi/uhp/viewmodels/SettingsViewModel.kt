package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import org.watsi.device.managers.PreferencesManager
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val observable = MutableLiveData<ViewState>()

    fun getObservable(copaymentAmount: Int): LiveData<ViewState> {
        observable.value = ViewState(copaymentAmount)
        return observable
    }

    fun updateCopaymentAmount(amount: Int?) {
        validateAndUpdate(observable.value?.copy(copaymentAmount = amount))
    }

    fun copaymentAmount(): Int? {
        return observable.value?.copaymentAmount
    }

    private fun validateAndUpdate(state: ViewState? = observable.value) {
        val currentDefaultCopayment = preferencesManager.getDefaultCopaymentAmount()
        val isValid = state?.copaymentAmount != null && state?.copaymentAmount != currentDefaultCopayment
        observable.value = state?.copy(isValid = isValid)
    }

    data class ViewState(val copaymentAmount: Int?,
                         val isValid: Boolean = false)
}
