package org.watsi.uhp.viewmodels

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import javax.inject.Inject

class NewClaimViewModel @Inject constructor() : ViewModel() {
    private val viewStateObservable = MutableLiveData<ViewState>()

    companion object {
        const val INVALID_MEMBERSHIP_NUMBER_ERROR = "Invalid membership number"
    }

    fun onRegionNumberChange(regionNumber: String) {
        viewStateObservable.value?.let {
            viewStateObservable.value = it.copy(regionNumber = regionNumber, error = "")
        }
    }

    fun onWoredaNumberChange(woredaNumber: String) {
        viewStateObservable.value?.let {
            viewStateObservable.value = it.copy(woredaNumber = woredaNumber, error = "")
        }
    }

    fun onKebeleNumberChange(kebeleNumber: String) {
        viewStateObservable.value?.let {
            viewStateObservable.value = it.copy(kebeleNumber = kebeleNumber, error = "")
        }
    }

    fun onMemberStatusChange(memberStatus: String) {
        viewStateObservable.value?.let {
            viewStateObservable.value = it.copy(memberStatus = memberStatus, error = "")
        }
    }

    fun onHouseholdNumberChange(householdNumber: String) {
        viewStateObservable.value?.let {
            viewStateObservable.value = it.copy(householdNumber = householdNumber, error = "")
        }
    }

    fun onHouseholdMemberNumberChange(householdMemberNumber: String) {
        viewStateObservable.value?.let {
            viewStateObservable.value = it.copy(householdMemberNumber = householdMemberNumber, error = "")
        }
    }

    fun hasError(viewState: ViewState): Boolean {
        return viewState.regionNumber.isBlank() || viewState.woredaNumber.isBlank() || viewState.kebeleNumber.isBlank() ||
                viewState.memberStatus.isBlank() || viewState.householdNumber.isBlank()
    }

    fun setError() {
        viewStateObservable.value?.let {
            viewStateObservable.value = it.copy(error = INVALID_MEMBERSHIP_NUMBER_ERROR)
        }
    }

    data class ViewState(val regionNumber: String = "",
                         val woredaNumber: String = "",
                         val kebeleNumber: String = "",
                         val memberStatus: String = "",
                         val householdNumber: String = "",
                         val householdMemberNumber: String = "",
                         val error: String = "")
}
